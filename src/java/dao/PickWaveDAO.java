package dao;

import context.DBContext;
import dto.PickWaveDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PickWaveDAO extends DBContext {

    public Long createWaveFromGDN(Long gdnId, Long createdBy) throws Exception {
        String sql = """
                INSERT INTO pick_wave (gdn_id, status, created_by, created_at)
                VALUES (?, 'CREATED', ?, NOW())
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, gdnId);
            if (createdBy != null) {
                ps.setLong(2, createdBy);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public PickWaveDTO getWaveById(Long waveId) throws Exception {
        String sql = """
                SELECT pw.wave_id, pw.gdn_id, gdn.gdn_number, pw.status, pw.created_by, u.full_name AS created_by_name, pw.created_at
                FROM pick_wave pw
                JOIN goods_delivery_note gdn ON gdn.gdn_id = pw.gdn_id
                LEFT JOIN `user` u ON u.user_id = pw.created_by
                WHERE pw.wave_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PickWaveDTO dto = new PickWaveDTO();
                    dto.setWaveId(rs.getLong("wave_id"));
                    dto.setGdnId(rs.getLong("gdn_id"));
                    dto.setGdnNumber(rs.getString("gdn_number"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null);
                    dto.setCreatedByName(rs.getString("created_by_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    return dto;
                }
            }
        }
        return null;
    }

    public PickWaveDTO getWaveByGdnId(Long gdnId) throws Exception {
        String sql = """
                SELECT pw.wave_id, pw.gdn_id, gdn.gdn_number, pw.status, pw.created_by, u.full_name AS created_by_name, pw.created_at
                FROM pick_wave pw
                JOIN goods_delivery_note gdn ON gdn.gdn_id = pw.gdn_id
                LEFT JOIN `user` u ON u.user_id = pw.created_by
                WHERE pw.gdn_id = ?
                ORDER BY pw.wave_id DESC
                LIMIT 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PickWaveDTO dto = new PickWaveDTO();
                    dto.setWaveId(rs.getLong("wave_id"));
                    dto.setGdnId(rs.getLong("gdn_id"));
                    dto.setGdnNumber(rs.getString("gdn_number"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null);
                    dto.setCreatedByName(rs.getString("created_by_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    return dto;
                }
            }
        }
        return null;
    }

    public List<PickWaveDTO> getWaveList(String status, int limit, int offset) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT pw.wave_id, pw.gdn_id, gdn.gdn_number, pw.status, pw.created_by, u.full_name AS created_by_name, pw.created_at
                FROM pick_wave pw
                JOIN goods_delivery_note gdn ON gdn.gdn_id = pw.gdn_id
                LEFT JOIN `user` u ON u.user_id = pw.created_by
                WHERE 1=1
                """);
        if (status != null && !status.isBlank()) {
            sql.append(" AND pw.status = ?");
        }
        sql.append(" ORDER BY pw.wave_id DESC LIMIT ? OFFSET ?");

        List<PickWaveDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx++, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PickWaveDTO dto = new PickWaveDTO();
                    dto.setWaveId(rs.getLong("wave_id"));
                    dto.setGdnId(rs.getLong("gdn_id"));
                    dto.setGdnNumber(rs.getString("gdn_number"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null);
                    dto.setCreatedByName(rs.getString("created_by_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public int countWaves(String status) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM pick_wave pw
                WHERE 1=1
                """);
        if (status != null && !status.isBlank()) {
            sql.append(" AND pw.status = ?");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (status != null && !status.isBlank()) {
                ps.setString(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateWaveStatus(Long waveId, String status) throws Exception {
        String sql = "UPDATE pick_wave SET status = ? WHERE wave_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, waveId);
            ps.executeUpdate();
        }
    }

    public void deleteWaveById(Long waveId) throws Exception {
        String sql = "DELETE FROM pick_wave WHERE wave_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            ps.executeUpdate();
        }
    }
}
