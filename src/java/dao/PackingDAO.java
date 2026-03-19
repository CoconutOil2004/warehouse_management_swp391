package dao;

import context.DBContext;
import dto.PackingDTO;
import dto.GDNLineDTO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackingDAO extends DBContext {

    public Long createPackingForGDN(Long gdnId) throws Exception {
        String sql = """
                INSERT INTO packing (gdn_id, status, packed_by, packed_at, package_label)
                VALUES (?, 'PENDING', NULL, NULL, NULL)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, gdnId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public PackingDTO getByPackId(Long packId) throws Exception {
        String sql = """
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, 
                       p.packed_at, p.package_label, p.package_type, p.weight, p.weight_unit, p.notes,
                       p.total_packages, p.current_package_num
                FROM packing p
                JOIN goods_delivery_note gdn ON gdn.gdn_id = p.gdn_id
                LEFT JOIN `user` u ON u.user_id = p.packed_by
                WHERE p.pack_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, packId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPackingDTO(rs);
                }
            }
        }
        return null;
    }

    public PackingDTO getByGdnId(Long gdnId) throws Exception {
        String sql = """
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, 
                       p.packed_at, p.package_label, p.package_type, p.weight, p.weight_unit, p.notes,
                       p.total_packages, p.current_package_num
                FROM packing p
                JOIN goods_delivery_note gdn ON gdn.gdn_id = p.gdn_id
                LEFT JOIN `user` u ON u.user_id = p.packed_by
                WHERE p.gdn_id = ?
                ORDER BY p.pack_id DESC
                LIMIT 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPackingDTO(rs);
                }
            }
        }
        return null;
    }

    public List<PackingDTO> listByGdnId(Long gdnId) throws Exception {
        List<PackingDTO> list = new ArrayList<>();
        String sql = """
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, p.packed_at, p.package_label
                FROM packing p
                JOIN goods_delivery_note gdn ON gdn.gdn_id = p.gdn_id
                LEFT JOIN `user` u ON u.user_id = p.packed_by
                WHERE p.gdn_id = ?
                ORDER BY p.pack_id DESC
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPackingDTO(rs));
                }
            }
        }
        return list;
    }

    private static PackingDTO mapPackingDTO(ResultSet rs) throws SQLException {
        PackingDTO dto = new PackingDTO();
        dto.setPackId(rs.getLong("pack_id"));
        dto.setGdnId(rs.getLong("gdn_id"));
        dto.setGdnNumber(rs.getString("gdn_number"));
        dto.setStatus(rs.getString("status"));
        dto.setPackedBy(rs.getObject("packed_by") != null ? rs.getLong("packed_by") : null);
        dto.setPackedByName(rs.getString("packed_by_name"));
        Timestamp packedAt = rs.getTimestamp("packed_at");
        if (packedAt != null) {
            dto.setPackedAt(packedAt.toLocalDateTime());
        }
        dto.setPackageLabel(getStringSafe(rs, "package_label"));
        dto.setPackageType(getStringSafe(rs, "package_type"));
        dto.setWeight(getBigDecimalSafe(rs, "weight"));
        dto.setWeightUnit(getStringSafe(rs, "weight_unit"));
        dto.setNotes(getStringSafe(rs, "notes"));
        dto.setTotalPackages(getIntSafe(rs, "total_packages"));
        dto.setCurrentPackageNum(getIntSafe(rs, "current_package_num"));
        return dto;
    }

    private static String getStringSafe(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getString(col);
        } catch (SQLException e) {
            // Backward-compatible with older SELECTs / schemas.
            return null;
        }
    }

    private static BigDecimal getBigDecimalSafe(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getBigDecimal(col);
        } catch (SQLException e) {
            return null;
        }
    }

    private static Integer getIntSafe(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getObject(col) != null ? rs.getInt(col) : null;
        } catch (SQLException e) {
            return null;
        }
    }

    public void updatePacking(Long packId, String status, Long packedBy, String packageLabel) throws Exception {
        updatePacking(packId, status, packedBy, packageLabel, null, null, null, null, null, null);
    }

    public void updatePacking(Long packId, String status, Long packedBy, String packageLabel,
                              String packageType, BigDecimal weight, String weightUnit,
                              String notes, Integer totalPackages, Integer currentPackageNum) throws Exception {
        String sql = """
                UPDATE packing SET 
                    status = ?, 
                    packed_by = ?, 
                    packed_at = CASE WHEN ? = 'DONE' THEN NOW() ELSE packed_at END,
                    package_label = ?,
                    package_type = ?,
                    weight = ?,
                    weight_unit = ?,
                    notes = ?,
                    total_packages = ?,
                    current_package_num = ?
                WHERE pack_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (packedBy != null) {
                ps.setLong(2, packedBy);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, status);
            ps.setString(4, packageLabel);
            ps.setString(5, packageType);
            if (weight != null) {
                ps.setBigDecimal(6, weight);
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setString(7, weightUnit);
            ps.setString(8, notes);
            if (totalPackages != null) {
                ps.setInt(9, totalPackages);
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            if (currentPackageNum != null) {
                ps.setInt(10, currentPackageNum);
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setLong(11, packId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Backward-compatible fallback when DB schema hasn't been migrated yet.
            // (e.g. missing package_type/weight/notes columns)
            String legacySql = """
                    UPDATE packing SET
                        status = ?,
                        packed_by = ?,
                        packed_at = CASE WHEN ? = 'DONE' THEN NOW() ELSE packed_at END,
                        package_label = ?
                    WHERE pack_id = ?
                    """;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(legacySql)) {
                ps.setString(1, status);
                if (packedBy != null) {
                    ps.setLong(2, packedBy);
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, status);
                ps.setString(4, packageLabel);
                ps.setLong(5, packId);
                ps.executeUpdate();
            }
        }
    }

    public void assignPacking(Long packId, Long packedBy) throws Exception {
        String sql = "UPDATE packing SET packed_by = ? WHERE pack_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (packedBy != null) {
                ps.setLong(1, packedBy);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, packId);
            ps.executeUpdate();
        }
    }

    public void updateGDNLinesPacked(Long gdnId, Map<Long, BigDecimal> lineQtyPacked) throws Exception {
        if (lineQtyPacked == null || lineQtyPacked.isEmpty()) {
            return;
        }
        String sql = "UPDATE goods_delivery_line SET qty_packed = ? WHERE gdn_line_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<Long, BigDecimal> e : lineQtyPacked.entrySet()) {
                ps.setBigDecimal(1, e.getValue());
                ps.setLong(2, e.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<PackingDTO> listByStatus(String status) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, 
                       p.packed_at, p.package_label, p.package_type, p.weight, p.weight_unit, p.notes,
                       p.total_packages, p.current_package_num,
                       so.so_number, c.name AS customer_name
                FROM packing p
                JOIN goods_delivery_note gdn ON gdn.gdn_id = p.gdn_id
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN `user` u ON u.user_id = p.packed_by
                WHERE 1=1
                """);
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status = ?");
        }
        sql.append(" ORDER BY p.pack_id DESC");

        List<PackingDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PackingDTO dto = mapPackingDTO(rs);
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public List<PackingDTO> listReadyForPacking() throws Exception {
        String sql = """
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, 
                       p.packed_at, p.package_label, p.package_type, p.weight, p.weight_unit, p.notes,
                       p.total_packages, p.current_package_num,
                       so.so_number, c.name AS customer_name
                FROM packing p
                JOIN goods_delivery_note gdn ON gdn.gdn_id = p.gdn_id
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN `user` u ON u.user_id = p.packed_by
                WHERE p.status IN ('PENDING', 'IN_PROGRESS')
                AND gdn.status IN ('PACKING')
                ORDER BY p.pack_id ASC
                """;
        List<PackingDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPackingDTO(rs));
            }
        }
        return list;
    }

    public List<PackingDTO> listGDNsReadyForPacking() throws Exception {
        String sql = """
                SELECT DISTINCT gdn.gdn_id, gdn.gdn_number, gdn.status as gdn_status,
                       so.so_number, c.name AS customer_name,
                       (SELECT p.pack_id FROM packing p WHERE p.gdn_id = gdn.gdn_id ORDER BY p.pack_id DESC LIMIT 1) as pack_id
                FROM goods_delivery_note gdn
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                WHERE gdn.status IN ('PACKING')
                ORDER BY gdn.gdn_id DESC
                """;
        List<PackingDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PackingDTO dto = new PackingDTO();
                dto.setGdnId(rs.getLong("gdn_id"));
                dto.setGdnNumber(rs.getString("gdn_number"));
                dto.setStatus("PENDING");
                dto.setCustomerName(rs.getString("customer_name"));
                dto.setPackId(rs.getObject("pack_id") != null ? rs.getLong("pack_id") : null);
                list.add(dto);
            }
        }
        return list;
    }

    public void updateLinePackedQty(Long gdnLineId, BigDecimal qtyPacked) throws Exception {
        String sql = "UPDATE goods_delivery_line SET qty_packed = ? WHERE gdn_line_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, qtyPacked);
            ps.setLong(2, gdnLineId);
            ps.executeUpdate();
        }
    }

    public boolean isAllLinesFullyPacked(Long gdnId) throws Exception {
        String sql = """
                SELECT COUNT(*) FROM goods_delivery_line 
                WHERE gdn_id = ? AND qty_packed < qty_picked
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public List<GDNLineDTO> getPackingLines(Long gdnId) throws Exception {
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
}
