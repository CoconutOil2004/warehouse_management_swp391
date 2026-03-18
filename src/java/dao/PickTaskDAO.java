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

  public static final String STATUS_CREATED = "CREATED";
  public static final String STATUS_PENDING = "PENDING";
  public static final String STATUS_ASSIGNED = "ASSIGNED";
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_COMPLETED = "COMPLETED";
  public static final String STATUS_CANCELLED = "CANCELLED";

  private static final String SELECT_TASK_HEAD = """
        SELECT pt.pick_task_id, pt.wave_id, pt.gdn_id,
            gdn.gdn_number, so.so_number, so.so_id,
            pt.status, pt.started_at, pt.completed_at,
            ptl.assigned_to, u.full_name AS assigned_to_name, ptl.assigned_at
        FROM pick_task pt
        LEFT JOIN pick_wave pw ON pt.wave_id = pw.wave_id
        LEFT JOIN goods_delivery_note gdn ON gdn.gdn_id = COALESCE(pt.gdn_id,
            (SELECT pwg.gdn_id FROM pick_wave_gdn pwg WHERE pwg.wave_id = pt.wave_id LIMIT 1))
        LEFT JOIN sales_order so ON gdn.so_id = so.so_id
        LEFT JOIN pick_task_line ptl ON ptl.pick_task_id = pt.pick_task_id AND ptl.assigned_to IS NOT NULL
        LEFT JOIN `user` u ON ptl.assigned_to = u.user_id
    """;

  /**
   * Get pick tasks assigned to a user (for "My Tasks" - via line assignment).
   */
  public List<PickTaskDTO> getMyPickTasks(
    Long userId,
    String status,
    int limit,
    int offset
  ) throws Exception {
    StringBuilder sql = new StringBuilder(
      """
      SELECT DISTINCT pt.pick_task_id, pt.wave_id, pt.gdn_id, pt.status, pt.started_at, pt.completed_at,
             gdn.gdn_number, so.so_number, so.so_id,
             MAX(ptl.assigned_at) AS assigned_at
      FROM pick_task pt
      JOIN pick_task_line ptl ON pt.pick_task_id = ptl.pick_task_id
      LEFT JOIN pick_wave pw ON pt.wave_id = pw.wave_id
      LEFT JOIN pick_wave_gdn pwg ON pw.wave_id = pwg.wave_id
      LEFT JOIN goods_delivery_note gdn ON gdn.gdn_id = COALESCE(pt.gdn_id, pwg.gdn_id)
      LEFT JOIN sales_order so ON gdn.so_id = so.so_id
      WHERE ptl.assigned_to = ?
      """
    );
    if (status != null && !status.isBlank()) {
      sql.append(" AND pt.status = ?");
    }
    sql.append(
      " GROUP BY pt.pick_task_id, pt.wave_id, pt.gdn_id, pt.status, pt.started_at, pt.completed_at, gdn.gdn_number, so.so_number, so.so_id"
    );
    sql.append(" ORDER BY MAX(ptl.assigned_at) DESC LIMIT ? OFFSET ?");

    List<PickTaskDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql.toString())
    ) {
      int paramIndex = 1;
      ps.setLong(paramIndex++, userId);
      if (status != null && !status.isBlank()) {
        ps.setString(paramIndex++, status);
      }
      ps.setInt(paramIndex++, limit);
      ps.setInt(paramIndex++, offset);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          PickTaskDTO dto = new PickTaskDTO();
          dto.setPickTaskId(rs.getLong("pick_task_id"));
          dto.setWaveId(
            rs.getObject("wave_id") != null ? rs.getLong("wave_id") : null
          );
          dto.setGdnId(
            rs.getObject("gdn_id") != null ? rs.getLong("gdn_id") : null
          );
          dto.setGdnNumber(rs.getString("gdn_number"));
          dto.setSoNumber(rs.getString("so_number"));
          dto.setSoId(
            rs.getObject("so_id") != null ? rs.getLong("so_id") : null
          );
          dto.setStatus(rs.getString("status"));
          Timestamp t = rs.getTimestamp("started_at");
          if (t != null) dto.setStartedAt(t.toLocalDateTime());
          t = rs.getTimestamp("completed_at");
          if (t != null) dto.setCompletedAt(t.toLocalDateTime());
          dto.setLines(getPickTaskLines(dto.getPickTaskId()));
          list.add(dto);
        }
      }
    }
    return list;
  }

  public int countMyPickTasks(Long userId, String status) throws Exception {
    StringBuilder sql = new StringBuilder(
      """
      SELECT COUNT(DISTINCT pt.pick_task_id)
      FROM pick_task pt
      JOIN pick_task_line ptl ON pt.pick_task_id = ptl.pick_task_id
      WHERE ptl.assigned_to = ?
      """
    );
    if (status != null && !status.isBlank()) {
      sql.append(" AND pt.status = ?");
    }

    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql.toString())
    ) {
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
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
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
    dto.setWaveId(
      rs.getObject("wave_id") != null ? rs.getLong("wave_id") : null
    );
    dto.setGdnId(rs.getObject("gdn_id") != null ? rs.getLong("gdn_id") : null);
    dto.setGdnNumber(rs.getString("gdn_number"));
    dto.setSoNumber(rs.getString("so_number"));
    dto.setSoId(rs.getObject("so_id") != null ? rs.getLong("so_id") : null);
    dto.setStatus(rs.getString("status"));
    Timestamp t = rs.getTimestamp("started_at");
    if (t != null) dto.setStartedAt(t.toLocalDateTime());
    t = rs.getTimestamp("completed_at");
    if (t != null) dto.setCompletedAt(t.toLocalDateTime());
    dto.setAssignedTo(
      rs.getObject("assigned_to") != null ? rs.getLong("assigned_to") : null
    );
    dto.setAssignedToName(rs.getString("assigned_to_name"));
    t = rs.getTimestamp("assigned_at");
    if (t != null) dto.setAssignedAt(t.toLocalDateTime());
    return dto;
  }

  /**
   * Get pick task lines (with from_slot_id, qty_to_pick, pick_status,
   * assignment info).
   */
  public List<PickTaskLineDTO> getPickTaskLines(Long pickTaskId)
    throws Exception {
    String sql = """
      SELECT ptl.pick_task_line_id, ptl.pick_task_id, ptl.gdn_line_id, ptl.from_slot_id,
          s.code AS slot_code, z.code AS zone_code,
          COALESCE(ptl.variant_id, gdl.variant_id) AS variant_id,
          pv.variant_sku, p.name AS product_name, pv.color, pv.size,
          ptl.qty_required, COALESCE(ptl.qty_to_pick, ptl.qty_required, 0) AS qty_to_pick,
          ptl.qty_picked, COALESCE(ptl.pick_status, 'PENDING') AS pick_status,
          ptl.assigned_to, u1.full_name AS assigned_to_name,
          ptl.assigned_by, u2.full_name AS assigned_by_name,
          ptl.assigned_at, ptl.completed_at, ptl.note
      FROM pick_task_line ptl
      JOIN goods_delivery_line gdl ON gdl.gdn_line_id = ptl.gdn_line_id
      JOIN product_variant pv ON pv.variant_id = COALESCE(ptl.variant_id, gdl.variant_id)
      JOIN product p ON p.product_id = pv.product_id
      LEFT JOIN slot s ON s.slot_id = ptl.from_slot_id
      LEFT JOIN zone z ON z.zone_id = s.zone_id
      LEFT JOIN `user` u1 ON ptl.assigned_to = u1.user_id
      LEFT JOIN `user` u2 ON ptl.assigned_by = u2.user_id
      WHERE ptl.pick_task_id = ?
      ORDER BY z.code ASC, s.code ASC, ptl.pick_task_line_id ASC
      """;
    List<PickTaskLineDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, pickTaskId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          PickTaskLineDTO line = new PickTaskLineDTO();
          line.setPickTaskLineId(rs.getLong("pick_task_line_id"));
          line.setPickTaskId(rs.getLong("pick_task_id"));
          line.setGdnLineId(rs.getLong("gdn_line_id"));
          line.setFromSlotId(
            rs.getObject("from_slot_id") != null
              ? rs.getLong("from_slot_id")
              : null
          );
          line.setSlotCode(rs.getString("slot_code"));
          line.setZoneCode(rs.getString("zone_code"));
          line.setVariantId(rs.getLong("variant_id"));
          line.setVariantSku(rs.getString("variant_sku"));
          line.setProductName(rs.getString("product_name"));
          line.setColor(rs.getString("color"));
          line.setSize(rs.getString("size"));
          line.setQtyRequired(rs.getBigDecimal("qty_required"));
          line.setQtyToPick(rs.getBigDecimal("qty_to_pick"));
          line.setQtyPicked(
            rs.getBigDecimal("qty_picked") != null
              ? rs.getBigDecimal("qty_picked")
              : BigDecimal.ZERO
          );
          line.setPickStatus(rs.getString("pick_status"));
          line.setAssignedTo(
            rs.getObject("assigned_to") != null
              ? rs.getLong("assigned_to")
              : null
          );
          line.setAssignedToName(rs.getString("assigned_to_name"));
          line.setAssignedBy(
            rs.getObject("assigned_by") != null
              ? rs.getLong("assigned_by")
              : null
          );
          line.setAssignedByName(rs.getString("assigned_by_name"));
          Timestamp t = rs.getTimestamp("assigned_at");
          if (t != null) line.setAssignedAt(t.toLocalDateTime());
          t = rs.getTimestamp("completed_at");
          if (t != null) line.setCompletedAt(t.toLocalDateTime());
          line.setNote(rs.getString("note"));
          list.add(line);
        }
      }
    }
    return list;
  }

  /**
   * Get all pick tasks related to a specific GDN (directly or via wave).
   */
  public List<PickTaskDTO> getTasksByGdnId(Long gdnId) throws Exception {
    String sql =
      "SELECT pt.pick_task_id, pt.wave_id, pt.gdn_id, gdn.gdn_number, so.so_number, so.so_id, " +
      "pt.status, pt.started_at, pt.completed_at, " +
      "ptl.assigned_to, u.full_name AS assigned_to_name " +
      "FROM pick_task pt " +
      "LEFT JOIN pick_wave pw ON pt.wave_id = pw.wave_id " +
      "LEFT JOIN pick_wave_gdn pwg ON pw.wave_id = pwg.wave_id " +
      "LEFT JOIN goods_delivery_note gdn ON gdn.gdn_id = COALESCE(pt.gdn_id, pwg.gdn_id) " +
      "LEFT JOIN sales_order so ON gdn.so_id = so.so_id " +
      "LEFT JOIN pick_task_line ptl ON ptl.pick_task_id = pt.pick_task_id AND ptl.assigned_to IS NOT NULL " +
      "LEFT JOIN `user` u ON ptl.assigned_to = u.user_id " +
      "WHERE COALESCE(pt.gdn_id, pwg.gdn_id) = ? ORDER BY pt.pick_task_id";
    List<PickTaskDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, gdnId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          PickTaskDTO dto = mapTaskFromRs(rs);
          list.add(dto);
        }
      }
    }
    return list;
  }

  /**
   * Create pick tasks from wave: group GDN lines by zone/slot, allocate
   * from_slot_id from inventory. Supports multiple GDNs in a single wave.
   *
   * @param waveId
   * @return true if tasks are created successfully; false if there is at
   * least one GDN line that cannot be fully allocated to any slot
   * (insufficient stock).
   * @throws java.lang.Exception
   */
  public boolean createTasksFromWave(Long waveId) throws Exception {
    PickWaveDAO waveDao = new PickWaveDAO();
    GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
    InventoryBalanceDAO invDao = new InventoryBalanceDAO();

    dto.PickWaveDTO wave = waveDao.getWaveById(waveId);
    if (wave == null) throw new SQLException("Wave not found: " + waveId);

    // Get all GDNs in this wave
    List<dto.GDNListDTO> gdns = wave.getGdns();
    if (gdns == null || gdns.isEmpty()) {
      return false;
    }

    // Get warehouse ID from first GDN (all GDNs should have same warehouse)
    Long warehouseId = null;
    for (dto.GDNListDTO gdnSummary : gdns) {
      dto.GDNDetailDTO gdnCheck = gdnDao.getGDNDetailById(
        gdnSummary.getGdnId()
      );
      if (gdnCheck != null && gdnCheck.getWarehouseId() != null) {
        warehouseId = gdnCheck.getWarehouseId();
        break;
      }
    }
    if (warehouseId == null) {
      throw new SQLException("No warehouse found for GDNs in wave");
    }

    // Collect all GDN lines from all GDNs
    List<AllocLine> allocs = new ArrayList<>();
    boolean hasInsufficientStock = false;

    for (dto.GDNListDTO gdnSummary : gdns) {
      dto.GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnSummary.getGdnId());
      if (gdn == null || gdn.getLines() == null) {
        hasInsufficientStock = true;
        continue;
      }

      List<dto.GDNLineDTO> gdnLines = gdn.getLines();
      for (dto.GDNLineDTO line : gdnLines) {
        if (line.getGdnLineId() == null || line.getVariantId() == null) {
          hasInsufficientStock = true;
          continue;
        }
        BigDecimal qtyNeed =
          line.getQtyRequired() != null
            ? line.getQtyRequired()
            : BigDecimal.ZERO;
        if (qtyNeed.compareTo(BigDecimal.ZERO) <= 0) continue;

        List<SlotQtyDTO> slots = invDao.getAvailableSlotsForVariant(
          warehouseId,
          line.getVariantId(),
          qtyNeed
        );
        if (slots == null || slots.isEmpty()) {
          hasInsufficientStock = true;
          continue;
        }
        BigDecimal remaining = qtyNeed;
        for (SlotQtyDTO slot : slots) {
          if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
          BigDecimal avail =
            slot.getQtyAvailable() != null
              ? slot.getQtyAvailable()
              : BigDecimal.ZERO;
          BigDecimal take = avail.min(remaining);
          if (take.compareTo(BigDecimal.ZERO) <= 0) continue;
          Long slotId = slot.getSlotId();
          Long zoneId = slot.getZoneId();
          allocs.add(
            new AllocLine(
              line.getGdnLineId(),
              gdnSummary.getGdnId(),
              line.getVariantId(),
              take,
              slotId,
              zoneId
            )
          );
          remaining = remaining.subtract(take);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
          hasInsufficientStock = true;
        }
      }
    }

    // If any line cannot be fully allocated to slots, do not create tasks
    if (allocs.isEmpty() || hasInsufficientStock) {
      return false;
    }

    // Group by gdn_id (one task per GDN)
    Map<Long, List<AllocLine>> byGdn = new LinkedHashMap<>();
    for (AllocLine a : allocs) {
      byGdn.computeIfAbsent(a.gdnId, k -> new ArrayList<>()).add(a);
    }

    String sqlTask = """
      INSERT INTO pick_task (wave_id, gdn_id, status)
      VALUES (?, ?, 'CREATED')
      """;
    String sqlLine = """
      INSERT INTO pick_task_line (pick_task_id, gdn_line_id, from_slot_id, variant_id, qty_required, qty_to_pick, qty_picked, pick_status)
      VALUES (?, ?, ?, ?, ?, ?, 0, 'PENDING')
      """;

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);
      try {
        for (Map.Entry<Long, List<AllocLine>> entry : byGdn.entrySet()) {
          Long gdnId = entry.getKey();
          List<AllocLine> gdnLines = entry.getValue();
          try (
            PreparedStatement psTask = conn.prepareStatement(
              sqlTask,
              Statement.RETURN_GENERATED_KEYS
            )
          ) {
            psTask.setLong(1, waveId);
            psTask.setLong(2, gdnId);
            psTask.executeUpdate();
            long pickTaskId;
            try (ResultSet rs = psTask.getGeneratedKeys()) {
              rs.next();
              pickTaskId = rs.getLong(1);
            }
            try (PreparedStatement psLine = conn.prepareStatement(sqlLine)) {
              for (AllocLine a : gdnLines) {
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
    long gdnId;
    long variantId;
    BigDecimal qtyToPick;
    Long fromSlotId;
    Long zoneId;

    AllocLine(
      long gdnLineId,
      long gdnId,
      long variantId,
      BigDecimal qtyToPick,
      Long fromSlotId,
      Long zoneId
    ) {
      this.gdnLineId = gdnLineId;
      this.gdnId = gdnId;
      this.variantId = variantId;
      this.qtyToPick = qtyToPick;
      this.fromSlotId = fromSlotId;
      this.zoneId = zoneId;
    }
  }

  public void assignTask(Long pickTaskId, Long assignedTo, Long assignedBy)
    throws Exception {
    // Task-level assignment is deprecated, use line-level assignment instead
    // This method is kept for backward compatibility
    String sql =
      "UPDATE pick_task SET status = 'ASSIGNED' WHERE pick_task_id = ?";
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, pickTaskId);
      ps.executeUpdate();
    }
  }

  public void startTask(Long pickTaskId) throws Exception {
    String sql =
      "UPDATE pick_task SET status = 'IN_PROGRESS', started_at = NOW() WHERE pick_task_id = ?";
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, pickTaskId);
      ps.executeUpdate();
    }
  }

  /**
   * Complete pick task: update line qty_picked/pick_status, GDN line
   * qty_picked, inventory balance.
   */
  public void completeTask(
    Long pickTaskId,
    List<PickTaskLineDTO> lines,
    Long pickedBy
  ) throws Exception {
    String sqlTask =
      "UPDATE pick_task SET status = 'COMPLETED', completed_at = NOW() WHERE pick_task_id = ?";
    String sqlLine =
      "UPDATE pick_task_line SET qty_picked = ?, pick_status = ?, picked_by = ? WHERE pick_task_line_id = ?";
    String sqlGDNLine =
      "UPDATE goods_delivery_line SET qty_picked = COALESCE(qty_picked, 0) + ? WHERE gdn_line_id = ?";
    String sqlInv =
      "UPDATE inventory_balance SET qty_available = qty_available - ?, qty_on_hand = qty_on_hand - ?, updated_at = NOW() WHERE variant_id = ? AND slot_id = ?";
    String sqlTxn =
      "INSERT INTO inventory_txn (txn_type, warehouse_id, from_slot_id, to_slot_id, variant_id, condition, qty_delta, ref_doc_type, ref_doc_id, note, created_by, txn_at) VALUES (?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, NOW())";
    String sqlGetTaskInfo =
      "SELECT pt.wave_id, s.warehouse_id FROM pick_task pt LEFT JOIN pick_task_line ptl ON ptl.pick_task_id = pt.pick_task_id LEFT JOIN slot s ON s.slot_id = ptl.from_slot_id WHERE pt.pick_task_id = ? LIMIT 1";

    Long warehouseId = null;

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);
      try (
        PreparedStatement psTask = conn.prepareStatement(sqlTask);
        PreparedStatement psLine = conn.prepareStatement(sqlLine);
        PreparedStatement psGDNLine = conn.prepareStatement(sqlGDNLine);
        PreparedStatement psInv = conn.prepareStatement(sqlInv);
        PreparedStatement psTxn = conn.prepareStatement(sqlTxn);
        PreparedStatement psGetTaskInfo = conn.prepareStatement(sqlGetTaskInfo)
      ) {
        psGetTaskInfo.setLong(1, pickTaskId);
        try (ResultSet rs = psGetTaskInfo.executeQuery()) {
          if (rs.next()) {
            warehouseId =
              rs.getObject("warehouse_id") != null
                ? rs.getLong("warehouse_id")
                : null;
          }
        }

        psTask.setLong(1, pickTaskId);
        psTask.executeUpdate();

        for (PickTaskLineDTO line : lines) {
          BigDecimal qty =
            line.getQtyPicked() != null ? line.getQtyPicked() : BigDecimal.ZERO;
          BigDecimal qtyToPick =
            line.getQtyToPick() != null ? line.getQtyToPick() : BigDecimal.ZERO;
          String pstatus;
          if (
            qty.compareTo(BigDecimal.ZERO) > 0 && qty.compareTo(qtyToPick) < 0
          ) {
            pstatus = "PARTIAL";
          } else {
            pstatus = "DONE";
          }

          psLine.setBigDecimal(1, qty);
          psLine.setString(2, pstatus);
          psLine.setObject(3, pickedBy);
          psLine.setLong(4, line.getPickTaskLineId());
          psLine.executeUpdate();

          psGDNLine.setBigDecimal(1, qty);
          psGDNLine.setLong(2, line.getGdnLineId());
          psGDNLine.executeUpdate();

          if (
            line.getFromSlotId() != null && qty.compareTo(BigDecimal.ZERO) > 0
          ) {
            psInv.setBigDecimal(1, qty);
            psInv.setBigDecimal(2, qty);
            psInv.setLong(3, line.getVariantId());
            psInv.setLong(4, line.getFromSlotId());
            psInv.executeUpdate();

            psTxn.setString(1, "PICK");
            psTxn.setObject(2, warehouseId);
            psTxn.setLong(3, line.getFromSlotId());
            psTxn.setLong(4, line.getVariantId());
            psTxn.setString(5, "GOOD");
            psTxn.setBigDecimal(6, qty.negate());
            psTxn.setString(7, "PICK_TASK");
            psTxn.setLong(8, pickTaskId);
            psTxn.setString(9, "Pick from slot");
            psTxn.setObject(10, pickedBy);
            psTxn.executeUpdate();
          }
        }

        conn.commit();

        checkAndUpdateGDNStatus(pickTaskId);
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  private void checkAndUpdateGDNStatus(Long pickTaskId) throws Exception {
    String sqlGetWaveAndGdns = """
      SELECT pt.wave_id, pwg.gdn_id
      FROM pick_task pt
      LEFT JOIN pick_wave pw ON pt.wave_id = pw.wave_id
      LEFT JOIN pick_wave_gdn pwg ON pw.wave_id = pwg.wave_id
      WHERE pt.pick_task_id = ?
      """;

    List<Long> gdnIds = new ArrayList<>();

    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sqlGetWaveAndGdns)
    ) {
      ps.setLong(1, pickTaskId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Long waveId =
            rs.getObject("wave_id") != null ? rs.getLong("wave_id") : null;
          Long gdnId =
            rs.getObject("gdn_id") != null ? rs.getLong("gdn_id") : null;
          if (gdnId != null && !gdnIds.contains(gdnId)) {
            gdnIds.add(gdnId);
          }
        }
      }
    }

    if (gdnIds.isEmpty()) {
      return;
    }

    String sqlCheckPending = """
      SELECT COUNT(*) FROM pick_task ptl
      JOIN goods_delivery_line gdl ON ptl.gdn_line_id = gdl.gdn_line_id
      WHERE gdl.gdn_id IN ?
      AND ptl.status != 'COMPLETED'
      """;

    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sqlCheckPending)
    ) {
      int idx = 1;
      for (Long gdnId : gdnIds) {
        ps.setLong(idx++, gdnId);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next() && rs.getInt(1) == 0) {
          GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
          for (Long gdnId : gdnIds) {
            gdnDao.updateGDNStatus(gdnId, "PICKED");
          }
        }
      }
    }
  }

  /**
   * Check if all tasks in a wave are completed.
   *
   * @param waveId
   * @return
   * @throws java.lang.Exception
   */
  public boolean isWaveComplete(Long waveId) throws Exception {
    if (waveId == null) return false;
    String sql =
      "SELECT COUNT(*) FROM pick_task WHERE wave_id = ? AND status != 'COMPLETED'";
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, waveId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) == 0;
        }
      }
    }
    return false;
  }

  /**
   * Get tasks by wave (for assign screen).
   *
   * @param waveId
   * @return
   * @throws java.lang.Exception
   */
  public List<PickTaskDTO> getTasksByWaveId(Long waveId) throws Exception {
    String sql =
      SELECT_TASK_HEAD + " WHERE pt.wave_id = ? ORDER BY pt.pick_task_id";
    List<PickTaskDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, waveId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          PickTaskDTO dto = mapTaskFromRs(rs);
          dto.setLines(getPickTaskLines(dto.getPickTaskId()));
          dto.setTotalLines(dto.getLines() != null ? dto.getLines().size() : 0);
          list.add(dto);
        }
      }
    }
    return list;
  }

  /**
   * Get workload of all warehouse staff. Returns active tasks and active pick
   * lines count for each staff member.
   *
   * @return List of UserWorkloadDTO
   * @throws java.lang.Exception
   */
  public List<dto.UserWorkloadDTO> getStaffWorkload() throws Exception {
    String sql = """
      SELECT
          u.user_id,
          u.full_name,
          COUNT(DISTINCT pt.pick_task_id) AS active_tasks,
          COUNT(ptl.pick_task_line_id) AS active_lines
      FROM `user` u
      LEFT JOIN pick_task_line ptl ON ptl.assigned_to = u.user_id
          AND ptl.pick_status IN ('PENDING', 'PICKED')
      LEFT JOIN pick_task pt ON ptl.pick_task_id = pt.pick_task_id
      WHERE u.status = 'ACTIVE'
      GROUP BY u.user_id, u.full_name
      ORDER BY active_tasks ASC, active_lines ASC
      """;

    List<dto.UserWorkloadDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          dto.UserWorkloadDTO dto = new dto.UserWorkloadDTO(
            rs.getLong("user_id"),
            rs.getString("full_name"),
            rs.getInt("active_tasks"),
            rs.getInt("active_lines")
          );
          list.add(dto);
        }
      }
    }
    return list;
  }

  //    /**
  //     * Count active pick lines for a user (used in load balancing).
  //     *
  //     * @param userId The user ID
  //     * @return Number of active pick lines
  //     * @throws java.lang.Exception
  //     */
  //    public int countActivePickLines(Long userId) throws Exception {
  //        String sql = """
  //      SELECT COUNT(ptl.pick_task_line_id) AS active_lines
  //      FROM pick_task_line ptl
  //      JOIN pick_task pt ON ptl.pick_task_id = pt.pick_task_id
  //      WHERE ptl.assigned_to = ?
  //        AND ptl.pick_status IN ('PENDING', 'PICKED')
  //      """;
  //
  //        try (
  //        Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
  //            ps.setLong(1, userId);
  //            try (ResultSet rs = ps.executeQuery()) {
  //                if (rs.next()) {
  //                    return rs.getInt("active_lines");
  //                }
  //            }
  //        }
  //        return list;
  //    }
  /**
   * Count active pick lines for a user (used in load balancing).
   *
   * @param userId The user ID
   * @return Number of active pick lines
   * @throws java.lang.Exception
   */
  public int countActivePickLines(Long userId) throws Exception {
    String sql = """
      SELECT COUNT(ptl.pick_task_line_id) AS active_lines
      FROM pick_task_line ptl
      JOIN pick_task pt ON ptl.pick_task_id = pt.pick_task_id
      WHERE ptl.assigned_to = ?
        AND ptl.pick_status IN ('PENDING', 'PICKED')
      """;

    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("active_lines");
        }
      }
    }
    return 0;
  }

  /**
   * Get suggested assignments for tasks in a wave using load balancing
   * algorithm. Tasks with more lines are assigned first to staff with lowest
   * current workload.
   *
   * @param waveId The wave ID
   * @return List of TaskAssignmentSuggestionDTO
   * @throws java.lang.Exception
   */
  public List<dto.TaskAssignmentSuggestionDTO> getSuggestedAssignments(
    Long waveId
  ) throws Exception {
    // Get all unassigned lines in the wave
    String sqlTasks = """
      SELECT ptl.pick_task_line_id, pt.pick_task_id, COUNT(ptl2.pick_task_line_id) AS task_line_count
      FROM pick_task pt
      JOIN pick_task_line ptl ON ptl.pick_task_id = pt.pick_task_id
      LEFT JOIN pick_task_line ptl2 ON ptl2.pick_task_id = pt.pick_task_id
      WHERE pt.wave_id = ?
        AND ptl.assigned_to IS NULL
      GROUP BY ptl.pick_task_line_id, pt.pick_task_id
      ORDER BY task_line_count DESC
      """;

    List<Map.Entry<Long, Integer>> tasksWithLines = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sqlTasks)
    ) {
      ps.setLong(1, waveId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          tasksWithLines.add(
            new java.util.AbstractMap.SimpleEntry<>(
              rs.getLong("pick_task_id"),
              rs.getInt("task_line_count")
            )
          );
        }
      }
    }

    // Get all warehouse staff with current workload
    List<dto.UserWorkloadDTO> staffList = getStaffWorkload();
    Map<Long, Integer> currentWorkload = new java.util.HashMap<>();
    Map<Long, String> staffNames = new java.util.HashMap<>();
    for (dto.UserWorkloadDTO staff : staffList) {
      currentWorkload.put(staff.getUserId(), staff.getActiveLines());
      staffNames.put(staff.getUserId(), staff.getFullName());
    }

    // Assign tasks using greedy algorithm
    List<dto.TaskAssignmentSuggestionDTO> suggestions = new ArrayList<>();
    for (Map.Entry<Long, Integer> task : tasksWithLines) {
      // Find staff with lowest current workload
      Long leastBusyStaffId = staffList
        .stream()
        .min(
          java.util.Comparator.comparingInt(s ->
            currentWorkload.get(s.getUserId())
          )
        )
        .map(dto.UserWorkloadDTO::getUserId)
        .orElse(staffList.get(0).getUserId());

      Integer taskLineCount = task.getValue();
      suggestions.add(
        new dto.TaskAssignmentSuggestionDTO(
          task.getKey(),
          leastBusyStaffId,
          staffNames.get(leastBusyStaffId),
          taskLineCount,
          currentWorkload.get(leastBusyStaffId),
          "Lowest current workload (" +
            currentWorkload.get(leastBusyStaffId) +
            " lines)"
        )
      );

      // Update workload for next iteration
      currentWorkload.put(
        leastBusyStaffId,
        currentWorkload.get(leastBusyStaffId) + taskLineCount
      );
    }

    return suggestions;
  }

  /**
   * Batch assign multiple tasks to a single user.
   *
   * @param taskIds List of task IDs to assign
   * @param userId The user ID to assign tasks to
   * @param assignedBy The user ID who is making the assignment (optional)
   * @throws java.lang.Exception
   */
  public void batchAssignTasks(List<Long> taskIds, Long userId, Long assignedBy)
    throws Exception {
    String sql = """
      UPDATE pick_task
      SET assigned_to = ?, assigned_by = ?, assigned_at = NOW(), status = 'ASSIGNED'
      WHERE pick_task_id = ?
      """;

    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      conn.setAutoCommit(false);
      try {
        for (Long taskId : taskIds) {
          ps.setLong(1, userId);
          if (assignedBy != null) {
            ps.setLong(2, assignedBy);
          } else {
            ps.setNull(2, Types.BIGINT);
          }
          ps.setLong(3, taskId);
          ps.executeUpdate();
        }
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  /**
   * Auto-assign all unassigned tasks in a wave using load balancing
   * algorithm.
   *
   * @param waveId The wave ID
   * @param assignedBy The user ID who is making the assignment (optional)
   * @throws java.lang.Exception
   */
  public void autoAssignTasks(Long waveId, Long assignedBy) throws Exception {
    List<dto.TaskAssignmentSuggestionDTO> suggestions = getSuggestedAssignments(
      waveId
    );

    // Group suggestions by assigned user
    Map<Long, List<Long>> tasksByUser = new java.util.HashMap<>();
    for (dto.TaskAssignmentSuggestionDTO suggestion : suggestions) {
      tasksByUser
        .computeIfAbsent(suggestion.getSuggestedUserId(), k ->
          new ArrayList<>()
        )
        .add(suggestion.getPickTaskId());
    }

    // Batch assign for each user
    for (Map.Entry<Long, List<Long>> entry : tasksByUser.entrySet()) {
      batchAssignTasks(entry.getValue(), entry.getKey(), assignedBy);
    }
  }

  /**
   * Assign multiple lines to a warehouse staff member. Line-level assignment
   * (new feature).
   */
  public void assignLines(List<Long> lineIds, Long assignedTo, Long assignedBy)
    throws Exception {
    if (lineIds == null || lineIds.isEmpty()) {
      return;
    }

    String sql = """
      UPDATE pick_task_line
      SET assigned_to = ?, assigned_by = ?, assigned_at = NOW(), pick_status = 'PENDING'
      WHERE pick_task_line_id = ?
      """;

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        for (Long lineId : lineIds) {
          ps.setLong(1, assignedTo);
          if (assignedBy != null) {
            ps.setLong(2, assignedBy);
          } else {
            ps.setNull(2, Types.BIGINT);
          }
          ps.setLong(3, lineId);
          ps.executeUpdate();
        }
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  /**
   * Auto-assign unassigned lines in a wave using load balancing.
   */
  public void autoAssignLines(Long waveId, Long assignedBy) throws Exception {
    List<PickTaskLineDTO> unassignedLines = getUnassignedLinesByWave(waveId);
    if (unassignedLines.isEmpty()) {
      return;
    }

    List<dto.UserWorkloadDTO> staffList = getStaffWorkload();
    if (staffList.isEmpty()) {
      throw new SQLException("No warehouse staff available for assignment");
    }

    Map<Long, Integer> currentWorkload = new java.util.HashMap<>();
    Map<Long, String> staffNames = new java.util.HashMap<>();
    for (dto.UserWorkloadDTO staff : staffList) {
      currentWorkload.put(staff.getUserId(), staff.getActiveLines());
      staffNames.put(staff.getUserId(), staff.getFullName());
    }

    List<Long> linesToAssign = new ArrayList<>();
    for (PickTaskLineDTO line : unassignedLines) {
      Long leastBusyStaffId = staffList
        .stream()
        .min(
          java.util.Comparator.comparingInt(s ->
            currentWorkload.get(s.getUserId())
          )
        )
        .map(dto.UserWorkloadDTO::getUserId)
        .orElse(staffList.get(0).getUserId());

      linesToAssign.add(line.getPickTaskLineId());
      currentWorkload.put(
        leastBusyStaffId,
        currentWorkload.get(leastBusyStaffId) + 1
      );
    }

    for (Long staffId : currentWorkload.keySet()) {
      List<Long> staffLines = unassignedLines
        .stream()
        .filter(l -> {
          int idx = unassignedLines.indexOf(l);
          return (
            idx <
            currentWorkload.get(staffId) +
            (linesToAssign.size() / staffList.size())
          );
        })
        .map(PickTaskLineDTO::getPickTaskLineId)
        .toList();

      if (!staffLines.isEmpty()) {
        assignLines(staffLines, staffId, assignedBy);
      }
    }
  }

  /**
   * Get unassigned lines for a wave.
   */
  public List<PickTaskLineDTO> getUnassignedLinesByWave(Long waveId)
    throws Exception {
    String sql = """
      SELECT ptl.pick_task_line_id, ptl.pick_task_id, ptl.gdn_line_id, ptl.from_slot_id,
          s.code AS slot_code, z.code AS zone_code,
          COALESCE(ptl.variant_id, gdl.variant_id) AS variant_id,
          pv.variant_sku, p.name AS product_name, pv.color, pv.size,
          ptl.qty_required, COALESCE(ptl.qty_to_pick, ptl.qty_required, 0) AS qty_to_pick,
          ptl.qty_picked, COALESCE(ptl.pick_status, 'PENDING') AS pick_status
      FROM pick_task_line ptl
      JOIN pick_task pt ON ptl.pick_task_id = pt.pick_task_id
      JOIN goods_delivery_line gdl ON gdl.gdn_line_id = ptl.gdn_line_id
      JOIN product_variant pv ON pv.variant_id = COALESCE(ptl.variant_id, gdl.variant_id)
      JOIN product p ON p.product_id = pv.product_id
      LEFT JOIN slot s ON s.slot_id = ptl.from_slot_id
      LEFT JOIN zone z ON z.zone_id = s.zone_id
      WHERE pt.wave_id = ? AND ptl.assigned_to IS NULL
      ORDER BY z.code ASC, s.code ASC
      """;

    List<PickTaskLineDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, waveId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          PickTaskLineDTO line = new PickTaskLineDTO();
          line.setPickTaskLineId(rs.getLong("pick_task_line_id"));
          line.setPickTaskId(rs.getLong("pick_task_id"));
          line.setGdnLineId(rs.getLong("gdn_line_id"));
          line.setFromSlotId(
            rs.getObject("from_slot_id") != null
              ? rs.getLong("from_slot_id")
              : null
          );
          line.setSlotCode(rs.getString("slot_code"));
          line.setZoneCode(rs.getString("zone_code"));
          line.setVariantId(rs.getLong("variant_id"));
          line.setVariantSku(rs.getString("variant_sku"));
          line.setProductName(rs.getString("product_name"));
          line.setColor(rs.getString("color"));
          line.setSize(rs.getString("size"));
          line.setQtyRequired(rs.getBigDecimal("qty_required"));
          line.setQtyToPick(rs.getBigDecimal("qty_to_pick"));
          line.setQtyPicked(
            rs.getBigDecimal("qty_picked") != null
              ? rs.getBigDecimal("qty_picked")
              : BigDecimal.ZERO
          );
          line.setPickStatus(rs.getString("pick_status"));
          list.add(line);
        }
      }
    }
    return list;
  }

  /**
   * Get lines assigned to a specific user (for PDA/Mobile view).
   */
  public List<PickTaskLineDTO> getMyAssignedLines(Long userId)
    throws Exception {
    String sql = """
      SELECT ptl.pick_task_line_id, ptl.pick_task_id, ptl.gdn_line_id, ptl.from_slot_id,
          s.code AS slot_code, z.code AS zone_code,
          COALESCE(ptl.variant_id, gdl.variant_id) AS variant_id,
          pv.variant_sku, p.name AS product_name, pv.color, pv.size,
          ptl.qty_required, COALESCE(ptl.qty_to_pick, ptl.qty_required, 0) AS qty_to_pick,
          ptl.qty_picked, COALESCE(ptl.pick_status, 'PENDING') AS pick_status,
          ptl.assigned_at, ptl.completed_at,
          pt.wave_id, pw.wave_code, gdn.gdn_number AS gdn_number
      FROM pick_task_line ptl
      JOIN pick_task pt ON ptl.pick_task_id = pt.pick_task_id
      JOIN pick_wave pw ON pt.wave_id = pw.wave_id
      JOIN goods_delivery_line gdl ON gdl.gdn_line_id = ptl.gdn_line_id
      JOIN goods_delivery_note gdn ON gdn.gdn_id = gdl.gdn_id
      JOIN product_variant pv ON pv.variant_id = COALESCE(ptl.variant_id, gdl.variant_id)
      JOIN product p ON p.product_id = pv.product_id
      LEFT JOIN slot s ON s.slot_id = ptl.from_slot_id
      LEFT JOIN zone z ON z.zone_id = s.zone_id
      WHERE ptl.assigned_to = ? AND ptl.pick_status IN ('PENDING', 'PICKED')
      ORDER BY pw.wave_id, z.code ASC, s.code ASC, ptl.pick_task_line_id ASC
      """;

    List<PickTaskLineDTO> list = new ArrayList<>();
    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          PickTaskLineDTO line = new PickTaskLineDTO();
          line.setPickTaskLineId(rs.getLong("pick_task_line_id"));
          line.setPickTaskId(rs.getLong("pick_task_id"));
          line.setGdnLineId(rs.getLong("gdn_line_id"));
          line.setFromSlotId(
            rs.getObject("from_slot_id") != null
              ? rs.getLong("from_slot_id")
              : null
          );
          line.setSlotCode(rs.getString("slot_code"));
          line.setZoneCode(rs.getString("zone_code"));
          line.setVariantId(rs.getLong("variant_id"));
          line.setVariantSku(rs.getString("variant_sku"));
          line.setProductName(rs.getString("product_name"));
          line.setColor(rs.getString("color"));
          line.setSize(rs.getString("size"));
          line.setQtyRequired(rs.getBigDecimal("qty_required"));
          line.setQtyToPick(rs.getBigDecimal("qty_to_pick"));
          line.setQtyPicked(
            rs.getBigDecimal("qty_picked") != null
              ? rs.getBigDecimal("qty_picked")
              : BigDecimal.ZERO
          );
          line.setPickStatus(rs.getString("pick_status"));
          line.setAssignedTo(userId);
          Timestamp t = rs.getTimestamp("assigned_at");
          if (t != null) line.setAssignedAt(t.toLocalDateTime());
          t = rs.getTimestamp("completed_at");
          if (t != null) line.setCompletedAt(t.toLocalDateTime());
          list.add(line);
        }
      }
    }
    return list;
  }

  /**
   * Start picking a line - update line status to PICKED. Also updates task
   * status to IN_PROGRESS if this is the first line being picked.
   */
  public void startLinePicking(Long lineId, Long userId) throws Exception {
    String sqlGetTask =
      "SELECT pick_task_id FROM pick_task_line WHERE pick_task_line_id = ?";
    String sqlStartTask =
      "UPDATE pick_task SET status = 'IN_PROGRESS', started_at = NOW() WHERE pick_task_id = ? AND started_at IS NULL";
    String sqlStartLine =
      "UPDATE pick_task_line SET pick_status = 'PICKED' WHERE pick_task_line_id = ? AND pick_status = 'PENDING'";

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);
      try (
        PreparedStatement psGetTask = conn.prepareStatement(sqlGetTask);
        PreparedStatement psStartTask = conn.prepareStatement(sqlStartTask);
        PreparedStatement psStartLine = conn.prepareStatement(sqlStartLine)
      ) {
        psGetTask.setLong(1, lineId);
        Long taskId = null;
        try (ResultSet rs = psGetTask.executeQuery()) {
          if (rs.next()) {
            taskId = rs.getLong("pick_task_id");
          }
        }

        if (taskId != null) {
          psStartTask.setLong(1, taskId);
          psStartTask.executeUpdate();
        }

        psStartLine.setLong(1, lineId);
        psStartLine.executeUpdate();

        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  /**
   * Complete picking for a line - update qty_picked, status, inventory.
   */
  public void completeLinePicking(
    Long lineId,
    BigDecimal qtyPicked,
    Long pickedBy
  ) throws Exception {
    String sqlGetLine = """
      SELECT ptl.pick_task_id, ptl.gdn_line_id, ptl.from_slot_id, ptl.variant_id,
             ptl.qty_to_pick, pt.wave_id
      FROM pick_task_line ptl
      JOIN pick_task pt ON ptl.pick_task_id = pt.pick_task_id
      WHERE ptl.pick_task_line_id = ?
      """;
    String sqlUpdateLine = """
      UPDATE pick_task_line
      SET qty_picked = ?, pick_status = ?, completed_at = NOW()
      WHERE pick_task_line_id = ?
      """;
    String sqlGDNLine =
      "UPDATE goods_delivery_line SET qty_picked = COALESCE(qty_picked, 0) + ? WHERE gdn_line_id = ?";
    String sqlInv =
      "UPDATE inventory_balance SET qty_available = qty_available - ?, qty_on_hand = qty_on_hand - ?, updated_at = NOW() WHERE variant_id = ? AND slot_id = ?";
    String sqlTxn =
      "INSERT INTO inventory_txn (txn_type, warehouse_id, from_slot_id, to_slot_id, variant_id, `condition`, qty_delta, ref_doc_type, ref_doc_id, note, created_by, txn_at) VALUES (?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, NOW())";

    Long warehouseId = null;
    Long taskId = null;
    Long gdnLineId = null;
    Long fromSlotId = null;
    Long variantId = null;
    BigDecimal qtyToPick = null;

    try (Connection conn = getConnection()) {
      conn.setAutoCommit(false);
      try (
        PreparedStatement psGetLine = conn.prepareStatement(sqlGetLine);
        PreparedStatement psUpdateLine = conn.prepareStatement(sqlUpdateLine);
        PreparedStatement psGDNLine = conn.prepareStatement(sqlGDNLine);
        PreparedStatement psInv = conn.prepareStatement(sqlInv);
        PreparedStatement psTxn = conn.prepareStatement(sqlTxn)
      ) {
        psGetLine.setLong(1, lineId);
        try (ResultSet rs = psGetLine.executeQuery()) {
          if (rs.next()) {
            taskId = rs.getLong("pick_task_id");
            gdnLineId = rs.getLong("gdn_line_id");
            fromSlotId =
              rs.getObject("from_slot_id") != null
                ? rs.getLong("from_slot_id")
                : null;
            variantId =
              rs.getObject("variant_id") != null
                ? rs.getLong("variant_id")
                : null;
            qtyToPick = rs.getBigDecimal("qty_to_pick");

            if (fromSlotId != null) {
              String sqlWh =
                "SELECT ib.warehouse_id FROM inventory_balance ib WHERE ib.slot_id = ? AND ib.variant_id = ? LIMIT 1";
              try (PreparedStatement psWh = conn.prepareStatement(sqlWh)) {
                psWh.setLong(1, fromSlotId);
                psWh.setLong(2, variantId);
                try (ResultSet rsWh = psWh.executeQuery()) {
                  if (rsWh.next()) {
                    warehouseId =
                      rsWh.getObject("warehouse_id") != null
                        ? rsWh.getLong("warehouse_id")
                        : null;
                  }
                }
              }
            }
          }
        }

        if (qtyPicked == null) {
          qtyPicked = BigDecimal.ZERO;
        }

        String pickStatus;
        if (
          qtyPicked.compareTo(BigDecimal.ZERO) > 0 &&
          qtyPicked.compareTo(qtyToPick) < 0
        ) {
          pickStatus = "PICKED";
        } else if (qtyPicked.compareTo(BigDecimal.ZERO) > 0) {
          pickStatus = "DONE";
        } else {
          pickStatus = "PENDING";
        }

        psUpdateLine.setBigDecimal(1, qtyPicked);
        psUpdateLine.setString(2, pickStatus);
        psUpdateLine.setLong(3, lineId);
        psUpdateLine.executeUpdate();

        if (gdnLineId != null && qtyPicked.compareTo(BigDecimal.ZERO) > 0) {
          psGDNLine.setBigDecimal(1, qtyPicked);
          psGDNLine.setLong(2, gdnLineId);
          psGDNLine.executeUpdate();
        }

        if (
          fromSlotId != null &&
          variantId != null &&
          qtyPicked.compareTo(BigDecimal.ZERO) > 0
        ) {
          psInv.setBigDecimal(1, qtyPicked);
          psInv.setBigDecimal(2, qtyPicked);
          psInv.setLong(3, variantId);
          psInv.setLong(4, fromSlotId);
          psInv.executeUpdate();

          psTxn.setString(1, "PICK");
          psTxn.setObject(2, warehouseId);
          psTxn.setLong(3, fromSlotId);
          psTxn.setLong(4, variantId);
          psTxn.setString(5, "GOOD");
          psTxn.setBigDecimal(6, qtyPicked.negate());
          psTxn.setString(7, "PICK_TASK_LINE");
          psTxn.setLong(8, lineId);
          psTxn.setString(9, "Pick from slot");
          psTxn.setObject(10, pickedBy);
          psTxn.executeUpdate();
        }

        conn.commit();

        if (taskId != null) {
          checkAndUpdateTaskStatus(taskId);
        }
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  /**
   * Check and update task status based on line completion. If all lines are
   * DONE, update task to COMPLETED.
   */
  public void checkAndUpdateTaskStatus(Long taskId) throws Exception {
    String sqlGetLines =
      "SELECT COUNT(*) as total, SUM(CASE WHEN pick_status IN ('DONE', 'COMPLETED') THEN 1 ELSE 0 END) as completed FROM pick_task_line WHERE pick_task_id = ?";

    try (
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sqlGetLines)
    ) {
      ps.setLong(1, taskId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          int total = rs.getInt("total");
          int completed = rs.getInt("completed");

          if (total > 0 && total == completed) {
            String sqlUpdateTask =
              "UPDATE pick_task SET status = 'COMPLETED', completed_at = NOW() WHERE pick_task_id = ?";
            try (
              PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateTask)
            ) {
              psUpdate.setLong(1, taskId);
              psUpdate.executeUpdate();
            }

            String sqlGetWaveId =
              "SELECT wave_id FROM pick_task WHERE pick_task_id = ?";
            Long waveId = null;
            try (
              PreparedStatement psGetWave = conn.prepareStatement(sqlGetWaveId)
            ) {
              psGetWave.setLong(1, taskId);
              try (ResultSet rsWave = psGetWave.executeQuery()) {
                if (rsWave.next()) {
                  waveId = rsWave.getLong("wave_id");
                }
              }
            }

            if (waveId != null) {
              PickWaveDAO waveDao = new PickWaveDAO();
              waveDao.checkAndUpdateWaveStatus(waveId);
            }
          } else {
            boolean anyInProgress = false;
            String sqlCheckProgress =
              "SELECT COUNT(*) FROM pick_task_line WHERE pick_task_id = ? AND pick_status IN ('PICKED')";
            try (
              PreparedStatement psCheck = conn.prepareStatement(
                sqlCheckProgress
              )
            ) {
              psCheck.setLong(1, taskId);
              try (ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next()) {
                  anyInProgress = rsCheck.getInt(1) > 0;
                }
              }
            }

            if (anyInProgress) {
              String sqlUpdateTask =
                "UPDATE pick_task SET status = 'IN_PROGRESS' WHERE pick_task_id = ? AND status != 'IN_PROGRESS'";
              try (
                PreparedStatement psUpdate = conn.prepareStatement(
                  sqlUpdateTask
                )
              ) {
                psUpdate.setLong(1, taskId);
                psUpdate.executeUpdate();
              }
            }
          }
        }
      }
    }
  }
}
