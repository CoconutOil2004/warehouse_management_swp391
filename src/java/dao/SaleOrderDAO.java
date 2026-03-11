package dao;

import context.DBContext;
import dto.SOLineCreateDTO;
import dto.SaleOrderHeaderDTO;
import dto.SaleOrderLineDTO;
import dto.SaleOrderListDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderDAO extends DBContext {

    Connection conn = DBContext.getConnection();

    public List<SaleOrderListDTO> searchSalesOrders(
            String keyword,
            String status,
            Date shipFrom,
            Date shipTo,
            int limit,
            int offset) throws Exception {

        StringBuilder sql = new StringBuilder("""
            SELECT
                so.so_id,
                so.so_number,
                c.name AS customer_name,
                so.requested_ship_date,
                so.ship_to_address,
                so.status,
                u.username AS imported_by
            FROM sales_order so
            JOIN customer c ON so.customer_id = c.customer_id
            LEFT JOIN user u ON so.imported_by = u.user_id
            WHERE 1 = 1
        """);

        List<Object> params = new ArrayList<>();
        if (keyword != null) {
            keyword = keyword.trim();
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (so.so_number LIKE ? OR c.name LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null) {
            status = status.trim();
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND so.status = ? ");
            params.add(status);
        }
        if (shipFrom != null) {
            sql.append(" AND so.requested_ship_date >= ? ");
            params.add(shipFrom);
        }
        if (shipTo != null) {
            sql.append(" AND so.requested_ship_date <= ? ");
            params.add(shipTo);
        }
        sql.append(" ORDER BY so.so_id DESC LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);
        List<SaleOrderListDTO> list = new ArrayList<>();
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleOrderListDTO dto = new SaleOrderListDTO();
                    dto.setSoId(rs.getLong("so_id"));
                    dto.setSoNumber(rs.getString("so_number"));
                    dto.setCustomerName(rs.getString("customer_name"));
                    Date sqlDate = rs.getDate("requested_ship_date");
                    dto.setRequestedShipDate(
                            sqlDate != null ? sqlDate.toLocalDate() : null);
                    dto.setShipToAddress(rs.getString("ship_to_address"));
                    dto.setStatus(rs.getString("status"));
                    dto.setImportedByUsername(rs.getString("imported_by"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public int countSalesOrders(
            String keyword,
            String status,
            Date shipFrom,
            Date shipTo) throws Exception {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM sales_order so
            JOIN customer c ON so.customer_id = c.customer_id
            WHERE 1 = 1
        """);
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            keyword = keyword.trim();
            sql.append(" AND (so.so_number LIKE ? OR c.name LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND so.status = ? ");
            params.add(status.trim());
        }
        if (shipFrom != null) {
            sql.append(" AND so.requested_ship_date >= ? ");
            params.add(shipFrom);
        }
        if (shipTo != null) {
            sql.append(" AND so.requested_ship_date <= ? ");
            params.add(shipTo);
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

    public boolean existsBySoNumber(String soNumber) throws Exception {
        String sql = "SELECT 1 FROM sales_order WHERE so_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean deleteSalesOrder(long soId) throws SQLException {
        try (Connection con = DBContext.getConnection()) {
            boolean oldAutoCommit = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement("""
                        DELETE r FROM inventory_reservation r
                        INNER JOIN sales_order_line sol ON r.so_line_id = sol.so_line_id
                        WHERE sol.so_id = ?
                        """)) {
                    ps.setLong(1, soId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM sales_order_line WHERE so_id=?")) {
                    ps.setLong(1, soId);
                    ps.executeUpdate();
                }

                int affected;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM sales_order WHERE so_id=?")) {
                    ps.setLong(1, soId);
                    affected = ps.executeUpdate();
                }

                con.commit();
                return affected > 0;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    /**
     * Get SO by SO number (for GDN creation)
     */
    public SaleOrderHeaderDTO getSaleOrderByNumber(String soNumber) throws Exception {
        String sql = """
            SELECT
                so.so_id AS soId,
                so.so_number AS soNumber,
                so.customer_id AS customerId,
                c.code AS customerCode,
                c.name AS customerName,
                c.email AS customerEmail,
                c.address AS customerAddress,
                c.phone AS customerPhone,
                so.ship_to_address AS shipToAddress,
                so.requested_ship_date AS requestedShipDate,
                so.status AS status,
                so.imported_by AS importedBy,
                u.username AS importedByUsername,
                so.imported_at AS importedAt
            FROM sales_order so
            JOIN customer c ON c.customer_id = so.customer_id
            LEFT JOIN user u ON u.user_id = so.imported_by
            WHERE so.so_number = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SaleOrderHeaderDTO dto = new SaleOrderHeaderDTO();
                    dto.setSoId(rs.getLong("soId"));
                    dto.setSoNumber(rs.getString("soNumber"));
                    dto.setCustomerId(rs.getLong("customerId"));
                    dto.setCustomerCode(rs.getString("customerCode"));
                    dto.setCustomerName(rs.getString("customerName"));
                    dto.setCustomerEmail(rs.getString("customerEmail"));
                    dto.setCustomerAddress(rs.getString("customerAddress"));
                    dto.setCustomerPhone(rs.getString("customerPhone"));
                    dto.setShipToAddress(rs.getString("shipToAddress"));
                    Date shipDate = rs.getDate("requestedShipDate");
                    dto.setRequestedShipDate(shipDate != null ? shipDate.toLocalDate() : null);
                    dto.setStatus(rs.getString("status"));
                    long importedBy = rs.getLong("importedBy");
                    dto.setImportedBy(rs.wasNull() ? null : importedBy);
                    dto.setImportedByUsername(rs.getString("importedByUsername"));
                    Timestamp importedAt = rs.getTimestamp("importedAt");
                    dto.setImportedAt(importedAt != null ? importedAt.toLocalDateTime() : null);
                    return dto;
                }
            }
        }
        return null;
    }

    public SaleOrderHeaderDTO getSaleOrderHeader(long soId) throws Exception {
        String sql = """
            SELECT
                so.so_id AS soId,
                so.so_number AS soNumber,
                so.customer_id AS customerId,
                c.code AS customerCode,
                c.name AS customerName,
                c.email AS customerEmail,
                c.address AS customerAddress,
                c.phone AS customerPhone,
                so.ship_to_address AS shipToAddress,
                so.requested_ship_date AS requestedShipDate,
                so.status AS status,
                so.imported_by AS importedBy,
                u.username AS importedByUsername,
                so.imported_at AS importedAt
            FROM sales_order so
            JOIN customer c ON c.customer_id = so.customer_id
            LEFT JOIN user u ON u.user_id = so.imported_by
            WHERE so.so_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, soId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SaleOrderHeaderDTO dto = new SaleOrderHeaderDTO();
                    dto.setSoId(rs.getLong("soId"));
                    dto.setSoNumber(rs.getString("soNumber"));
                    dto.setCustomerId(rs.getLong("customerId"));
                    dto.setCustomerCode(rs.getString("customerCode"));
                    dto.setCustomerName(rs.getString("customerName"));
                    dto.setCustomerEmail(rs.getString("customerEmail"));
                    dto.setCustomerAddress(rs.getString("customerAddress"));
                    dto.setCustomerPhone(rs.getString("customerPhone"));
                    dto.setShipToAddress(rs.getString("shipToAddress"));
                    Date shipDate = rs.getDate("requestedShipDate");
                    dto.setRequestedShipDate(shipDate != null ? shipDate.toLocalDate() : null);
                    dto.setStatus(rs.getString("status"));
                    long importedBy = rs.getLong("importedBy");
                    dto.setImportedBy(rs.wasNull() ? null : importedBy);
                    dto.setImportedByUsername(rs.getString("importedByUsername"));
                    Timestamp importedAt = rs.getTimestamp("importedAt");
                    dto.setImportedAt(importedAt != null ? importedAt.toLocalDateTime() : null);
                    return dto;
                }
            }
        }
        return null; // hoặc throw "SO not found"
    }

    public List<SaleOrderLineDTO> getSaleOrderDetailLines(long soId) throws Exception {
        String sql = """
            SELECT
                sol.so_line_id,
                sol.so_id,
                p.product_id,
                p.sku AS product_sku,
                p.name AS product_name,
                pv.variant_id,
                pv.variant_sku,
                pv.color,
                pv.size,
                pv.barcode,
                sol.qty_ordered AS ordered_qty,
                sol.unit_price,
                sol.discount
            FROM sales_order_line sol
            JOIN product_variant pv ON pv.variant_id = sol.variant_id
            JOIN product p ON p.product_id = pv.product_id
            WHERE sol.so_id = ?
            ORDER BY sol.so_line_id
        """;

        List<SaleOrderLineDTO> lines = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, soId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleOrderLineDTO dto = new SaleOrderLineDTO();
                    dto.setSoLineId(rs.getLong("so_line_id"));
                    dto.setSoId(rs.getLong("so_id"));
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setProductSku(rs.getString("product_sku"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setVariantId(rs.getLong("variant_id"));
                    dto.setVariantSku(rs.getString("variant_sku"));
                    dto.setColor(rs.getString("color"));
                    dto.setSize(rs.getString("size"));
                    dto.setBarcode(rs.getString("barcode"));
                    dto.setOrderedQty(rs.getBigDecimal("ordered_qty"));
                    dto.setUnitPrice(rs.getBigDecimal("unit_price"));
                    dto.setDiscount(rs.getBigDecimal("discount"));
                    lines.add(dto);
                }
            }
        }

        return lines;
    }

    public long createManualSO(
            String soNumber,
            long customerId,
            Date requestedShipDate,
            String shipToAddress,
            long userId,
            List<SOLineCreateDTO> lines) throws Exception {
        return createSO(soNumber, customerId, requestedShipDate, shipToAddress, userId, lines, "CREATED");
    }

    /** Import from Excel: status = IMPORTED (không cho update sau này). */
    public long createImportedSO(
            String soNumber,
            long customerId,
            Date requestedShipDate,
            String shipToAddress,
            long userId,
            List<SOLineCreateDTO> lines) throws Exception {
        return createSO(soNumber, customerId, requestedShipDate, shipToAddress, userId, lines, "IMPORTED");
    }

    private long createSO(
            String soNumber,
            long customerId,
            Date requestedShipDate,
            String shipToAddress,
            long userId,
            List<SOLineCreateDTO> lines,
            String status) throws Exception {

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("SO must have at least 1 line");
        }

        final long warehouseId = 1L;
        final String condition = "GOOD";

        String sqlSO = """
        INSERT INTO sales_order
            (so_number, customer_id, requested_ship_date,
             ship_to_address, status, imported_by, imported_at, note)
        VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)
    """;

        // Insert line từng dòng để lấy so_line_id (reservation cần so_line_id)
        String sqlInsertLine = """
        INSERT INTO sales_order_line
            (so_id, variant_id, qty_ordered, unit_price, discount)
        VALUES (?, ?, ?, ?, ?)
    """;

        // Lock + check tồn
        String sqlLockSummary = """
        SELECT qty_available
        FROM inventory_summary
        WHERE warehouse_id = ?
          AND variant_id = ?
          AND `condition` = ?
        FOR UPDATE
    """;

        // Update reserved/available
        String sqlUpdateSummary = """
        UPDATE inventory_summary
        SET qty_reserved = qty_reserved + ?,
            qty_available = qty_available - ?
        WHERE warehouse_id = ?
          AND variant_id = ?
          AND `condition` = ?
    """;

        // Insert reservation
        String sqlInsertReservation = """
        INSERT INTO inventory_reservation
            (warehouse_id, so_line_id, variant_id, qty_reserved, status)
        VALUES (?, ?, ?, ?, 'ACTIVE')
    """;

        try (Connection con = DBContext.getConnection()) {
            if (con == null) {
                throw new SQLException("Cannot get DB connection");
            }

            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (
                    PreparedStatement psSO = con.prepareStatement(sqlSO, Statement.RETURN_GENERATED_KEYS); PreparedStatement psLock = con.prepareStatement(sqlLockSummary); PreparedStatement psLine = con.prepareStatement(sqlInsertLine, Statement.RETURN_GENERATED_KEYS); PreparedStatement psRes = con.prepareStatement(sqlInsertReservation); PreparedStatement psUpd = con.prepareStatement(sqlUpdateSummary)) {
                // 1) Insert SO header
                psSO.setString(1, soNumber);
                psSO.setLong(2, customerId);

                if (requestedShipDate != null) {
                    psSO.setDate(3, requestedShipDate);
                } else {
                    psSO.setNull(3, Types.DATE);
                }

                if (shipToAddress != null && !shipToAddress.isBlank()) {
                    psSO.setString(4, shipToAddress.trim());
                } else {
                    psSO.setNull(4, Types.VARCHAR);
                }

                psSO.setString(5, status);
                psSO.setLong(6, userId);

                // note (tạm để null)
                psSO.setNull(7, Types.VARCHAR);

                psSO.executeUpdate();

                long soId;
                try (ResultSet gk = psSO.getGeneratedKeys()) {
                    if (!gk.next()) {
                        throw new SQLException("Cannot get generated so_id");
                    }
                    soId = gk.getLong(1);
                }

                // 2) Với mỗi line: lock + check + insert line + reserve + update summary
                for (SOLineCreateDTO l : lines) {
                    if (l.getQty() == null || l.getQty().signum() <= 0) {
                        throw new IllegalArgumentException("Invalid qty for variantId=" + l.getVariantId());
                    }

                    // 2.1) Lock & check inventory_summary
                    psLock.setLong(1, warehouseId);
                    psLock.setLong(2, l.getVariantId());
                    psLock.setString(3, condition);

                    java.math.BigDecimal available = java.math.BigDecimal.ZERO;
                    boolean hasRow = false;

                    try (ResultSet rs = psLock.executeQuery()) {
                        if (rs.next()) {
                            hasRow = true;
                            available = rs.getBigDecimal("qty_available");
                            if (available == null) {
                                available = java.math.BigDecimal.ZERO;
                            }
                        }
                    }

                    if (!hasRow) {
                        throw new IllegalArgumentException(
                                "No inventory_summary row for variantId=" + l.getVariantId()
                                + " (warehouseId=" + warehouseId + ", condition=" + condition + ")");
                    }

                    if (available.compareTo(l.getQty()) < 0) {
                        throw new IllegalArgumentException(
                                "Not enough stock for variantId=" + l.getVariantId()
                                + ". Available=" + available + ", Required=" + l.getQty());
                    }

                    // 2.2) Insert SO line (lấy so_line_id)
                    psLine.setLong(1, soId);
                    psLine.setLong(2, l.getVariantId());
                    psLine.setBigDecimal(3, l.getQty());

                    if (l.getUnitPrice() != null) {
                        psLine.setBigDecimal(4, l.getUnitPrice());
                    } else {
                        psLine.setNull(4, Types.DECIMAL);
                    }

                    if (l.getDiscount() != null) {
                        psLine.setBigDecimal(5, l.getDiscount());
                    } else {
                        psLine.setNull(5, Types.DECIMAL);
                    }

                    psLine.executeUpdate();

                    long soLineId;
                    try (ResultSet gk2 = psLine.getGeneratedKeys()) {
                        if (!gk2.next()) {
                            throw new SQLException("Cannot get generated so_line_id");
                        }
                        soLineId = gk2.getLong(1);
                    }

                    // 2.3) Insert reservation (ACTIVE)
                    psRes.setLong(1, warehouseId);
                    psRes.setLong(2, soLineId);
                    psRes.setLong(3, l.getVariantId());
                    psRes.setBigDecimal(4, l.getQty());
                    psRes.executeUpdate();

                    // 2.4) Update inventory_summary (reserved += qty, available -= qty)
                    psUpd.setBigDecimal(1, l.getQty());
                    psUpd.setBigDecimal(2, l.getQty());
                    psUpd.setLong(3, warehouseId);
                    psUpd.setLong(4, l.getVariantId());
                    psUpd.setString(5, condition);

                    int updated = psUpd.executeUpdate();
                    if (updated != 1) {
                        throw new SQLException("Update inventory_summary failed for variantId=" + l.getVariantId());
                    }
                }

                con.commit();
                return soId;

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public void updateSalesOrderWithReservation(
            SaleOrderHeaderDTO header,
            List<SaleOrderLineDTO> newLines, long userId) throws Exception {

        if (newLines == null || newLines.isEmpty()) {
            throw new IllegalArgumentException("SO must have at least 1 line");
        }

        final long warehouseId = 1L;
        final String condition = "GOOD";

        try (Connection con = DBContext.getConnection()) {

            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try {

//                lock so + check SO status
                String sqlLockSO = "SELECT status FROM sales_order WHERE so_id=? FOR UPDATE";
                String status;

                try (PreparedStatement ps = con.prepareStatement(sqlLockSO)) {
                    ps.setLong(1, header.getSoId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("SO not found");
                        }
                        status = rs.getString("status");
                    }
                }

                if (!"CREATED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException(
                            "SO cannot be updated when status = " + status);
                }

                String sqlCheckGDN = """
                SELECT COUNT(*)
                FROM goods_delivery_line gdl
                JOIN sales_order_line sol
                    ON sol.so_line_id = gdl.so_line_id
                WHERE sol.so_id = ?
            """;

                try (PreparedStatement ps = con.prepareStatement(sqlCheckGDN)) {
                    ps.setLong(1, header.getSoId());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new IllegalArgumentException(
                                    "SO already has delivery. Cannot update.");
                        }
                    }
                }

                String sqlSelectRes = """
                SELECT r.reservation_id, r.variant_id, r.qty_reserved
                FROM inventory_reservation r
                JOIN sales_order_line sol
                    ON sol.so_line_id = r.so_line_id
                WHERE sol.so_id = ?
                  AND r.status='ACTIVE'
            """;

                String sqlReturnSummary = """
                UPDATE inventory_summary
                SET qty_reserved = qty_reserved - ?,
                    qty_available = qty_available + ?
                WHERE warehouse_id=? AND variant_id=? AND `condition`=?
            """;

                String sqlReleaseRes
                        = "UPDATE inventory_reservation SET status='RELEASED' WHERE reservation_id=?";

                try (PreparedStatement psSel = con.prepareStatement(sqlSelectRes); PreparedStatement psRet = con.prepareStatement(sqlReturnSummary); PreparedStatement psRel = con.prepareStatement(sqlReleaseRes)) {

                    psSel.setLong(1, header.getSoId());

                    try (ResultSet rs = psSel.executeQuery()) {
                        while (rs.next()) {

                            long reservationId = rs.getLong("reservation_id");
                            long variantId = rs.getLong("variant_id");
                            java.math.BigDecimal qty = rs.getBigDecimal("qty_reserved");

                            psRet.setBigDecimal(1, qty);
                            psRet.setBigDecimal(2, qty);
                            psRet.setLong(3, warehouseId);
                            psRet.setLong(4, variantId);
                            psRet.setString(5, condition);
                            psRet.executeUpdate();

                            psRel.setLong(1, reservationId);
                            psRel.executeUpdate();
                        }
                    }
                }

                // 4️⃣ Update header
                String sqlUpdateHeader = """
                UPDATE sales_order
                SET so_number=?,
                    customer_id=?,
                    requested_ship_date=?,
                    ship_to_address=?
                WHERE so_id=?
            """;

                try (PreparedStatement ps = con.prepareStatement(sqlUpdateHeader)) {

                    ps.setString(1, header.getSoNumber());
                    ps.setLong(2, header.getCustomerId());

                    if (header.getRequestedShipDate() != null) {
                        ps.setDate(3,
                                java.sql.Date.valueOf(header.getRequestedShipDate()));
                    } else {
                        ps.setNull(3, Types.DATE);
                    }

                    if (header.getShipToAddress() != null
                            && !header.getShipToAddress().isBlank()) {
                        ps.setString(4, header.getShipToAddress());
                    } else {
                        ps.setNull(4, Types.VARCHAR);
                    }

                    ps.setLong(5, header.getSoId());

                    ps.executeUpdate();
                }

                // Xóa inventory_reservation trước (vì FK so_line_id -> sales_order_line)
                try (PreparedStatement ps = con.prepareStatement("""
                        DELETE r FROM inventory_reservation r
                        INNER JOIN sales_order_line sol ON r.so_line_id = sol.so_line_id
                        WHERE sol.so_id = ?
                        """)) {
                    ps.setLong(1, header.getSoId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps
                        = con.prepareStatement("DELETE FROM sales_order_line WHERE so_id=?")) {
                    ps.setLong(1, header.getSoId());
                    ps.executeUpdate();
                }

                String sqlLockSummary = """
                SELECT qty_available
                FROM inventory_summary
                WHERE warehouse_id=? AND variant_id=? AND `condition`=?
                FOR UPDATE
            """;

                String sqlInsertLine = """
                INSERT INTO sales_order_line
                (so_id, variant_id, qty_ordered, unit_price, discount)
                VALUES (?,?,?,?,?)
            """;

                String sqlReserveSummary = """
                UPDATE inventory_summary
                SET qty_reserved = qty_reserved + ?,
                    qty_available = qty_available - ?
                WHERE warehouse_id=? AND variant_id=? AND `condition`=?
            """;

                String sqlInsertRes = """
                INSERT INTO inventory_reservation
                (warehouse_id, so_line_id, variant_id, qty_reserved, status)
                VALUES (?, ?, ?, ?, 'ACTIVE')
            """;

                try (PreparedStatement psLock = con.prepareStatement(sqlLockSummary); PreparedStatement psInsLine
                        = con.prepareStatement(sqlInsertLine,
                                Statement.RETURN_GENERATED_KEYS); PreparedStatement psRes = con.prepareStatement(sqlInsertRes); PreparedStatement psUpd
                        = con.prepareStatement(sqlReserveSummary)) {

                    for (SaleOrderLineDTO l : newLines) {

                        psLock.setLong(1, warehouseId);
                        psLock.setLong(2, l.getVariantId());
                        psLock.setString(3, condition);

                        java.math.BigDecimal available;

                        try (ResultSet rs = psLock.executeQuery()) {
                            if (!rs.next()) {
                                throw new IllegalArgumentException(
                                        "No inventory summary for variantId="
                                        + l.getVariantId());
                            }
                            available = rs.getBigDecimal("qty_available");
                        }

                        if (available.compareTo(l.getOrderedQty()) < 0) {
                            throw new IllegalArgumentException(
                                    "Not enough stock for variantId="
                                    + l.getVariantId());
                        }

                        // insert line
                        psInsLine.setLong(1, header.getSoId());
                        psInsLine.setLong(2, l.getVariantId());
                        psInsLine.setBigDecimal(3, l.getOrderedQty());
                        psInsLine.setBigDecimal(4, l.getUnitPrice());
                        psInsLine.setBigDecimal(5, l.getDiscount());
                        psInsLine.executeUpdate();

                        long soLineId;
                        try (ResultSet gk
                                = psInsLine.getGeneratedKeys()) {
                            gk.next();
                            soLineId = gk.getLong(1);
                        }

                        // insert reservation
                        psRes.setLong(1, warehouseId);
                        psRes.setLong(2, soLineId);
                        psRes.setLong(3, l.getVariantId());
                        psRes.setBigDecimal(4, l.getOrderedQty());
                        psRes.executeUpdate();

                        // update summary
                        psUpd.setBigDecimal(1, l.getOrderedQty());
                        psUpd.setBigDecimal(2, l.getOrderedQty());
                        psUpd.setLong(3, warehouseId);
                        psUpd.setLong(4, l.getVariantId());
                        psUpd.setString(5, condition);
                        psUpd.executeUpdate();
                    }
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }
}
