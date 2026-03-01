package dao;

import context.DBContext;
import dto.SlotProductDTO;
import dto.SlotQtyDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InventoryBalanceDAO extends DBContext {

    public List<SlotProductDTO> getProductsBySlotId(Long slotId) throws Exception {
        List<SlotProductDTO> list = new ArrayList<>();
        
        String sql = """
            SELECT 
                ib.variant_id,
                pv.variant_sku,
                p.name AS product_name,
                p.sku AS product_sku,
                ib.`condition`,
                ib.qty_on_hand,
                ib.qty_reserved,
                ib.qty_available
            FROM inventory_balance ib
            JOIN product_variant pv ON pv.variant_id = ib.variant_id
            JOIN product p ON p.product_id = pv.product_id
            WHERE ib.slot_id = ?
            ORDER BY ib.variant_id
        """;

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setLong(1, slotId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SlotProductDTO dto = new SlotProductDTO();
                    dto.setVariantId(rs.getLong("variant_id"));
                    dto.setVariantSku(rs.getString("variant_sku"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setProductSku(rs.getString("product_sku"));
                    dto.setCondition(rs.getString("condition"));
                    dto.setQtyOnHand(rs.getBigDecimal("qty_on_hand"));
                    dto.setQtyReserved(rs.getBigDecimal("qty_reserved"));
                    dto.setQtyAvailable(rs.getBigDecimal("qty_available"));
                    
                    list.add(dto);
                }
            }
        }
        
        return list;
    }

    public void assignProductToSlot(Long warehouseId, Long slotId, Long variantId, 
                                     String condition, BigDecimal qty) throws Exception {
        String sqlCheck = """
            SELECT inv_balance_id, qty_on_hand 
            FROM inventory_balance
            WHERE warehouse_id = ? AND slot_id = ? AND variant_id = ? AND `condition` = ?
        """;
        
        String sqlInsert = """
            INSERT INTO inventory_balance 
            (warehouse_id, slot_id, variant_id, `condition`, qty_on_hand, qty_reserved, qty_available)
            VALUES (?, ?, ?, ?, ?, 0, ?)
        """;
        
        String sqlUpdate = """
            UPDATE inventory_balance
            SET qty_on_hand = qty_on_hand + ?,
                qty_available = qty_available + ?,
                updated_at = NOW()
            WHERE warehouse_id = ? AND slot_id = ? AND variant_id = ? AND `condition` = ?
        """;

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setLong(1, warehouseId);
                psCheck.setLong(2, slotId);
                psCheck.setLong(3, variantId);
                psCheck.setString(4, condition);
                
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        // Update existing
                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                            psUpdate.setBigDecimal(1, qty);
                            psUpdate.setBigDecimal(2, qty);
                            psUpdate.setLong(3, warehouseId);
                            psUpdate.setLong(4, slotId);
                            psUpdate.setLong(5, variantId);
                            psUpdate.setString(6, condition);
                            psUpdate.executeUpdate();
                        }
                    } else {
                        // Insert new
                        try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                            psInsert.setLong(1, warehouseId);
                            psInsert.setLong(2, slotId);
                            psInsert.setLong(3, variantId);
                            psInsert.setString(4, condition);
                            psInsert.setBigDecimal(5, qty);
                            psInsert.setBigDecimal(6, qty);
                            psInsert.executeUpdate();
                        }
                    }
                }
            }
            
            con.commit();
        } catch (Exception e) {
            throw e;
        }
    }

    public BigDecimal getTotalCapacityUsed(Long slotId) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(qty_on_hand), 0) AS total
            FROM inventory_balance
            WHERE slot_id = ?
        """;

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setLong(1, slotId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Get slots with available qty for a variant (for allocating from_slot_id in pick task lines).
     * Ordered by slot for consistent allocation (FIFO by slot code).
     */
    public List<SlotQtyDTO> getAvailableSlotsForVariant(Long warehouseId, Long variantId, BigDecimal minQty) throws Exception {
        List<SlotQtyDTO> list = new ArrayList<>();
        String sql = """
            SELECT ib.slot_id, s.code AS slot_code, z.zone_id, z.code AS zone_code, ib.qty_available
            FROM inventory_balance ib
            JOIN slot s ON s.slot_id = ib.slot_id
            JOIN zone z ON z.zone_id = s.zone_id
            WHERE ib.warehouse_id = ? AND ib.variant_id = ? AND ib.qty_available > 0
            ORDER BY z.code, s.code
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            ps.setLong(2, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SlotQtyDTO dto = new SlotQtyDTO();
                    dto.setSlotId(rs.getLong("slot_id"));
                    dto.setSlotCode(rs.getString("slot_code"));
                    dto.setZoneId(rs.getLong("zone_id"));
                    dto.setZoneCode(rs.getString("zone_code"));
                    dto.setQtyAvailable(rs.getBigDecimal("qty_available"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * Deduct quantity from warehouse inventory for a variant (outbound).
     * Deducts from slots that have qty_available; may span multiple rows.
     */
    public void deductQtyFromWarehouseVariant(Long warehouseId, Long variantId, BigDecimal qtyToDeduct) throws Exception {
        if (qtyToDeduct == null || qtyToDeduct.signum() <= 0) return;
        String sqlSelect = """
            SELECT inv_balance_id, slot_id, `condition`, qty_available
            FROM inventory_balance
            WHERE warehouse_id = ? AND variant_id = ? AND qty_available > 0
            ORDER BY inv_balance_id
            """;
        String sqlUpdate = """
            UPDATE inventory_balance
            SET qty_on_hand = qty_on_hand - ?,
                qty_available = qty_available - ?
            WHERE inv_balance_id = ?
            """;
        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
                psSelect.setLong(1, warehouseId);
                psSelect.setLong(2, variantId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    java.util.List<Object[]> rows = new java.util.ArrayList<>();
                    while (rs.next()) {
                        rows.add(new Object[] {
                            rs.getLong("inv_balance_id"),
                            rs.getBigDecimal("qty_available")
                        });
                    }
                    BigDecimal remaining = qtyToDeduct;
                    for (Object[] row : rows) {
                        if (remaining.signum() <= 0) break;
                        Long invBalanceId = (Long) row[0];
                        BigDecimal available = (BigDecimal) row[1];
                        BigDecimal deduct = remaining.min(available);
                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                            psUpdate.setBigDecimal(1, deduct);
                            psUpdate.setBigDecimal(2, deduct);
                            psUpdate.setLong(3, invBalanceId);
                            psUpdate.executeUpdate();
                        }
                        remaining = remaining.subtract(deduct);
                    }
                    if (remaining.signum() > 0) {
                        con.rollback();
                        throw new IllegalStateException("Insufficient inventory for variant " + variantId + ": need " + qtyToDeduct + ", short by " + remaining);
                    }
                }
            }
            con.commit();
        }
    }
}
