package dao;

import context.DBContext;
import dto.PickTaskDTO;
import dto.PickWaveDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickWaveDAO extends DBContext {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public Long createWave(List<Long> gdnIds, Long createdBy) throws Exception {
        String sqlWave = """
      INSERT INTO pick_wave (status, created_by, created_at)
      VALUES ('CREATED', ?, NOW())
      """;
        String sqlWaveGdn = """
      INSERT INTO pick_wave_gdn (wave_id, gdn_id, created_at)
      VALUES (?, ?, NOW())
      """;
        try (
        Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (
            PreparedStatement psWave = conn.prepareStatement(
            sqlWave,
            Statement.RETURN_GENERATED_KEYS
            ); PreparedStatement psWaveGdn = conn.prepareStatement(sqlWaveGdn)) {
                if (createdBy != null) {
                    psWave.setLong(1, createdBy);
                } else {
                    psWave.setNull(1, Types.BIGINT);
                }
                psWave.executeUpdate();

                Long waveId = null;
                try (ResultSet rs = psWave.getGeneratedKeys()) {
                    if (rs.next()) {
                        waveId = rs.getLong(1);
                    }
                }

                if (waveId == null) {
                    conn.rollback();
                    return null;
                }

                for (Long gdnId : gdnIds) {
                    psWaveGdn.setLong(1, waveId);
                    psWaveGdn.setLong(2, gdnId);
                    psWaveGdn.executeUpdate();
                }

                conn.commit();

                PickTaskDAO taskDao = new PickTaskDAO();
                taskDao.createTasksFromWave(waveId);

                return waveId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public PickWaveDTO getWaveById(Long waveId) throws Exception {
        String sql = """
      SELECT pw.wave_id, pw.wave_code, pw.status,
             pw.created_by, u.full_name AS created_by_name, pw.created_at
      FROM pick_wave pw
      LEFT JOIN `user` u ON u.user_id = pw.created_by
      WHERE pw.wave_id = ?
      """;
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PickWaveDTO dto = new PickWaveDTO();
                    dto.setWaveId(rs.getLong("wave_id"));
                    dto.setWaveCode(rs.getString("wave_code"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(
                    rs.getObject("created_by") != null ? rs.getLong("created_by") : null
                    );
                    dto.setCreatedByName(rs.getString("created_by_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    dto.setGdns(getWaveGdns(waveId));
                    dto.setGdnCount(countWaveGdns(waveId));
                    return dto;
                }
            }
        }
        return null;
    }

    public PickWaveDTO getWaveByGdnId(Long gdnId) throws Exception {
        String sql = """
      SELECT pw.wave_id, pw.wave_code, pw.status, pw.created_by, u.full_name AS created_by_name, pw.created_at
      FROM pick_wave pw
      JOIN pick_wave_gdn pwg ON pw.wave_id = pwg.wave_id
      LEFT JOIN `user` u ON u.user_id = pw.created_by
      WHERE pwg.gdn_id = ?
      ORDER BY pw.wave_id DESC
      LIMIT 1
      """;
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PickWaveDTO dto = new PickWaveDTO();
                    dto.setWaveId(rs.getLong("wave_id"));
                    dto.setWaveCode(rs.getString("wave_code"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(
                    rs.getObject("created_by") != null ? rs.getLong("created_by") : null
                    );
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

    /**
     * Check if a GDN is already in any wave (via pick_wave_gdn). Returns the
     * wave_id if found, null otherwise.
     */
    public Long getWaveIdByGdnId(Long gdnId) throws Exception {
        String sql = """
      SELECT pwg.wave_id FROM pick_wave_gdn pwg
      WHERE pwg.gdn_id = ?
      LIMIT 1
      """;
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("wave_id");
                }
            }
        }
        return null;
    }

    public List<PickWaveDTO> getWaveList(String status, int limit, int offset)
    throws Exception {
        StringBuilder sql = new StringBuilder(
        """
      SELECT pw.wave_id, pw.wave_code, pw.status,
             pw.created_by, u.full_name AS created_by_name, pw.created_at,
             (SELECT COUNT(*) FROM pick_wave_gdn pwg WHERE pwg.wave_id = pw.wave_id) AS gdn_count
      FROM pick_wave pw
      LEFT JOIN `user` u ON u.user_id = pw.created_by
      WHERE 1=1
      """
        );
        if (status != null && !status.isBlank()) {
            sql.append(" AND pw.status = ?");
        }
        sql.append(" ORDER BY pw.wave_id DESC LIMIT ? OFFSET ?");

        List<PickWaveDTO> list = new ArrayList<>();
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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
                    dto.setWaveCode(rs.getString("wave_code"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedBy(
                    rs.getObject("created_by") != null ? rs.getLong("created_by") : null
                    );
                    dto.setCreatedByName(rs.getString("created_by_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        dto.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    dto.setGdnCount(rs.getInt("gdn_count"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public int countWaves(String status) throws Exception {
        StringBuilder sql = new StringBuilder(
        """
      SELECT COUNT(*)
      FROM pick_wave pw
      WHERE 1=1
      """
        );
        if (status != null && !status.isBlank()) {
            sql.append(" AND pw.status = ?");
        }

        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, waveId);
            ps.executeUpdate();
        }
    }

    public void deleteWaveById(Long waveId) throws Exception {
        String sql = "DELETE FROM pick_wave WHERE wave_id = ?";
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            ps.executeUpdate();
        }
    }

    /**
     * Get GDNs filtered by zones. GDN is included if it has ANY line item with
     * inventory in ANY of the selected zones.
     *
     * @param zoneIds List of zone IDs to filter by
     * @param status GDN status filter (e.g., "PENDING"), use null or empty for
     * all statuses
     * @param limit Maximum number of results
     * @param offset Offset for pagination
     * @return List of GDNListDTO matching the filter criteria
     */
    public List<dto.GDNListDTO> getGdnsByZoneFilter(
    List<Long> zoneIds,
    String status,
    int limit,
    int offset
    ) throws Exception {
        StringBuilder sql = new StringBuilder(
        """
      SELECT DISTINCT gdn.gdn_id, gdn.gdn_number, so.so_number,
             c.name AS customer_name, gdn.status,
             u.full_name AS creator_name, gdn.created_at, gdn.confirmed_at
      FROM goods_delivery_note gdn
      JOIN goods_delivery_line gdl ON gdn.gdn_id = gdl.gdn_id
      LEFT JOIN sales_order so ON gdn.so_id = so.so_id
      LEFT JOIN customer c ON so.customer_id = c.customer_id
      LEFT JOIN `user` u ON gdn.created_by = u.user_id
      WHERE 1=1
      """
        );

        // Add status filter
        if (status != null && !status.isBlank()) {
            sql.append(" AND gdn.status = ?");
        }

        // Add zone filter - GDN must have at least one line with inventory in selected zones
        if (zoneIds != null && !zoneIds.isEmpty()) {
            sql.append(
            """
            AND EXISTS (
                SELECT 1
                FROM inventory_balance ib
                JOIN slot s ON ib.slot_id = s.slot_id
                WHERE ib.variant_id = gdl.variant_id
                  AND s.zone_id IN (
        """
            );
            sql.append(String.join(",", Collections.nCopies(zoneIds.size(), "?")));
            sql.append(")");
            sql.append(")");
        }

        sql.append(" ORDER BY gdn.gdn_id DESC LIMIT ? OFFSET ?");

        List<dto.GDNListDTO> list = new ArrayList<>();
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }

            if (zoneIds != null && !zoneIds.isEmpty()) {
                for (Long zoneId : zoneIds) {
                    ps.setLong(idx++, zoneId);
                }
            }

            ps.setInt(idx++, limit);
            ps.setInt(idx++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.GDNListDTO dto = new dto.GDNListDTO();
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
     * Count GDNs filtered by zones.
     */
    public int countGdnsByZoneFilter(List<Long> zoneIds, String status)
    throws Exception {
        StringBuilder sql = new StringBuilder(
        """
      SELECT COUNT(DISTINCT gdn.gdn_id)
      FROM goods_delivery_note gdn
      JOIN goods_delivery_line gdl ON gdn.gdn_id = gdl.gdn_id
      WHERE 1=1
      """
        );

        if (status != null && !status.isBlank()) {
            sql.append(" AND gdn.status = ?");
        }

        if (zoneIds != null && !zoneIds.isEmpty()) {
            sql.append(
            """
            AND EXISTS (
                SELECT 1
                FROM inventory_balance ib
                JOIN slot s ON ib.slot_id = s.slot_id
                WHERE ib.variant_id = gdl.variant_id
                  AND s.zone_id IN (
        """
            );
            sql.append(String.join(",", Collections.nCopies(zoneIds.size(), "?")));
            sql.append(")");
            sql.append(")");
        }

        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }

            if (zoneIds != null && !zoneIds.isEmpty()) {
                for (Long zoneId : zoneIds) {
                    ps.setLong(idx++, zoneId);
                }
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
     * Add a GDN to an existing wave.
     *
     * @param waveId The wave ID
     * @param gdnId The GDN ID to add
     * @throws Exception if wave or GDN doesn't exist, or if already linked
     */
    public void addGdnToWave(Long waveId, Long gdnId) throws Exception {
        String sql = """
      INSERT INTO pick_wave_gdn (wave_id, gdn_id, created_at)
      VALUES (?, ?, NOW())
      """;
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            ps.setLong(2, gdnId);
            int rows = ps.executeUpdate();
            System.out.println("DEBUG addGdnToWave: waveId=" + waveId + ", gdnId=" + gdnId + ", rows=" + rows);
        } catch (SQLException e) {
            System.out.println("DEBUG addGdnToWave ERROR: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Remove a GDN from a wave.
     *
     * @param waveId The wave ID
     * @param gdnId The GDN ID to remove
     * @throws Exception if wave-gdn link doesn't exist
     */
    public void removeGdnFromWave(Long waveId, Long gdnId) throws Exception {
        String sql = "DELETE FROM pick_wave_gdn WHERE wave_id = ? AND gdn_id = ?";
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            ps.setLong(2, gdnId);
            ps.executeUpdate();
        }
    }

    /**
     * Get all GDNs in a wave.
     *
     * @param waveId The wave ID
     * @return List of GDNListDTO in the wave
     */
    public List<dto.GDNListDTO> getWaveGdns(Long waveId) throws Exception {
        String sql = """
      SELECT gdn.gdn_id, gdn.gdn_number, so.so_number,
             c.name AS customer_name, gdn.status,
             u.full_name AS creator_name, gdn.created_at, gdn.confirmed_at
      FROM pick_wave_gdn pwg
      JOIN goods_delivery_note gdn ON pwg.gdn_id = gdn.gdn_id
      LEFT JOIN sales_order so ON gdn.so_id = so.so_id
      LEFT JOIN customer c ON so.customer_id = c.customer_id
      LEFT JOIN `user` u ON gdn.created_by = u.user_id
      WHERE pwg.wave_id = ?
      ORDER BY gdn.gdn_id
      """;

        List<dto.GDNListDTO> list = new ArrayList<>();
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.GDNListDTO dto = new dto.GDNListDTO();
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
     * Get count of GDNs in a wave.
     */
    public int countWaveGdns(Long waveId) throws Exception {
        String sql = "SELECT COUNT(*) FROM pick_wave_gdn WHERE wave_id = ?";
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Release a wave - updates wave status to RELEASED.
     * Tasks are already created when wave is created.
     */
    public boolean releaseWave(Long waveId) throws Exception {
        PickWaveDTO wave = getWaveById(waveId);
        if (wave == null) {
            throw new SQLException("Wave not found: " + waveId);
        }

        if (!STATUS_CREATED.equals(wave.getStatus())) {
            throw new SQLException("Wave is not in CREATED status. Current status: " + wave.getStatus());
        }

        updateWaveStatus(waveId, STATUS_RELEASED);
        return true;
    }

    /**
     * Cancel a wave.
     */
    public void cancelWave(Long waveId) throws Exception {
        PickWaveDTO wave = getWaveById(waveId);
        if (wave == null) {
            throw new SQLException("Wave not found: " + waveId);
        }

        if (STATUS_DONE.equals(wave.getStatus()) || STATUS_CANCELLED.equals(wave.getStatus())) {
            throw new SQLException("Wave is already completed or cancelled");
        }

        updateWaveStatus(waveId, STATUS_CANCELLED);
    }

    /**
     * Check and update wave status based on task completion. If all tasks are
     * completed, update wave status to DONE.
     */
    public void checkAndUpdateWaveStatus(Long waveId) throws Exception {
        PickWaveDTO wave = getWaveById(waveId);
        if (wave == null) {
            return;
        }

        if (!STATUS_RELEASED.equals(wave.getStatus()) && !STATUS_IN_PROGRESS.equals(wave.getStatus())) {
            return;
        }

        PickTaskDAO taskDao = new PickTaskDAO();
        List<PickTaskDTO> tasks = taskDao.getTasksByWaveId(waveId);

        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        boolean allCompleted = tasks.stream()
        .allMatch(t -> PickTaskDTO.STATUS_COMPLETED.equals(t.getStatus()));

        if (allCompleted) {
            updateWaveStatus(waveId, STATUS_DONE);
        } else {
            boolean anyInProgress = tasks.stream()
            .anyMatch(t -> PickTaskDTO.STATUS_IN_PROGRESS.equals(t.getStatus()));
            if (anyInProgress) {
                updateWaveStatus(waveId, STATUS_IN_PROGRESS);
            }
        }
    }

    /**
     * Get wave by ID for checking status (lightweight query).
     */
    public String getWaveStatus(Long waveId) throws Exception {
        String sql = "SELECT status FROM pick_wave WHERE wave_id = ?";
        try (
        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }
}
