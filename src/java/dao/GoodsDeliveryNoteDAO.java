package dao;

import context.DBContext;
import dto.GDNListDTO;
import dto.GDNDetailDTO;
import dto.GDNLineDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoodsDeliveryNoteDAO extends DBContext {

    /**
     * Returns so_id list that already have a GDN (one SO = one GDN only).
     */
    public List<Long> getSoIdsThatHaveGdn() throws Exception {
        String sql = "SELECT DISTINCT so_id FROM goods_delivery_note WHERE so_id IS NOT NULL";
        List<Long> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getLong("so_id"));
            }
        }
        return list;
    }

    /**
     * Get list of GDN with pagination and filters
     */
    public List<GDNListDTO> getGDNList(String gdnNumber, String soNumber, String status,
            int limit, int offset) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    gdn.gdn_id,
                    gdn.gdn_number,
                    so.so_number,
                    c.name AS customer_name,
                    gdn.status,
                    u.full_name AS creator_name,
                    gdn.created_at,
                    gdn.confirmed_at
                FROM goods_delivery_note gdn
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN user u ON gdn.created_by = u.user_id
                WHERE 1=1
            """);

        if (gdnNumber != null && !gdnNumber.isBlank()) {
            sql.append(" AND gdn.gdn_number LIKE ?");
        }
        if (soNumber != null && !soNumber.isBlank()) {
            sql.append(" AND so.so_number LIKE ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND gdn.status = ?");
        }

        sql.append(" ORDER BY gdn.gdn_id DESC LIMIT ? OFFSET ?");

        List<GDNListDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (gdnNumber != null && !gdnNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + gdnNumber + "%");
            }
            if (soNumber != null && !soNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + soNumber + "%");
            }
            if (status != null && !status.isBlank()) {
                ps.setString(paramIdx++, status);
            }
            ps.setInt(paramIdx++, limit);
            ps.setInt(paramIdx++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GDNListDTO dto = new GDNListDTO();
                    dto.setGdnId(rs.getLong("gdn_id"));
                    dto.setGdnNumber(rs.getString("gdn_number"));
                    dto.setSoNumber(rs.getString("so_number"));
                    dto.setCustomerName(rs.getString("customer_name"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatorName(rs.getString("creator_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
                    if (confirmedAt != null) {
                        dto.setConfirmedAt(confirmedAt.toLocalDateTime());
                    }
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * Count GDN with filters
     */
    public int countGDN(String gdnNumber, String soNumber, String status) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM goods_delivery_note gdn
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                WHERE 1=1
            """);

        if (gdnNumber != null && !gdnNumber.isBlank()) {
            sql.append(" AND gdn.gdn_number LIKE ?");
        }
        if (soNumber != null && !soNumber.isBlank()) {
            sql.append(" AND so.so_number LIKE ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND gdn.status = ?");
        }

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (gdnNumber != null && !gdnNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + gdnNumber + "%");
            }
            if (soNumber != null && !soNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + soNumber + "%");
            }
            if (status != null && !status.isBlank()) {
                ps.setString(paramIdx++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get GDN detail by ID
     */
    public GDNDetailDTO getGDNDetailById(Long gdnId) throws Exception {
        String sql = """
                SELECT 
                    gdn.gdn_id,
                    gdn.gdn_number,
                    gdn.warehouse_id,
                    gdn.so_id,
                    so.so_number,
                    so.customer_id,
                    c.name AS customer_name,
                    so.ship_to_address AS customer_address,
                    gdn.gdn_type,
                    gdn.status,
                    gdn.created_by,
                    u.full_name AS creator_name,
                    gdn.created_at,
                    gdn.confirmed_at
                FROM goods_delivery_note gdn
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN user u ON gdn.created_by = u.user_id
                WHERE gdn.gdn_id = ?
            """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GDNDetailDTO dto = new GDNDetailDTO();
                    dto.setGdnId(rs.getLong("gdn_id"));
                    dto.setGdnNumber(rs.getString("gdn_number"));
                    dto.setWarehouseId(rs.getObject("warehouse_id") != null ? rs.getLong("warehouse_id") : null);
                    dto.setSoId(rs.getObject("so_id") != null ? rs.getLong("so_id") : null);
                    dto.setSoNumber(rs.getString("so_number"));
                    dto.setCustomerId(rs.getObject("customer_id") != null ? rs.getLong("customer_id") : null);
                    dto.setCustomerName(rs.getString("customer_name"));
                    dto.setCustomerAddress(rs.getString("customer_address"));
                    dto.setGdnType(rs.getString("gdn_type"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null);
                    dto.setCreatorName(rs.getString("creator_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
                    if (confirmedAt != null) {
                        dto.setConfirmedAt(confirmedAt.toLocalDateTime());
                    }

                    // Get GDN lines
                    List<GDNLineDTO> lines = getGDNLines(gdnId);
                    dto.setLines(lines);

                    return dto;
                }
            }
        }
        return null;
    }

    /**
     * Get GDN lines with inventory availability (per GDN warehouse).
     */
    private List<GDNLineDTO> getGDNLines(Long gdnId) throws Exception {
        String sql = """
                SELECT 
                    gdl.gdn_line_id,
                    gdl.so_line_id,
                    gdl.variant_id,
                    pv.variant_sku,
                    p.name AS product_name,
                    pv.color,
                    pv.size,
                    gdl.qty_required,
                    gdl.qty_picked,
                    gdl.qty_packed,
                    COALESCE(SUM(ib.qty_available), 0) AS qty_available
                FROM goods_delivery_line gdl
                JOIN goods_delivery_note gdn ON gdl.gdn_id = gdn.gdn_id
                JOIN product_variant pv ON gdl.variant_id = pv.variant_id
                JOIN product p ON pv.product_id = p.product_id
                LEFT JOIN inventory_balance ib ON ib.variant_id = gdl.variant_id AND ib.warehouse_id = gdn.warehouse_id
                WHERE gdl.gdn_id = ?
                GROUP BY gdl.gdn_line_id, gdl.so_line_id, gdl.variant_id,
                         pv.variant_sku, p.name, pv.color, pv.size,
                         gdl.qty_required, gdl.qty_picked, gdl.qty_packed
                ORDER BY gdl.gdn_line_id
            """;

        List<GDNLineDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GDNLineDTO line = new GDNLineDTO();
                    line.setGdnLineId(rs.getLong("gdn_line_id"));
                    line.setSoLineId(rs.getObject("so_line_id") != null ? rs.getLong("so_line_id") : null);
                    line.setVariantId(rs.getLong("variant_id"));
                    line.setVariantSku(rs.getString("variant_sku"));
                    line.setProductName(rs.getString("product_name"));
                    line.setColor(rs.getString("color"));
                    line.setSize(rs.getString("size"));
                    line.setQtyRequired(rs.getBigDecimal("qty_required"));
                    line.setQtyPicked(rs.getBigDecimal("qty_picked"));
                    line.setQtyPacked(rs.getBigDecimal("qty_packed"));
                    line.setQtyAvailable(rs.getBigDecimal("qty_available"));
                    list.add(line);
                }
            }
        }
        return list;
    }

    /**
     * Create GDN from Sales Order.
     * Auto-generates lines from SO lines.
     * Status is always PENDING when created (same as GRN).
     */
    public Long createGDNFromSO(Long soId, Long warehouseId, Long createdBy) throws Exception {
        String sqlGDN = """
                INSERT INTO goods_delivery_note
                    (gdn_number, warehouse_id, so_id, gdn_type, status, created_by, created_at)
                VALUES (?, ?, ?, 'CUSTOMER', 'PENDING', ?, NOW())
            """;

        String sqlLine = """
                INSERT INTO goods_delivery_line
                    (gdn_id, so_line_id, variant_id, qty_required, qty_picked, qty_packed)
                VALUES (?, ?, ?, ?, 0, 0)
            """;

        // Generate GDN number
        String gdnNumber = generateGDNNumber();

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psGDN = conn.prepareStatement(sqlGDN, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement psLine = conn.prepareStatement(sqlLine)) {

                // Insert GDN header (status = PENDING like GRN)
                psGDN.setString(1, gdnNumber);
                psGDN.setLong(2, warehouseId);
                psGDN.setLong(3, soId);
                if (createdBy != null) {
                    psGDN.setLong(4, createdBy);
                } else {
                    psGDN.setNull(4, Types.BIGINT);
                }

                psGDN.executeUpdate();

                Long gdnId;
                try (ResultSet rs = psGDN.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Cannot get generated gdn_id");
                    }
                    gdnId = rs.getLong(1);
                }

                // Get SO lines and create GDN lines
                SaleOrderDAO soDAO = new SaleOrderDAO();
                List<dto.SaleOrderLineDTO> soLines = soDAO.getSaleOrderDetailLines(soId);

                for (dto.SaleOrderLineDTO soLine : soLines) {
                    psLine.setLong(1, gdnId);
                    psLine.setLong(2, soLine.getSoLineId());
                    psLine.setLong(3, soLine.getVariantId());
                    psLine.setBigDecimal(4, soLine.getOrderedQty());
                    psLine.addBatch();
                }
                psLine.executeBatch();

                conn.commit();
                return gdnId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Update GDN status
     */
    public boolean updateGDNStatus(Long gdnId, String status) throws Exception {
        String sql = """
                UPDATE goods_delivery_note
                SET status = ?, confirmed_at = CASE WHEN ? = 'CONFIRMED' THEN NOW() ELSE confirmed_at END
                WHERE gdn_id = ?
            """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setLong(3, gdnId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Update GDN line picked/packed quantities
     */
    public boolean updateGDNLineQuantities(Long gdnLineId, java.math.BigDecimal qtyPicked, 
            java.math.BigDecimal qtyPacked) throws Exception {
        String sql = """
                UPDATE goods_delivery_line
                SET qty_picked = ?, qty_packed = ?
                WHERE gdn_line_id = ?
            """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, qtyPicked);
            ps.setBigDecimal(2, qtyPacked);
            ps.setLong(3, gdnLineId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deduct inventory when GDN is confirmed (qty_picked per line from warehouse).
     */
    public void deductInventoryOnConfirm(Long gdnId) throws Exception {
        GDNDetailDTO gdn = getGDNDetailById(gdnId);
        if (gdn == null || gdn.getWarehouseId() == null || gdn.getLines() == null) return;
        InventoryBalanceDAO invDao = new InventoryBalanceDAO();
        for (GDNLineDTO line : gdn.getLines()) {
            java.math.BigDecimal qty = line.getQtyPicked() != null ? line.getQtyPicked() : java.math.BigDecimal.ZERO;
            if (qty.signum() > 0 && line.getVariantId() != null) {
                invDao.deductQtyFromWarehouseVariant(gdn.getWarehouseId(), line.getVariantId(), qty);
            }
        }
    }

    /**
     * Generate GDN number
     */
    private String generateGDNNumber() throws Exception {
        String sql = "SELECT COUNT(*) + 1 AS next_num FROM goods_delivery_note";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextNum = rs.getInt("next_num");
                return "GDN-" + String.format("%06d", nextNum);
            }
        }
        return "GDN-000001";
    }
}
