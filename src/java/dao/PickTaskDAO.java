package dao;

import context.DBContext;
import dto.PickTaskDTO;
import dto.PickTaskLineDTO;
import dto.SlotQtyDTO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PickTaskDAO extends DBContext {

    private static final String SELECT_TASK_HEAD = """
                SELECT pt.pick_task_id, pt.wave_id, COALESCE(pt.gdn_id, pw.gdn_id) AS gdn_id,
                    gdn.gdn_number, so.so_number, so.so_id,
                    pt.assigned_to, u1.full_name AS assigned_to_name,
                    pt.assigned_by, u2.full_name AS assigned_by_name,
                    pt.status, pt.assigned_at, pt.started_at, pt.completed_at
                FROM pick_task pt
                LEFT JOIN pick_wave pw ON pt.wave_id = pw.wave_id
                JOIN goods_delivery_note gdn ON gdn.gdn_id = COALESCE(pt.gdn_id, pw.gdn_id)
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN `user` u1 ON pt.assigned_to = u1.user_id
                LEFT JOIN `user` u2 ON pt.assigned_by = u2.user_id
            """;

    /**
     * Get pick tasks assigned to a user (for "My Tasks").
     */
    public List<PickTaskDTO> getMyPickTasks(Long userId, String status, int limit, int offset) throws Exception {
        StringBuilder sql = new StringBuilder(SELECT_TASK_HEAD);
        sql.append(" WHERE pt.assigned_to = ?");
        if (status != null && !status.isBlank()) {
            sql.append(" AND pt.status = ?");
        }
        sql.append(" ORDER BY pt.assigned_at DESC LIMIT ? OFFSET ?");

        List<PickTaskDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            ps.setLong(paramIndex++, userId);
            if (status != null && !status.isBlank()) {
                ps.setString(paramIndex++, status);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex++, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PickTaskDTO dto = mapTaskFromRs(rs);
                    dto.setLines(getPickTaskLines(dto.getPickTaskId()));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public int countMyPickTasks(Long userId, String status) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM pick_task pt WHERE pt.assigned_to = ?");
        if (status != null && !status.isBlank()) {
            sql.append(" AND pt.status = ?");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            ps.setLong(paramIndex++, userId);
            if (status != null && !status.isBlank()) {
                ps.setString(paramIndex++, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Get pick task by ID.
     */
    public PickTaskDTO getPickTaskById(Long pickTaskId) throws Exception {
        String sql = SELECT_TASK_HEAD + " WHERE pt.pick_task_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pickTaskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PickTaskDTO dto = mapTaskFromRs(rs);
                    dto.setLines(getPickTaskLines(pickTaskId));
                    return dto;
                }
            }
        }
        return null;
    }

    private static PickTaskDTO mapTaskFromRs(ResultSet rs) throws SQLException {
        PickTaskDTO dto = new PickTaskDTO();
        dto.setPickTaskId(rs.getLong("pick_task_id"));
        dto.setWaveId(rs.getObject("wave_id") != null ? rs.getLong("wave_id") : null);
        dto.setGdnId(rs.getLong("gdn_id"));
        dto.setGdnNumber(rs.getString("gdn_number"));
        dto.setSoNumber(rs.getString("so_number"));
        dto.setSoId(rs.getObject("so_id") != null ? rs.getLong("so_id") : null);
        dto.setAssignedTo(rs.getObject("assigned_to") != null ? rs.getLong("assigned_to") : null);
        dto.setAssignedToName(rs.getString("assigned_to_name"));
        dto.setAssignedBy(rs.getObject("assigned_by") != null ? rs.getLong("assigned_by") : null);
        dto.setAssignedByName(rs.getString("assigned_by_name"));
        dto.setStatus(rs.getString("status"));
        Timestamp t = rs.getTimestamp("assigned_at");
        if (t != null) dto.setAssignedAt(t.toLocalDateTime());
        t = rs.getTimestamp("started_at");
        if (t != null) dto.setStartedAt(t.toLocalDateTime());
        t = rs.getTimestamp("completed_at");
        if (t != null) dto.setCompletedAt(t.toLocalDateTime());
        return dto;
    }

    /**
     * Get pick task lines (with from_slot_id, qty_to_pick, pick_status).
     */
    public List<PickTaskLineDTO> getPickTaskLines(Long pickTaskId) throws Exception {
        String sql = """
                SELECT ptl.pick_task_line_id, ptl.pick_task_id, ptl.gdn_line_id, ptl.from_slot_id,
                    s.code AS slot_code, z.code AS zone_code,
                    COALESCE(ptl.variant_id, gdl.variant_id) AS variant_id,
                    pv.variant_sku, p.name AS product_name, pv.color, pv.size,
                    COALESCE(ptl.qty_to_pick, ptl.qty_required, 0) AS qty_to_pick,
                    ptl.qty_picked, COALESCE(ptl.pick_status, 'PENDING') AS pick_status, ptl.note
                FROM pick_task_line ptl
                JOIN goods_delivery_line gdl ON gdl.gdn_line_id = ptl.gdn_line_id
                JOIN product_variant pv ON pv.variant_id = COALESCE(ptl.variant_id, gdl.variant_id)
                JOIN product p ON p.product_id = pv.product_id
                LEFT JOIN slot s ON s.slot_id = ptl.from_slot_id
                LEFT JOIN zone z ON z.zone_id = s.zone_id
                WHERE ptl.pick_task_id = ?
                ORDER BY ptl.pick_task_line_id
                """;
        List<PickTaskLineDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pickTaskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PickTaskLineDTO line = new PickTaskLineDTO();
                    line.setPickTaskLineId(rs.getLong("pick_task_line_id"));
                    line.setPickTaskId(rs.getLong("pick_task_id"));
                    line.setGdnLineId(rs.getLong("gdn_line_id"));
                    line.setFromSlotId(rs.getObject("from_slot_id") != null ? rs.getLong("from_slot_id") : null);
                    line.setSlotCode(rs.getString("slot_code"));
                    line.setZoneCode(rs.getString("zone_code"));
                    line.setVariantId(rs.getLong("variant_id"));
                    line.setVariantSku(rs.getString("variant_sku"));
                    line.setProductName(rs.getString("product_name"));
                    line.setColor(rs.getString("color"));
                    line.setSize(rs.getString("size"));
                    line.setQtyToPick(rs.getBigDecimal("qty_to_pick"));
                    line.setQtyPicked(rs.getBigDecimal("qty_picked") != null ? rs.getBigDecimal("qty_picked") : BigDecimal.ZERO);
                    line.setPickStatus(rs.getString("pick_status"));
                    line.setNote(rs.getString("note"));
                    list.add(line);
                }
            }
        }
        return list;
    }

    /**
     * Create pick tasks from wave: group GDN lines by zone/slot, allocate from_slot_id from inventory.
     *
     * @return true if tasks are created successfully; false if there is at least one GDN line
     *         that cannot be fully allocated to any slot (insufficient stock).
     */
    public boolean createTasksFromWave(Long waveId) throws Exception {
        PickWaveDAO waveDao = new PickWaveDAO();
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        InventoryBalanceDAO invDao = new InventoryBalanceDAO();

        dto.PickWaveDTO wave = waveDao.getWaveById(waveId);
        if (wave == null) throw new SQLException("Wave not found: " + waveId);

        dto.GDNDetailDTO gdn = gdnDao.getGDNDetailById(wave.getGdnId());
        if (gdn == null || gdn.getWarehouseId() == null) throw new SQLException("GDN or warehouse not found");

        Long warehouseId = gdn.getWarehouseId();
        List<dto.GDNLineDTO> gdnLines = gdn.getLines();
        if (gdnLines == null || gdnLines.isEmpty()) return false;

        // Allocate (gdn_line_id, variant_id, qty_to_pick, from_slot_id, zone_id) per line
        List<AllocLine> allocs = new ArrayList<>();
        boolean hasInsufficientStock = false;
        for (dto.GDNLineDTO line : gdnLines) {
            BigDecimal qtyNeed = line.getQtyRequired() != null ? line.getQtyRequired() : BigDecimal.ZERO;
            if (qtyNeed.compareTo(BigDecimal.ZERO) <= 0) continue;

            List<SlotQtyDTO> slots = invDao.getAvailableSlotsForVariant(warehouseId, line.getVariantId(), qtyNeed);
            if (slots.isEmpty()) {
                hasInsufficientStock = true;
                continue;
            }
            BigDecimal remaining = qtyNeed;
            for (SlotQtyDTO slot : slots) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal take = slot.getQtyAvailable().min(remaining);
                if (take.compareTo(BigDecimal.ZERO) <= 0) continue;
                allocs.add(new AllocLine(line.getGdnLineId(), line.getVariantId(), take, slot.getSlotId(), slot.getZoneId()));
                remaining = remaining.subtract(take);
            }
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                hasInsufficientStock = true;
            }
        }

        // If any line cannot be fully allocated to slots, do not create tasks
        if (allocs.isEmpty() || hasInsufficientStock) {
            return false;
        }

        // Group by zone_id (null zone -> one group)
        Map<Long, List<AllocLine>> byZone = new LinkedHashMap<>();
        for (AllocLine a : allocs) {
            Long zid = a.zoneId != null ? a.zoneId : -1L;
            byZone.computeIfAbsent(zid, k -> new ArrayList<>()).add(a);
        }

        String sqlTask = """
                INSERT INTO pick_task (wave_id, gdn_id, assigned_to, assigned_by, status, assigned_at)
                VALUES (?, ?, NULL, NULL, 'CREATED', NULL)
                """;
        String sqlLine = """
                INSERT INTO pick_task_line (pick_task_id, gdn_line_id, from_slot_id, variant_id, qty_required, qty_to_pick, qty_picked, pick_status)
                VALUES (?, ?, ?, ?, ?, ?, 0, 'PENDING')
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long gdnId = wave.getGdnId();
                for (List<AllocLine> zoneLines : byZone.values()) {
                    try (PreparedStatement psTask = conn.prepareStatement(sqlTask, Statement.RETURN_GENERATED_KEYS)) {
                        psTask.setLong(1, waveId);
                        psTask.setLong(2, gdnId);
                        psTask.executeUpdate();
                        long pickTaskId;
                        try (ResultSet rs = psTask.getGeneratedKeys()) {
                            rs.next();
                            pickTaskId = rs.getLong(1);
                        }
                        try (PreparedStatement psLine = conn.prepareStatement(sqlLine)) {
                            for (AllocLine a : zoneLines) {
                                psLine.setLong(1, pickTaskId);
                                psLine.setLong(2, a.gdnLineId);
                                if (a.fromSlotId != null) {
                                    psLine.setLong(3, a.fromSlotId);
                                } else {
                                    psLine.setNull(3, Types.BIGINT);
                                }
                                psLine.setLong(4, a.variantId);
                                psLine.setBigDecimal(5, a.qtyToPick);
                                psLine.setBigDecimal(6, a.qtyToPick);
                                psLine.executeUpdate();
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return true;
    }

    private static class AllocLine {
        long gdnLineId;
        long variantId;
        BigDecimal qtyToPick;
        Long fromSlotId;
        Long zoneId;

        AllocLine(long gdnLineId, long variantId, BigDecimal qtyToPick, Long fromSlotId, Long zoneId) {
            this.gdnLineId = gdnLineId;
            this.variantId = variantId;
            this.qtyToPick = qtyToPick;
            this.fromSlotId = fromSlotId;
            this.zoneId = zoneId;
        }
    }

    public void assignTask(Long pickTaskId, Long assignedTo, Long assignedBy) throws Exception {
        String sql = "UPDATE pick_task SET assigned_to = ?, assigned_by = ?, assigned_at = NOW(), status = 'ASSIGNED' WHERE pick_task_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assignedTo);
            if (assignedBy != null) {
                ps.setLong(2, assignedBy);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setLong(3, pickTaskId);
            ps.executeUpdate();
        }
    }

    public void startTask(Long pickTaskId) throws Exception {
        String sql = "UPDATE pick_task SET status = 'IN_PROGRESS', started_at = NOW() WHERE pick_task_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pickTaskId);
            ps.executeUpdate();
        }
    }

    /**
     * Complete pick task: update line qty_picked/pick_status, GDN line qty_picked, inventory balance.
     */
    public void completeTask(Long pickTaskId, List<PickTaskLineDTO> lines) throws Exception {
        String sqlTask = "UPDATE pick_task SET status = 'COMPLETED', completed_at = NOW() WHERE pick_task_id = ?";
        String sqlLine = "UPDATE pick_task_line SET qty_picked = ?, pick_status = ? WHERE pick_task_line_id = ?";
        String sqlGDNLine = "UPDATE goods_delivery_line SET qty_picked = COALESCE(qty_picked, 0) + ? WHERE gdn_line_id = ?";
        String sqlInv = "UPDATE inventory_balance SET qty_available = qty_available - ?, qty_on_hand = qty_on_hand - ?, updated_at = NOW() WHERE variant_id = ? AND slot_id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psTask = conn.prepareStatement(sqlTask);
                 PreparedStatement psLine = conn.prepareStatement(sqlLine);
                 PreparedStatement psGDNLine = conn.prepareStatement(sqlGDNLine);
                 PreparedStatement psInv = conn.prepareStatement(sqlInv)) {

                psTask.setLong(1, pickTaskId);
                psTask.executeUpdate();

                for (PickTaskLineDTO line : lines) {
                    BigDecimal qty = line.getQtyPicked() != null ? line.getQtyPicked() : BigDecimal.ZERO;
                    String pstatus = line.getPickStatus() != null ? line.getPickStatus() : "DONE";

                    psLine.setBigDecimal(1, qty);
                    psLine.setString(2, pstatus);
                    psLine.setLong(3, line.getPickTaskLineId());
                    psLine.executeUpdate();

                    psGDNLine.setBigDecimal(1, qty);
                    psGDNLine.setLong(2, line.getGdnLineId());
                    psGDNLine.executeUpdate();

                    if (line.getFromSlotId() != null && qty.compareTo(BigDecimal.ZERO) > 0) {
                        psInv.setBigDecimal(1, qty);
                        psInv.setBigDecimal(2, qty);
                        psInv.setLong(3, line.getVariantId());
                        psInv.setLong(4, line.getFromSlotId());
                        psInv.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Get tasks by wave (for assign screen).
     */
    public List<PickTaskDTO> getTasksByWaveId(Long waveId) throws Exception {
        String sql = SELECT_TASK_HEAD + " WHERE pt.wave_id = ? ORDER BY pt.pick_task_id";
        List<PickTaskDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, waveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PickTaskDTO dto = mapTaskFromRs(rs);
                    dto.setLines(getPickTaskLines(dto.getPickTaskId()));
                    list.add(dto);
                }
            }
        }
        return list;
    }
}
