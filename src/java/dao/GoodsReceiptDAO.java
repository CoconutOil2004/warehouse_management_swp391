package dao;

import context.DBContext;
import model.GoodsReceipt;
import model.GoodsReceiptLine;
import model.PutAwayLine;
import dto.ProductVariantDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoodsReceiptDAO extends DBContext {

    public List<dto.GoodsReceiptListDTO> getFilteredGRNs(String grnNumber, Long supplierId, String status,
            String sortField,
            String sortOrder, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                    SELECT gr.grn_id, gr.grn_number, gr.status, gr.created_at, po.po_number,
                           s.name as supplier_name, u.full_name as creator_name
                    FROM goods_receipt gr
                    JOIN purchase_order po ON gr.po_id = po.po_id
                    JOIN supplier s ON po.supplier_id = s.supplier_id
                    JOIN user u ON gr.created_by = u.user_id
                    WHERE 1=1
                """);

        if (grnNumber != null && !grnNumber.isBlank()) {
            sql.append(" AND gr.grn_number LIKE ?");
        }
        if (supplierId != null) {
            sql.append(" AND po.supplier_id = ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND gr.status = ?");
        }

        // Validate sortField and sortOrder to prevent SQL injection
        String validSortField = "gr.grn_id";
        if ("grn_number".equals(sortField)) {
            validSortField = "gr.grn_number";
        } else if ("supplier_name".equals(sortField)) {
            validSortField = "s.name";
        } else if ("status".equals(sortField)) {
            validSortField = "gr.status";
        } else if ("grn_id".equals(sortField)) {
            validSortField = "gr.grn_id";
        }

        String validSortOrder = "DESC";
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            validSortOrder = "ASC";
        }

        sql.append(" ORDER BY ").append(validSortField).append(" ").append(validSortOrder);
        sql.append(" LIMIT ? OFFSET ?");

        List<dto.GoodsReceiptListDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (grnNumber != null && !grnNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + grnNumber + "%");
            }
            if (supplierId != null) {
                ps.setLong(paramIdx++, supplierId);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(paramIdx++, status);
            }
            ps.setInt(paramIdx++, limit);
            ps.setInt(paramIdx++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.GoodsReceiptListDTO item = new dto.GoodsReceiptListDTO();
                    item.setGrnId(rs.getLong("grn_id"));
                    item.setGrnNumber(rs.getString("grn_number"));
                    item.setSupplierName(rs.getString("supplier_name"));
                    item.setStatus(rs.getString("status"));
                    item.setCreatorName(rs.getString("creator_name"));
                    item.setPoNumber(rs.getString("po_number"));
                    Timestamp createdAtTs = rs.getTimestamp("created_at");
                    if (createdAtTs != null) {
                        item.setCreatedAt(createdAtTs.toLocalDateTime());
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }

    public int countFilteredGRNs(String grnNumber, Long supplierId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(*)
                    FROM goods_receipt gr
                    JOIN purchase_order po ON gr.po_id = po.po_id
                    WHERE 1=1
                """);

        if (grnNumber != null && !grnNumber.isBlank()) {
            sql.append(" AND gr.grn_number LIKE ?");
        }
        if (supplierId != null) {
            sql.append(" AND po.supplier_id = ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND gr.status = ?");
        }

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (grnNumber != null && !grnNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + grnNumber + "%");
            }
            if (supplierId != null) {
                ps.setLong(paramIdx++, supplierId);
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

    public long createGRN(GoodsReceipt grn, List<GoodsReceiptLine> lines) throws SQLException {
        String sqlGR = """
                    INSERT INTO goods_receipt (grn_number, po_id, warehouse_id, status, created_by, created_at, note)
                    VALUES (?, ?, ?, 'PENDING', ?, NOW(), ?)
                """;
        String sqlLine = """
                    INSERT INTO goods_receipt_line (grn_id, po_line_id, variant_id, qty_expected, qty_received, qty_good, qty_missing, qty_damaged, qty_extra_good, qty_extra_damaged, note)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            long grnId;
            try (PreparedStatement ps = conn.prepareStatement(sqlGR, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, grn.getGrnNumber());
                ps.setLong(2, grn.getPoId());
                ps.setLong(3, grn.getWarehouseId());
                ps.setLong(4, grn.getCreatedBy());
                ps.setString(5, grn.getNote());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        grnId = rs.getLong(1);
                    } else {
                        throw new SQLException("Creating GRN failed, no ID obtained.");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlLine)) {
                for (GoodsReceiptLine line : lines) {
                    ps.setLong(1, grnId);
                    ps.setObject(2, line.getPoLineId());
                    ps.setLong(3, line.getVariantId());
                    ps.setBigDecimal(4, line.getQtyExpected());
                    ps.setBigDecimal(5, line.getQtyReceived());
                    ps.setBigDecimal(6, line.getQtyGood());
                    ps.setBigDecimal(7, line.getQtyMissing());
                    ps.setBigDecimal(8, line.getQtyDamaged());
                    ps.setBigDecimal(9, line.getQtyExtraGood() != null ? line.getQtyExtraGood() : java.math.BigDecimal.ZERO);
                    ps.setBigDecimal(10, line.getQtyExtraDamaged() != null ? line.getQtyExtraDamaged() : java.math.BigDecimal.ZERO);
                    ps.setString(11, line.getNote());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return grnId;
        } catch (SQLException e) {
            // Transaction rollback is handled if exception occurs
            throw e;
        }
    }

    public GoodsReceipt getById(Long id) throws SQLException {
        String sql = """
                    SELECT gr.*, po.po_number, s.name as supplier_name
                    FROM goods_receipt gr
                    JOIN purchase_order po ON gr.po_id = po.po_id
                    JOIN supplier s ON po.supplier_id = s.supplier_id
                    WHERE gr.grn_id = ?
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGR(rs);
                }
            }
        }
        return null;
    }

    public List<GoodsReceiptLine> getLinesByGrnId(Long grnId) throws SQLException {
        String sql = """
                    SELECT gl.*, pv.variant_sku AS sku, p.name AS product_name, pol.unit_price
                    FROM goods_receipt_line gl
                    JOIN product_variant pv ON gl.variant_id = pv.variant_id
                    JOIN product p ON pv.product_id = p.product_id
                    LEFT JOIN purchase_order_line pol ON gl.po_line_id = pol.po_line_id
                    WHERE gl.grn_id = ?
                """;
        List<GoodsReceiptLine> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, grnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLine(rs));
                }
            }
        }
        return list;
    }

    public boolean updateStatus(Long grnId, String status, Long userId) throws SQLException {
        String sql = "UPDATE goods_receipt SET status = ?, approved_by = ?, approved_at = NOW() WHERE grn_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            ps.setLong(3, grnId);
            return ps.executeUpdate() > 0;
        }
    }

    private GoodsReceipt mapResultSetToGR(ResultSet rs) throws SQLException {
        GoodsReceipt gr = new GoodsReceipt();
        gr.setGrnId(rs.getLong("grn_id"));
        gr.setGrnNumber(rs.getString("grn_number"));
        gr.setPoId(rs.getLong("po_id"));
        try {
            gr.setPoNumber(rs.getString("po_number"));
            gr.setSupplierName(rs.getString("supplier_name"));
        } catch (Exception e) {
        }
        gr.setWarehouseId(rs.getLong("warehouse_id"));
        gr.setStatus(rs.getString("status"));
        gr.setCreatedBy(rs.getLong("created_by"));
        gr.setApprovedBy(rs.getObject("approved_by") != null ? rs.getLong("approved_by") : null);
        gr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp ts = rs.getTimestamp("approved_at");
        if (ts != null) {
            gr.setApprovedAt(ts.toLocalDateTime());
        }
        gr.setDeliveredBy(rs.getString("delivered_by"));
        Timestamp rsTs = rs.getTimestamp("received_at");
        if (rsTs != null) {
            gr.setReceivedAt(rsTs.toLocalDateTime());
        }
        gr.setNote(rs.getString("note"));
        return gr;
    }

    private GoodsReceiptLine mapResultSetToLine(ResultSet rs) throws SQLException {
        GoodsReceiptLine l = new GoodsReceiptLine();
        l.setGrnLineId(rs.getLong("grn_line_id"));
        l.setGrnId(rs.getLong("grn_id"));
        l.setPoLineId(rs.getObject("po_line_id") != null ? rs.getLong("po_line_id") : null);
        l.setVariantId(rs.getLong("variant_id"));
        try {
            l.setSku(rs.getString("sku"));
            l.setProductName(rs.getString("product_name"));
        } catch (Exception e) {
        }
        l.setQtyExpected(rs.getBigDecimal("qty_expected"));
        l.setQtyReceived(rs.getBigDecimal("qty_received"));
        l.setQtyGood(rs.getBigDecimal("qty_good"));
        l.setQtyMissing(rs.getBigDecimal("qty_missing"));
        l.setQtyDamaged(rs.getBigDecimal("qty_damaged"));
        l.setQtyExtraGood(rs.getBigDecimal("qty_extra_good"));
        l.setQtyExtraDamaged(rs.getBigDecimal("qty_extra_damaged"));
        try {
            l.setUnitPrice(rs.getBigDecimal("unit_price"));
        } catch (Exception e) {
        }
        l.setNote(rs.getString("note"));
        return l;
    }

    public List<ProductVariantDTO> getActiveVariants() throws Exception {
        List<ProductVariantDTO> list = new ArrayList<>();

        String sql = """
                    SELECT
                        pv.variant_id,
                        pv.variant_sku,
                        p.sku AS product_sku,
                        p.name AS product_name
                    FROM product_variant pv
                    JOIN product p ON p.product_id = pv.product_id
                    ORDER BY pv.variant_id
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductVariantDTO v = new ProductVariantDTO();
                v.setVariantId(rs.getLong("variant_id"));
                v.setVariantSku(rs.getString("variant_sku"));
                v.setProductSku(rs.getString("product_sku"));
                v.setProductName(rs.getString("product_name"));
                list.add(v);
            }
        }
        return list;
    }

    public List<dto.PurchaseOrderListDTO> getPurchaseOrdersForSelection(Long includePoId) throws SQLException {
        String sql = """
                    SELECT po_id, po_number
                    FROM purchase_order
                    WHERE ((status != 'CLOSED' AND status != 'CANCELLED')
                           AND po_id NOT IN (SELECT po_id FROM goods_receipt WHERE status IN ('DRAFT', 'PENDING', 'APPROVED') AND po_id IS NOT NULL))
                    OR po_id = ?
                    ORDER BY po_id DESC
                """;
        List<dto.PurchaseOrderListDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, includePoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.PurchaseOrderListDTO dto = new dto.PurchaseOrderListDTO();
                    dto.setPoId(rs.getLong("po_id"));
                    dto.setPoNumber(rs.getString("po_number"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public boolean isGrnNumberExists(String grnNumber, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM goods_receipt WHERE grn_number = ? AND (? IS NULL OR grn_id != ?)";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, grnNumber);
            ps.setObject(2, excludeId);
            ps.setObject(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void deleteGRN(Long grnId) throws SQLException {
        String sqlPutawayLines = "DELETE FROM putaway_line WHERE grn_line_id IN (SELECT grn_line_id FROM goods_receipt_line WHERE grn_id = ?)";
        String sqlPutawayOrders = "DELETE FROM putaway_order WHERE grn_id = ?";
        String sqlLines = "DELETE FROM goods_receipt_line WHERE grn_id = ?";
        String sqlGR = "DELETE FROM goods_receipt WHERE grn_id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psPL = conn.prepareStatement(sqlPutawayLines)) {
                    psPL.setLong(1, grnId);
                    psPL.executeUpdate();
                }
                try (PreparedStatement psPO = conn.prepareStatement(sqlPutawayOrders)) {
                    psPO.setLong(1, grnId);
                    psPO.executeUpdate();
                }
                try (PreparedStatement psLines = conn.prepareStatement(sqlLines)) {
                    psLines.setLong(1, grnId);
                    psLines.executeUpdate();
                }
                try (PreparedStatement psGR = conn.prepareStatement(sqlGR)) {
                    psGR.setLong(1, grnId);
                    psGR.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updateGRN(GoodsReceipt grn, List<GoodsReceiptLine> lines) throws SQLException {
        String sqlUpdateGR = """
                    UPDATE goods_receipt
                    SET grn_number = ?, po_id = ?, warehouse_id = ?, note = ?
                    WHERE grn_id = ?
                """;
        String sqlDeleteLines = "DELETE FROM goods_receipt_line WHERE grn_id = ?";
        String sqlInsertLine = """
                    INSERT INTO goods_receipt_line (grn_id, po_line_id, variant_id, qty_expected, qty_received, qty_good, qty_missing, qty_damaged, qty_extra_good, qty_extra_damaged, note)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateGR)) {
                ps.setString(1, grn.getGrnNumber());
                ps.setLong(2, grn.getPoId());
                ps.setLong(3, grn.getWarehouseId());
                ps.setString(4, grn.getNote());
                ps.setLong(5, grn.getGrnId());
                ps.executeUpdate();
            }
            // Fix 500 error: Delete existing putaway info first because putaway_line refers
            // to grn_line_id
            String sqlDeletePutAwayLines = "DELETE FROM putaway_line WHERE grn_line_id IN (SELECT grn_line_id FROM goods_receipt_line WHERE grn_id = ?)";
            String sqlDeletePutAwayOrders = "DELETE FROM putaway_order WHERE grn_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDeletePutAwayLines)) {
                ps.setLong(1, grn.getGrnId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDeletePutAwayOrders)) {
                ps.setLong(1, grn.getGrnId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteLines)) {
                ps.setLong(1, grn.getGrnId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertLine)) {
                for (GoodsReceiptLine line : lines) {
                    ps.setLong(1, grn.getGrnId());
                    ps.setObject(2, line.getPoLineId());
                    ps.setLong(3, line.getVariantId());
                    ps.setBigDecimal(4, line.getQtyExpected());
                    ps.setBigDecimal(5, line.getQtyReceived());
                    ps.setBigDecimal(6, line.getQtyGood());
                    ps.setBigDecimal(7, line.getQtyMissing());
                    ps.setBigDecimal(8, line.getQtyDamaged());
                    ps.setBigDecimal(9, line.getQtyExtraGood() != null ? line.getQtyExtraGood() : java.math.BigDecimal.ZERO);
                    ps.setBigDecimal(10, line.getQtyExtraDamaged() != null ? line.getQtyExtraDamaged() : java.math.BigDecimal.ZERO);
                    ps.setString(11, line.getNote());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw e;
        }
    }

    public String getNextGrnNumber() throws SQLException {
        String sql = "SELECT MAX(grn_id) FROM goods_receipt";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong(1) + 1;
            }
            return String.format("GRN-%05d", nextId);
        }
    }

    public void savePutawayInfo(Long grnId, Long userId, List<PutAwayLine> lines) throws SQLException {
        String sqlOrder = "INSERT INTO putaway_order (grn_id, status, created_by, created_at) VALUES (?, 'DONE', ?, NOW())";
        String sqlLine = "INSERT INTO putaway_line (putaway_id, grn_line_id, to_slot_id, qty_putaway, performed_by, performed_at) VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            long putawayId;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, grnId);
                ps.setLong(2, userId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        putawayId = rs.getLong(1);
                    } else {
                        throw new SQLException("Failed to get putaway_id");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlLine)) {
                for (PutAwayLine line : lines) {
                    ps.setLong(1, putawayId);
                    ps.setLong(2, line.getGrnLineId());
                    ps.setLong(3, line.getToSlotId());
                    ps.setBigDecimal(4, line.getQtyPutaway());
                    ps.setLong(5, userId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        }
    }

    public List<dto.PutawayDetailDTO> getPutawayDetailsByGrnId(Long grnId) throws SQLException {
        String sql = """
                    SELECT
                        pv.variant_sku AS sku,
                        p.name AS product_name,
                        s.code AS slot_code,
                        z.name AS zone_name,
                        z.code AS zone_code,
                        z.zone_type AS zone_type,
                        pl.qty_putaway
                    FROM putaway_line pl
                    JOIN putaway_order po ON pl.putaway_id = po.putaway_id
                    JOIN goods_receipt_line gl ON pl.grn_line_id = gl.grn_line_id
                    JOIN product_variant pv ON gl.variant_id = pv.variant_id
                    JOIN product p ON pv.product_id = p.product_id
                    JOIN slot s ON pl.to_slot_id = s.slot_id
                    JOIN zone z ON s.zone_id = z.zone_id
                    WHERE po.grn_id = ?
                    ORDER BY pv.variant_sku, s.code
                """;
        List<dto.PutawayDetailDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, grnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.PutawayDetailDTO d = new dto.PutawayDetailDTO();
                    d.setSku(rs.getString("sku"));
                    d.setProductName(rs.getString("product_name"));
                    d.setSlotCode(rs.getString("slot_code"));
                    d.setZoneName(rs.getString("zone_name"));
                    String zCode = rs.getString("zone_code");
                    d.setZoneCode(zCode);
                    d.setQtyPutaway(rs.getBigDecimal("qty_putaway"));

                    String zType = rs.getString("zone_type");

                    // Determine type based on zone type
                    if ("DAMAGE".equals(zType)) {
                        d.setType("DAMAGED");
                    } else if ("STORAGE".equals(zType)) {
                        d.setType("GOOD");
                    } else if ("EXCESS".equals(zType)) {
                        d.setType("EXCESS");
                    } else {
                        d.setType("STORAGE");
                    }

                    list.add(d);
                }
            }
        }
        return list;
    }

    public List<PutAwayLine> getPutawayLinesByGrnId(Long grnId) throws SQLException {
        String sql = """
                    SELECT pl.*
                    FROM putaway_line pl
                    JOIN putaway_order po ON pl.putaway_id = po.putaway_id
                    WHERE po.grn_id = ?
                """;
        List<PutAwayLine> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, grnId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PutAwayLine l = new PutAwayLine();
                    l.setPutawayLineId(rs.getLong("putaway_line_id"));
                    l.setPutawayId(rs.getLong("putaway_id"));
                    l.setGrnLineId(rs.getLong("grn_line_id"));
                    l.setToSlotId(rs.getLong("to_slot_id"));
                    l.setQtyPutaway(rs.getBigDecimal("qty_putaway"));
                    list.add(l);
                }
            }
        }
        return list;
    }

    public Long getVariantIdByGrnLineId(Long grnLineId) throws SQLException {
        String sql = "SELECT variant_id FROM goods_receipt_line WHERE grn_line_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, grnLineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public boolean isPutawayComplete(Long grnId) throws SQLException {
        // Putaway must cover all physically received items: GOOD + DAMAGED + EXCESS
        String sqlExpected = "SELECT SUM(qty_good + qty_damaged + qty_extra_good + qty_extra_damaged) FROM goods_receipt_line WHERE grn_id = ?";
        String sqlActual = "SELECT SUM(pl.qty_putaway) FROM putaway_line pl JOIN putaway_order po ON pl.putaway_id = po.putaway_id WHERE po.grn_id = ?";

        java.math.BigDecimal expected = java.math.BigDecimal.ZERO;
        java.math.BigDecimal actual = java.math.BigDecimal.ZERO;

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlExpected)) {
                ps.setLong(1, grnId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        expected = rs.getBigDecimal(1);
                        if (expected == null) expected = java.math.BigDecimal.ZERO;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlActual)) {
                ps.setLong(1, grnId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        actual = rs.getBigDecimal(1);
                        if (actual == null) actual = java.math.BigDecimal.ZERO;
                    }
                }
            }
        }

        return expected.compareTo(java.math.BigDecimal.ZERO) > 0 && expected.compareTo(actual) == 0;
    }

    /**
     * Business rule: if there is any GRN (PENDING/DRAFT) for this PO whose
     * putaway is not complete yet, PO must not be editable to avoid mismatch.
     */
    public boolean hasIncompletePutawayForPo(Long poId) throws SQLException {
        if (poId == null) return false;
        String sql = """
                SELECT 1
                FROM goods_receipt gr
                JOIN (
                    SELECT grn_id, COALESCE(SUM(qty_good + qty_damaged + qty_extra_good + qty_extra_damaged), 0) AS expected
                    FROM goods_receipt_line
                    GROUP BY grn_id
                ) e ON e.grn_id = gr.grn_id
                LEFT JOIN (
                    SELECT po.grn_id, COALESCE(SUM(pl.qty_putaway), 0) AS actual
                    FROM putaway_order po
                    JOIN putaway_line pl ON pl.putaway_id = po.putaway_id
                    GROUP BY po.grn_id
                ) a ON a.grn_id = gr.grn_id
                WHERE gr.po_id = ?
                  AND gr.status IN ('PENDING', 'DRAFT')
                  AND e.expected > 0
                  AND e.expected <> COALESCE(a.actual, 0)
                LIMIT 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
