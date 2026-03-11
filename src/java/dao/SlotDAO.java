package dao;

import context.DBContext;
import model.Slot;
import dto.SlotDetailDTO;
import dao.InventoryBalanceDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SlotDAO extends DBContext {

    public List<Slot> getSlotsByZoneId(Long zoneId) throws Exception {
        List<Slot> list = new ArrayList<>();

        String sql = """
                    SELECT
                        slot_id,
                        zone_id,
                        code,
                        row_no,
                        col_no,
                        max_capacity,
                        capacity_uom,
                        status
                    FROM slot
                    WHERE zone_id = ?
                    ORDER BY row_no, col_no
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, zoneId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Slot s = new Slot();
                    s.setSlotId(rs.getLong("slot_id"));
                    s.setZoneId(rs.getLong("zone_id"));
                    s.setCode(rs.getString("code"));

                    Integer rowNo = rs.getObject("row_no") != null ? rs.getInt("row_no") : null;
                    s.setRowNo(rowNo);

                    Integer colNo = rs.getObject("col_no") != null ? rs.getInt("col_no") : null;
                    s.setColNo(colNo);

                    s.setMaxCapacity(rs.getBigDecimal("max_capacity"));
                    s.setCapacityUom(rs.getString("capacity_uom"));
                    s.setStatus(rs.getString("status"));

                    list.add(s);
                }
            }
        }

        return list;
    }

    public Long createSlot(Long zoneId, String code, Integer rowNo, Integer colNo,
            BigDecimal maxCapacity, String capacityUom) throws Exception {
        String sql = """
                    INSERT INTO slot (zone_id, code, row_no, col_no, max_capacity, capacity_uom, status)
                    VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, zoneId);
            ps.setString(2, code);

            if (rowNo != null) {
                ps.setInt(3, rowNo);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            if (colNo != null) {
                ps.setInt(4, colNo);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (maxCapacity != null) {
                ps.setBigDecimal(5, maxCapacity);
            } else {
                ps.setNull(5, java.sql.Types.DECIMAL);
            }

            ps.setString(6, capacityUom);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return null;
    }

    public boolean hasSlots(Long zoneId) throws Exception {
        String sql = """
                    SELECT COUNT(*) as count
                    FROM slot
                    WHERE zone_id = ?
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, zoneId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }

        return false;
    }

    /**
     * Kiểm tra xem code có tồn tại trong database không (toàn bộ, không chỉ zone
     * hiện tại)
     */
    public boolean slotCodeExists(String code) throws Exception {
        String sql = """
                    SELECT COUNT(*) as count
                    FROM slot
                    WHERE code = ?
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }

        return false;
    }

    public void createSlotsBatch(Long zoneId, int rows, int cols, String codePrefix) throws Exception {
        // Normalize codePrefix: trim và uppercase
        if (codePrefix == null) {
            codePrefix = "SLOT";
        }
        codePrefix = codePrefix.trim().toUpperCase();

        // Lấy tất cả codes hiện có trong toàn bộ database để tránh duplicate
        String sqlGetAllCodes = """
                    SELECT code FROM slot
                """;

        java.util.Set<String> allExistingCodes = new java.util.HashSet<>();
        try (Connection con = DBContext.getConnection();
                PreparedStatement psGetAll = con.prepareStatement(sqlGetAllCodes)) {
            try (ResultSet rs = psGetAll.executeQuery()) {
                while (rs.next()) {
                    allExistingCodes.add(rs.getString("code"));
                }
            }
        }

        String sql = """
                    INSERT INTO slot (zone_id, code, row_no, col_no, status)
                    VALUES (?, ?, ?, ?, 'ACTIVE')
                """;

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int insertedCount = 0;

                for (int row = 1; row <= rows; row++) {
                    for (int col = 1; col <= cols; col++) {
                        String code = codePrefix + "-R" + row + "-C" + col;

                        // Chỉ insert nếu chưa tồn tại (bỏ qua các codes đã tồn tại, không báo lỗi)
                        if (!allExistingCodes.contains(code)) {
                            ps.setLong(1, zoneId);
                            ps.setString(2, code);
                            ps.setInt(3, row);
                            ps.setInt(4, col);
                            ps.addBatch();
                            insertedCount++;
                        }
                        // Nếu đã tồn tại thì bỏ qua, không báo lỗi
                    }
                }

                if (insertedCount > 0) {
                    ps.executeBatch();
                    con.commit();
                }
                // Nếu không có slot nào được tạo (tất cả đều đã tồn tại), không báo lỗi, chỉ
                // không làm gì
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                con.rollback();
                // Nếu vẫn bị lỗi constraint (trường hợp hiếm), chỉ báo lỗi đơn giản
                throw new Exception(
                        "Some slot codes already exist and were skipped. New slots have been created successfully.");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<SlotDetailDTO> getSlotsWithInventoryByZoneId(Long zoneId, Long warehouseId) throws Exception {
        List<SlotDetailDTO> list = new ArrayList<>();

        // Bước 1: Lấy tất cả thông tin slot trước
        List<SlotDetailDTO> tempList = new ArrayList<>();
        String sql = """
                    SELECT
                        slot_id,
                        code,
                        row_no,
                        col_no,
                        max_capacity,
                        capacity_uom,
                        status
                    FROM slot
                    WHERE zone_id = ?
                    ORDER BY row_no, col_no
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, zoneId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SlotDetailDTO dto = new SlotDetailDTO();
                    dto.setSlotId(rs.getLong("slot_id"));
                    dto.setSlotCode(rs.getString("code"));
                    dto.setRowNo(rs.getObject("row_no") != null ? rs.getInt("row_no") : null);
                    dto.setColNo(rs.getObject("col_no") != null ? rs.getInt("col_no") : null);
                    dto.setStatus(rs.getString("status"));
                    dto.setMaxCapacity(rs.getBigDecimal("max_capacity"));
                    dto.setCapacityUom(rs.getString("capacity_uom"));
                    tempList.add(dto);
                }
            }
        }

        // Bước 2: Sau khi đóng ResultSet, mới lấy inventory data
        InventoryBalanceDAO invDAO = new InventoryBalanceDAO();
        for (SlotDetailDTO dto : tempList) {
            Long slotId = dto.getSlotId();

            // Lấy products trong slot
            List<dto.SlotProductDTO> products = invDAO.getProductsBySlotId(slotId);
            dto.setProducts(products);

            // Tính toán capacity
            BigDecimal usedCapacity = invDAO.getTotalCapacityUsed(slotId);
            dto.setUsedCapacity(usedCapacity);

            BigDecimal maxCap = dto.getMaxCapacity();
            if (maxCap != null) {
                dto.setAvailableCapacity(maxCap.subtract(usedCapacity));
            } else {
                dto.setAvailableCapacity(BigDecimal.ZERO);
            }

            dto.setIsEmpty(products.isEmpty());

            list.add(dto);
        }

        return list;
    }

    public SlotDetailDTO getSlotDetail(Long slotId, Long warehouseId) throws Exception {
        SlotDetailDTO dto = null;

        // Bước 1: Lấy thông tin slot trước
        String sql = """
                    SELECT
                        slot_id,
                        code,
                        row_no,
                        col_no,
                        max_capacity,
                        capacity_uom,
                        status
                    FROM slot
                    WHERE slot_id = ?
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, slotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto = new SlotDetailDTO();
                    dto.setSlotId(rs.getLong("slot_id"));
                    dto.setSlotCode(rs.getString("code"));
                    dto.setRowNo(rs.getObject("row_no") != null ? rs.getInt("row_no") : null);
                    dto.setColNo(rs.getObject("col_no") != null ? rs.getInt("col_no") : null);
                    dto.setStatus(rs.getString("status"));
                    dto.setMaxCapacity(rs.getBigDecimal("max_capacity"));
                    dto.setCapacityUom(rs.getString("capacity_uom"));
                }
            }
        }

        // Bước 2: Sau khi đóng ResultSet, mới lấy inventory data
        if (dto != null) {
            InventoryBalanceDAO invDAO = new InventoryBalanceDAO();

            // Lấy products trong slot
            List<dto.SlotProductDTO> products = invDAO.getProductsBySlotId(slotId);
            dto.setProducts(products);

            // Tính toán capacity
            BigDecimal usedCapacity = invDAO.getTotalCapacityUsed(slotId);
            dto.setUsedCapacity(usedCapacity);

            BigDecimal maxCap = dto.getMaxCapacity();
            if (maxCap != null) {
                dto.setAvailableCapacity(maxCap.subtract(usedCapacity));
            } else {
                dto.setAvailableCapacity(BigDecimal.ZERO);
            }

            dto.setIsEmpty(products.isEmpty());
        }

        return dto;
    }

    public String getZoneTypeBySlotId(Long slotId) throws Exception {
        String sql = """
                    SELECT z.zone_type
                    FROM slot s
                    JOIN zone z ON s.zone_id = z.zone_id
                    WHERE s.slot_id = ?
                """;
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("zone_type");
                }
            }
        }
        return null;
    }
    
    /** Check if slot belongs to the given warehouse (slot -> zone -> warehouse). */
    public boolean isSlotInWarehouse(Long slotId, Long warehouseId) throws Exception {
        if (slotId == null || warehouseId == null) return false;
        String sql = """
                    SELECT 1 FROM slot s
                    JOIN zone z ON s.zone_id = z.zone_id
                    WHERE s.slot_id = ? AND z.warehouse_id = ?
                """;
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, slotId);
            ps.setLong(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateMaxCapacity(Long slotId, BigDecimal maxCapacity) throws Exception {
        String sql = """
            UPDATE slot
            SET max_capacity = ?
            WHERE slot_id = ?
        """;
        
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (maxCapacity != null) {
                ps.setBigDecimal(1, maxCapacity);
            } else {
                ps.setNull(1, java.sql.Types.DECIMAL);
            }
            ps.setLong(2, slotId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
