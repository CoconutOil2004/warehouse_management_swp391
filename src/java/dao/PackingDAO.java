package dao;

import context.DBContext;
import dto.PackingDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, p.packed_at, p.package_label
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
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, p.packed_at, p.package_label
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
        dto.setPackageLabel(rs.getString("package_label"));
        return dto;
    }

    public void updatePacking(Long packId, String status, Long packedBy, String packageLabel) throws Exception {
        String sql = """
                UPDATE packing SET status = ?, packed_by = ?, packed_at = NOW(), package_label = ?
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
            ps.setString(3, packageLabel);
            ps.setLong(4, packId);
            ps.executeUpdate();
        }
    }

    /**
     * Update GDN line qty_packed (e.g. set to qty_picked for full pack).
     */
    public void updateGDNLinesPacked(Long gdnId, java.util.Map<Long, java.math.BigDecimal> lineQtyPacked) throws Exception {
        if (lineQtyPacked == null || lineQtyPacked.isEmpty()) {
            return;
        }
        String sql = "UPDATE goods_delivery_line SET qty_packed = ? WHERE gdn_line_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (java.util.Map.Entry<Long, java.math.BigDecimal> e : lineQtyPacked.entrySet()) {
                ps.setBigDecimal(1, e.getValue());
                ps.setLong(2, e.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<PackingDTO> listByStatus(String status) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT p.pack_id, p.gdn_id, gdn.gdn_number, p.status, p.packed_by, u.full_name AS packed_by_name, p.packed_at, p.package_label
                FROM packing p
                JOIN goods_delivery_note gdn ON gdn.gdn_id = p.gdn_id
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
                    list.add(mapPackingDTO(rs));
                }
            }
        }
        return list;
    }
}
