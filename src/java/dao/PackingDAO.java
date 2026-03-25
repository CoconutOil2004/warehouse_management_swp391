package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import context.DBContext;
import dto.PackingLineConfigDTO;
import dto.PackingSessionDTO;
import dto.PackingTaskDTO;
import model.PackingLineConfig;
import model.PackingTask;

public class PackingDAO extends DBContext {

    /**
     * Create a new packing session
     */
    public Long createPackingSession(Long gdnId, Long createdBy) throws Exception {
        String sql = "INSERT INTO packing_session (gdn_id, status, created_by, created_at) VALUES (?, 'PENDING', ?, NOW())";
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

    /**
     * Batch insert packing line configs
     */
    public void saveLineConfigs(Long sessionId, List<PackingLineConfig> configs) throws Exception {
        if (configs == null || configs.isEmpty()) return;
        String sql = "INSERT INTO packing_line_config (packing_session_id, gdn_line_id, items_per_pack, num_packs) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (PackingLineConfig cfg : configs) {
                ps.setLong(1, sessionId);
                ps.setLong(2, cfg.getGdnLineId());
                ps.setInt(3, cfg.getItemsPerPack());
                ps.setInt(4, cfg.getNumPacks());
                ps.addBatch();
            }
            ps.executeBatch();
            
            // Retrieve generated IDs and set them back to models (needed for task assignment)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                while (rs.next() && i < configs.size()) {
                    configs.get(i).setPackingLineConfigId(rs.getLong(1));
                    i++;
                }
            }
        }
    }

    /**
     * Batch insert packing tasks
     */
    public void savePackingTasks(List<PackingTask> tasks) throws Exception {
        if (tasks == null || tasks.isEmpty()) return;
        String sql = "INSERT INTO packing_task (packing_line_config_id, assigned_to, assigned_packs, packed_packs, status, created_at) VALUES (?, ?, ?, 0, 'PENDING', NOW())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PackingTask t : tasks) {
                ps.setLong(1, t.getPackingLineConfigId());
                ps.setLong(2, t.getAssignedTo());
                ps.setInt(3, t.getAssignedPacks());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Get Session by GDN ID
     */
    public PackingSessionDTO getPackingSessionByGdnId(Long gdnId) throws Exception {
        String sql = """
                SELECT ps.*, gdn.gdn_number, so.so_number, c.name AS customer_name, u.full_name AS created_by_name
                FROM packing_session ps
                JOIN goods_delivery_note gdn ON ps.gdn_id = gdn.gdn_id
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN `user` u ON ps.created_by = u.user_id
                WHERE ps.gdn_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPackingSessionDTO(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get Session by ID
     */
    public PackingSessionDTO getPackingSessionById(Long packingSessionId) throws Exception {
        String sql = """
                SELECT ps.*, gdn.gdn_number, so.so_number, c.name AS customer_name, u.full_name AS created_by_name
                FROM packing_session ps
                JOIN goods_delivery_note gdn ON ps.gdn_id = gdn.gdn_id
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN `user` u ON ps.created_by = u.user_id
                WHERE ps.packing_session_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, packingSessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPackingSessionDTO(rs);
                }
            }
        }
        return null;
    }

    /**
     * List sessions (for packing-list page)
     */
    public List<PackingSessionDTO> listPackingSessions(String status) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT ps.*, gdn.gdn_number, so.so_number, c.name AS customer_name, u.full_name AS created_by_name
                FROM packing_session ps
                JOIN goods_delivery_note gdn ON ps.gdn_id = gdn.gdn_id
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN `user` u ON ps.created_by = u.user_id
                WHERE 1=1
                """);
        if (status != null && !status.isBlank()) {
            sql.append(" AND ps.status = ?");
        }
        sql.append(" ORDER BY ps.packing_session_id DESC");

        List<PackingSessionDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (status != null && !status.isBlank()) {
                ps.setString(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPackingSessionDTO(rs));
                }
            }
        }
        return list;
    }

    /**
     * List GDNs ready for packing (for Create Packing view)
     */
    public List<PackingSessionDTO> listGDNsReadyForPacking() throws Exception {
        String sql = """
                SELECT gdn.gdn_id, gdn.gdn_number, so.so_number, c.name AS customer_name
                FROM goods_delivery_note gdn
                LEFT JOIN sales_order so ON gdn.so_id = so.so_id
                LEFT JOIN customer c ON so.customer_id = c.customer_id
                LEFT JOIN packing_session ps ON gdn.gdn_id = ps.gdn_id
                WHERE gdn.status = 'PACKING' AND ps.packing_session_id IS NULL
                ORDER BY gdn.gdn_id ASC
                """;
        List<PackingSessionDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PackingSessionDTO dto = new PackingSessionDTO();
                dto.setGdnId(rs.getLong("gdn_id"));
                dto.setGdnNumber(rs.getString("gdn_number"));
                dto.setSoNumber(rs.getString("so_number"));
                dto.setCustomerName(rs.getString("customer_name"));
                dto.setStatus("NOT_STARTED");
                list.add(dto);
            }
        }
        return list;
    }

    /**
     * Get Line Configs for a Session
     */
    public List<PackingLineConfigDTO> getLineConfigsBySessionId(Long sessionId) throws Exception {
        String sql = """
                SELECT plc.*, pv.variant_sku, p.name AS product_name, pv.color, pv.size, gdl.qty_picked
                FROM packing_line_config plc
                JOIN goods_delivery_line gdl ON plc.gdn_line_id = gdl.gdn_line_id
                JOIN product_variant pv ON gdl.variant_id = pv.variant_id
                JOIN product p ON pv.product_id = p.product_id
                WHERE plc.packing_session_id = ?
                ORDER BY plc.packing_line_config_id ASC
                """;
        List<PackingLineConfigDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PackingLineConfigDTO dto = new PackingLineConfigDTO();
                    dto.setPackingLineConfigId(rs.getLong("packing_line_config_id"));
                    dto.setPackingSessionId(rs.getLong("packing_session_id"));
                    dto.setGdnLineId(rs.getLong("gdn_line_id"));
                    dto.setItemsPerPack(rs.getInt("items_per_pack"));
                    dto.setNumPacks(rs.getInt("num_packs"));
                    dto.setVariantSku(rs.getString("variant_sku"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setColor(rs.getString("color"));
                    dto.setSize(rs.getString("size"));
                    dto.setQtyPicked(rs.getBigDecimal("qty_picked"));
                    list.add(dto);
                }
            }
        }
        return list;
    }
    
    /**
     * Get Tasks for a Session (for manager view)
     */
    public List<PackingTaskDTO> getTasksBySessionId(Long sessionId) throws Exception {
        String sql = """
                SELECT pt.*, u.full_name AS assigned_to_name, gdn.gdn_number, 
                       pv.variant_sku, p.name AS product_name, plc.items_per_pack, gdl.qty_picked
                FROM packing_task pt
                JOIN packing_line_config plc ON pt.packing_line_config_id = plc.packing_line_config_id
                JOIN goods_delivery_line gdl ON plc.gdn_line_id = gdl.gdn_line_id
                JOIN goods_delivery_note gdn ON gdl.gdn_id = gdn.gdn_id
                JOIN product_variant pv ON gdl.variant_id = pv.variant_id
                JOIN product p ON pv.product_id = p.product_id
                LEFT JOIN `user` u ON pt.assigned_to = u.user_id
                WHERE plc.packing_session_id = ?
                ORDER BY pt.packing_task_id ASC
                """;
        return fetchTasks(sql, sessionId);
    }

    /**
     * Get Tasks for a specific user (for my-packing-task view)
     */
    public List<PackingTaskDTO> getTasksByUserId(Long userId) throws Exception {
        String sql = """
                SELECT pt.*, u.full_name AS assigned_to_name, gdn.gdn_number, 
                       pv.variant_sku, p.name AS product_name, plc.items_per_pack, gdl.qty_picked
                FROM packing_task pt
                JOIN packing_line_config plc ON pt.packing_line_config_id = plc.packing_line_config_id
                JOIN goods_delivery_line gdl ON plc.gdn_line_id = gdl.gdn_line_id
                JOIN goods_delivery_note gdn ON gdl.gdn_id = gdn.gdn_id
                JOIN product_variant pv ON gdl.variant_id = pv.variant_id
                JOIN product p ON pv.product_id = p.product_id
                LEFT JOIN `user` u ON pt.assigned_to = u.user_id
                WHERE pt.assigned_to = ? AND pt.status != 'DONE'
                ORDER BY pt.packing_task_id ASC
                """;
        return fetchTasks(sql, userId);
    }
    
    private List<PackingTaskDTO> fetchTasks(String sql, Long param) throws Exception {
        List<PackingTaskDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PackingTaskDTO dto = new PackingTaskDTO();
                    dto.setPackingTaskId(rs.getLong("packing_task_id"));
                    dto.setPackingLineConfigId(rs.getLong("packing_line_config_id"));
                    dto.setAssignedTo(rs.getLong("assigned_to"));
                    dto.setAssignedPacks(rs.getInt("assigned_packs"));
                    dto.setPackedPacks(rs.getInt("packed_packs"));
                    dto.setStatus(rs.getString("status"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) dto.setCreatedAt(createdAt.toLocalDateTime());
                    
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) dto.setUpdatedAt(updatedAt.toLocalDateTime());
                    
                    dto.setAssignedToName(rs.getString("assigned_to_name"));
                    dto.setGdnNumber(rs.getString("gdn_number"));
                    dto.setVariantSku(rs.getString("variant_sku"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setItemsPerPack(rs.getInt("items_per_pack"));
                    dto.setQtyPicked(rs.getBigDecimal("qty_picked"));
                    
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * Update Task Progress (Incremental)
     * @return true if successful, false if it exceeds assigned_packs
     */
    public boolean updateTaskProgress(Long taskId, int newlyPacked) throws Exception {
        // First check if it exceeds
        String checkSql = "SELECT packed_packs, assigned_packs FROM packing_task WHERE packing_task_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setLong(1, taskId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    int current = rs.getInt("packed_packs");
                    int assigned = rs.getInt("assigned_packs");
                    if (current + newlyPacked > assigned) {
                        return false;
                    }
                }
            }
        }

        String sql = """
                UPDATE packing_task 
                SET packed_packs = packed_packs + ?, 
                    updated_at = NOW(),
                    status = CASE WHEN (packed_packs + ?) >= assigned_packs THEN 'DONE' 
                                  WHEN (packed_packs + ?) > 0 THEN 'IN_PROGRESS'
                                  ELSE 'PENDING' END
                WHERE packing_task_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newlyPacked);
            ps.setInt(2, newlyPacked);
            ps.setInt(3, newlyPacked);
            ps.setLong(4, taskId);
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Update session status to IN_PROGRESS (called when first task starts)
     */
    public void markSessionInProgress(Long sessionId) throws Exception {
        String sql = "UPDATE packing_session SET status = 'IN_PROGRESS' WHERE packing_session_id = ? AND status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.executeUpdate();
        }
    }

    /**
     * Check if all lines in the session are fully done
     */
    public boolean isAllLinesDoneForSession(Long sessionId) throws Exception {
        String sql = """
                SELECT COUNT(*) FROM packing_line_config plc
                WHERE plc.packing_session_id = ?
                AND NOT EXISTS (
                    SELECT 1 FROM packing_task pt 
                    WHERE pt.packing_line_config_id = plc.packing_line_config_id 
                    AND pt.status != 'DONE'
                )
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Check if total lines equals done lines
                    return rs.getInt(1) == getTotalLineConfigsForSession(sessionId);
                }
            }
        }
        return false;
    }

    private int getTotalLineConfigsForSession(Long sessionId) throws Exception {
        String sql = "SELECT COUNT(*) FROM packing_line_config WHERE packing_session_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Check if all tasks for a specific line are done
     */
    public boolean isAllTasksDoneForLine(Long gdnLineId) throws Exception {
        String sql = """
                SELECT COUNT(*) FROM packing_task pt
                JOIN packing_line_config plc ON pt.packing_line_config_id = plc.packing_line_config_id
                WHERE plc.gdn_line_id = ? AND pt.status != 'DONE'
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gdnLineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    /**
     * Check if all tasks for a session are done
     */
    public boolean isAllTasksDoneForSession(Long sessionId) throws Exception {
        String sql = """
                SELECT COUNT(*) FROM packing_task pt
                JOIN packing_line_config plc ON pt.packing_line_config_id = plc.packing_line_config_id
                WHERE plc.packing_session_id = ? AND pt.status != 'DONE'
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    /**
     * Mark session as DONE
     */
    public void completeSession(Long sessionId) throws Exception {
        String sql = "UPDATE packing_session SET status = 'DONE', completed_at = NOW() WHERE packing_session_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.executeUpdate();
        }
    }
    
    private PackingSessionDTO mapPackingSessionDTO(ResultSet rs) throws SQLException {
        PackingSessionDTO dto = new PackingSessionDTO();
        dto.setPackingSessionId(rs.getLong("packing_session_id"));
        dto.setGdnId(rs.getLong("gdn_id"));
        dto.setStatus(rs.getString("status"));
        dto.setCreatedBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null);
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) dto.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) dto.setCompletedAt(completedAt.toLocalDateTime());
        
        dto.setGdnNumber(rs.getString("gdn_number"));
        dto.setSoNumber(rs.getString("so_number"));
        dto.setCustomerName(rs.getString("customer_name"));
        dto.setCreatedByName(rs.getString("created_by_name"));
        return dto;
    }
}
