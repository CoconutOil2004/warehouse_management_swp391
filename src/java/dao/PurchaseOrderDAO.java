package dao;

import context.DBContext;
import dto.POLineCreateDTO;
import dto.PurchaseOrderHeaderDTO;
import dto.PurchaseOrderLineDTO;
import dto.PurchaseOrderListDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO extends DBContext {

    Connection conn = DBContext.getConnection();
    public PurchaseOrderHeaderDTO getPurchaseOrderHeader(long poId) throws Exception {
        String sql = """
                    SELECT
                        po.po_id AS poId,
                        po.po_number AS poNumber,
                        po.supplier_id AS supplierId,
                        s.code AS supplierCode,
                        s.name AS supplierName,
                        s.email AS supplierEmail,
                        s.phone AS supplierPhone,
                        s.address AS supplierAddress,
                        po.expected_delivery_date AS expectedDeliveryDate,
                        po.status,
                        po.note
                    FROM purchase_order po
                    JOIN supplier s ON s.supplier_id = po.supplier_id
                    WHERE po.po_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PurchaseOrderHeaderDTO dto = new PurchaseOrderHeaderDTO();
                    dto.setPoId(rs.getLong("poId"));
                    dto.setPoNumber(rs.getString("poNumber"));
                    dto.setSupplierId(rs.getLong("supplierId"));
                    dto.setSupplierCode(rs.getString("supplierCode"));
                    dto.setSupplierName(rs.getString("supplierName"));
                    dto.setSupplierEmail(rs.getString("supplierEmail"));
                    dto.setSupplierPhone(rs.getString("supplierPhone"));
                    dto.setSupplierAddress(rs.getString("supplierAddress"));
                    dto.setExpectedDeliveryDate(rs.getDate("expectedDeliveryDate"));
                    dto.setStatus(rs.getString("status"));
                    dto.setNote(rs.getString("note"));
                    return dto;
                }
            }
        }
        return null; // hoặc throw nếu muốn: "PO not found"
    }

    public List<PurchaseOrderLineDTO> getPurchaseOrderDetailLines(long poId) throws Exception {
        String sql = """
                 SELECT
                          pl.po_line_id,
                          p.product_id,
                          p.name AS product_name,
                          pv.variant_id,
                          pv.variant_sku,
                          pv.color,
                          pv.size,
                          pv.barcode,
                          pv.status AS variant_status,
                          pl.qty_ordered AS ordered_qty,
                          pl.unit_price,
                          (pl.qty_ordered * pl.unit_price) AS line_amount
                        FROM purchase_order_line pl
                        JOIN product_variant pv ON pv.variant_id = pl.variant_id
                        JOIN product p ON p.product_id = pv.product_id
                        WHERE pl.po_id = ?
                        ORDER BY pl.po_line_id
                """;
        List<PurchaseOrderLineDTO> lines = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderLineDTO dto = new PurchaseOrderLineDTO();
                    dto.setPoLineId(rs.getLong("po_line_id"));
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setVariantId(rs.getLong("variant_id"));
                    dto.setVariantSku(rs.getString("variant_sku"));
                    dto.setColor(rs.getString("color"));
                    dto.setSize(rs.getString("size"));
                    dto.setBarcode(rs.getString("barcode"));
                    dto.setVariantStatus(rs.getString("variant_status"));
                    dto.setOrderedQty(rs.getBigDecimal("ordered_qty"));
                    dto.setUnitPrice(rs.getBigDecimal("unit_price"));
                    dto.setLineAmount(rs.getBigDecimal("line_amount"));
                    lines.add(dto);
                }
            }
        }
        return lines;
    }

    public long createManualPO(
            String poNumber,
            long supplierId,
            Date expectedDate,
            String note,
            long userId,
            List<POLineCreateDTO> lines) throws Exception {
        // Manual create: status = CREATED, source_file_name = 'manual_create'
        return createPO(poNumber, supplierId, expectedDate, note, userId, lines,
                "CREATED", "manual_create");
    }

    public long createImportedPO(
            String poNumber,
            long supplierId,
            Date expectedDate,
            String note,
            long userId,
            List<POLineCreateDTO> lines) throws Exception {
        // Import from Excel: status = IMPORTED, source_file_name = 'excel_import'
        return createPO(poNumber, supplierId, expectedDate, note, userId, lines,
                "IMPORTED", "excel_import");
    }

    private long createPO(
            String poNumber,
            long supplierId,
            Date expectedDate,
            String note,
            long userId,
            List<POLineCreateDTO> lines,
            String status,
            String sourceFileName) throws Exception {

        String sqlPO = """
                    INSERT INTO purchase_order
                        (po_number, supplier_id, expected_delivery_date, status,
                         imported_by, imported_at, source_file_name, note)
                      VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)
                """;

        String sqlLine = """
                    INSERT INTO purchase_order_line
                      (po_id, variant_id, qty_ordered, unit_price, currency)
                    VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psPO = con.prepareStatement(sqlPO, Statement.RETURN_GENERATED_KEYS)) {
                psPO.setString(1, poNumber);
                psPO.setLong(2, supplierId);
                if (expectedDate != null) {
                    psPO.setDate(3, expectedDate);
                } else {
                    psPO.setNull(3, Types.DATE);
                }
                psPO.setString(4, status);
                psPO.setLong(5, userId);
                psPO.setString(6, sourceFileName);
                psPO.setString(7, note);

                psPO.executeUpdate();

                long poId;
                try (ResultSet rs = psPO.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Cannot get generated po_id");
                    }
                    poId = rs.getLong(1);
                }

                try (PreparedStatement psLine = con.prepareStatement(sqlLine)) {
                    for (POLineCreateDTO l : lines) {
                        psLine.setLong(1, poId);
                        psLine.setLong(2, l.getVariantId());
                        psLine.setBigDecimal(3, l.getQty());

                        if (l.getUnitPrice() != null) {
                            psLine.setBigDecimal(4, l.getUnitPrice());
                        } else {
                            psLine.setNull(4, Types.DECIMAL);
                        }
                        String currency = (l.getCurrency() == null || l.getCurrency().isBlank())
                                ? "VND"
                                : l.getCurrency().trim();
                        psLine.setString(5, currency);

                        psLine.addBatch();
                    }
                    psLine.executeBatch();
                }
                con.commit();
                return poId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public boolean deletePurchaseOrder(long poId) throws SQLException {
        String deleteLinesSql = "DELETE FROM purchase_order_line WHERE po_id = ?";
        String deletePoSql = "DELETE FROM purchase_order WHERE po_id = ?";

        boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteLinesSql)) {
                ps.setLong(1, poId);
                ps.executeUpdate();
            }

            int affected;
            try (PreparedStatement ps = conn.prepareStatement(deletePoSql)) {
                ps.setLong(1, poId);
                affected = ps.executeUpdate();
            }

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    public List<PurchaseOrderListDTO> searchPurchaseOrders(
            String keyword,
            String status,
            Date expectedFrom,
            Date expectedTo,
            int limit,
            int offset) throws Exception {

        StringBuilder sql = new StringBuilder("""
                    SELECT
                        po.po_id,
                        po.po_number,
                        s.name AS supplier_name,
                        po.expected_delivery_date,
                        po.status,
                        u.username AS imported_by,
                        po.imported_at
                    FROM purchase_order po
                    JOIN supplier s ON po.supplier_id = s.supplier_id
                    LEFT JOIN user u ON po.imported_by = u.user_id
                    WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null) {
            keyword = keyword.trim();
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (po.po_number LIKE ? OR s.name LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        if (status != null) {
            status = status.trim();
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND po.status = ? ");
            params.add(status);
        }

        if (expectedFrom != null) {
            sql.append(" AND po.expected_delivery_date >= ? ");
            params.add(expectedFrom);
        }
        if (expectedTo != null) {
            sql.append(" AND po.expected_delivery_date <= ? ");
            params.add(expectedTo);
        }

        sql.append(" ORDER BY po.po_id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<PurchaseOrderListDTO> list = new ArrayList<>();

        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderListDTO dto = new PurchaseOrderListDTO();
                    dto.setPoId(rs.getLong("po_id"));
                    dto.setPoNumber(rs.getString("po_number"));
                    dto.setSupplierName(rs.getString("supplier_name"));

                    Date sqlDate = rs.getDate("expected_delivery_date");
                    dto.setExpectedDeliveryDate(sqlDate != null ? sqlDate.toLocalDate() : null);

                    dto.setStatus(rs.getString("status"));
                    dto.setImportedByUsername(rs.getString("imported_by"));
                    Timestamp ts = rs.getTimestamp("imported_at");
                    dto.setImportedAt(ts != null ? ts.toLocalDateTime() : null);

                    list.add(dto);
                }
            }
        }

        return list;
    }

    public int countPurchaseOrders(String keyword, String status, Date expectedFrom, Date expectedTo) throws Exception {

        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(*)
                    FROM purchase_order po
                    JOIN supplier s ON po.supplier_id = s.supplier_id
                    WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (po.po_number LIKE ? OR s.name LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND po.status = ? ");
            params.add(status);
        }

        if (expectedFrom != null) {
            sql.append(" AND po.expected_delivery_date >= ? ");
            params.add(expectedFrom);
        }
        if (expectedTo != null) {
            sql.append(" AND po.expected_delivery_date <= ? ");
            params.add(expectedTo);
        }
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean existsByPoNumber(String poNumber) throws Exception {
        String sql = "SELECT 1 FROM purchase_order WHERE po_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, poNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // có dòng => đã tồn tại
            }
        }
    }

    public void updatePurchaseOrder(PurchaseOrderHeaderDTO header,
            List<PurchaseOrderLineDTO> lines) throws Exception {

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("PO must have at least 1 line");
        }

        boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1. Check status
            String status = getPoStatusForUpdate(header.getPoId());
            if (!"CREATED".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException(
                        "PO cannot be updated when status = " + status);
            }
            
            // 2. Update header
            updateHeader(header);

            // 3. Delete old lines
            deleteLinesByPoId(header.getPoId());

            // 4. Insert new lines
            insertLines(header.getPoId(), lines);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    private String getPoStatusForUpdate(long poId) throws Exception {
        String sql = "SELECT status FROM purchase_order WHERE po_id=? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("PO not found: " + poId);
                }
                return rs.getString("status");
            }
        }


    }
    private void updateHeader(PurchaseOrderHeaderDTO h) throws Exception {

        String sql = """
                    UPDATE purchase_order
                    SET po_number = ?,
                        supplier_id = ?,
                        expected_delivery_date = ?,
                        note = ?
                    WHERE po_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getPoNumber());
            ps.setLong(2, h.getSupplierId());
            ps.setDate(3, h.getExpectedDeliveryDate());
            ps.setString(4, h.getNote());
            ps.setLong(5, h.getPoId());
            ps.executeUpdate();
        }
    }

    private void deleteLinesByPoId(long poId) throws Exception {

        String sql = "DELETE FROM purchase_order_line WHERE po_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, poId);
            ps.executeUpdate();
        }
    }

    private void insertLines(long poId,
            List<PurchaseOrderLineDTO> lines) throws Exception {

        String sql = """
                    INSERT INTO purchase_order_line
                    (po_id, variant_id, qty_ordered, unit_price)
                    VALUES (?,?,?,?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (PurchaseOrderLineDTO l : lines) {

                ps.setLong(1, poId);
                ps.setLong(2, l.getVariantId());
                ps.setBigDecimal(3, l.getOrderedQty());
                ps.setBigDecimal(4, l.getUnitPrice());

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    public boolean updateStatus(long poId, String status) throws SQLException {
        String sql = "UPDATE purchase_order SET status = ? WHERE po_id = ?";
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.trim().toUpperCase());
            ps.setLong(2, poId);
            return ps.executeUpdate() > 0;
        }
    }

}
