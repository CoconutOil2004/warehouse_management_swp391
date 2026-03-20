
package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import context.DBContext;
import dto.ProductDetailDTO;
import dto.ProductInventoryListDTO;
import dto.ProductListDTO;
import dto.ProductVariantDetailDTO;

public class ProductDAO extends DBContext {

    /**
     * Get all products without pagination (for dropdowns)
     *
     * @return List of all products
     * @throws Exception
     */
    public List<ProductListDTO> getProducts() throws Exception {
        List<ProductListDTO> list = new ArrayList<>();

        String sql = """
                    SELECT
                        p.product_id,
                        p.sku,
                        p.name,
                        p.barcode,
                        p.created_at
                    FROM product p
                    ORDER BY p.product_id DESC
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductListDTO dto = new ProductListDTO();
                dto.setProductId(rs.getLong("product_id"));
                dto.setSku(rs.getString("sku"));
                dto.setName(rs.getString("name"));
                dto.setBarcode(rs.getString("barcode"));

                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    dto.setCreatedAt(ts.toLocalDateTime());
                }

                list.add(dto);
            }
        }

        return list;
    }

    /**
     * Get products that exist in warehouse inventory with detailed inventory
     * information
     *
     * @param warehouseId   Warehouse ID
     * @param limit         Limit
     * @param offset        Offset
     * @param filterSku     Filter by SKU
     * @param filterName    Filter by name
     * @param filterBarcode Filter by barcode
     * @param sortBy        Sort column
     * @param sortOrder     Sort order (ASC/DESC)
     * @return List of products in inventory with details
     * @throws Exception
     */
    public List<ProductInventoryListDTO> getProductsInInventory(Long warehouseId, int limit, int offset,
            String filterSku, String filterName,
            String filterBarcode, String filterZoneCode,
            String filterCondition, String sortBy,
            String sortOrder) throws Exception {
        List<ProductInventoryListDTO> list = new ArrayList<>();

        // Validate sortBy to prevent SQL injection
        String[] allowedSortColumns = { "product_id", "sku", "name", "barcode", "created_at", "slot_code", "zone_code",
                "condition" };
        String validSortBy = "product_id";
        for (String col : allowedSortColumns) {
            if (col.equalsIgnoreCase(sortBy)) {
                validSortBy = col;
                break;
            }
        }

        // Validate sortOrder
        String validSortOrder = "DESC";
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            validSortOrder = "ASC";
        }

        // Build WHERE clause for filters
        StringBuilder whereClause = new StringBuilder();
        java.util.List<String> conditions = new ArrayList<>();

        conditions.add("ib.warehouse_id = ?");
        conditions.add("ib.qty_on_hand > 0");

        if (filterSku != null && !filterSku.isBlank()) {
            conditions.add("(p.sku LIKE ? OR pv.variant_sku LIKE ?)");
        }
        if (filterName != null && !filterName.isBlank()) {
            conditions.add("p.name LIKE ?");
        }
        if (filterBarcode != null && !filterBarcode.isBlank()) {
            conditions.add("(p.barcode LIKE ? OR pv.barcode LIKE ?)");
        }
        if (filterZoneCode != null && !filterZoneCode.isBlank()) {
            conditions.add("z.code = ?");
        }
        if (filterCondition != null && !filterCondition.isBlank()) {
            conditions.add("ib.condition = ?");
        }

        whereClause.append("WHERE ").append(String.join(" AND ", conditions));

        // Build ORDER BY clause
        String orderByClause = "";
        if ("slot_code".equalsIgnoreCase(validSortBy)) {
            orderByClause = "ORDER BY s.code " + validSortOrder;
        } else if ("zone_code".equalsIgnoreCase(validSortBy)) {
            orderByClause = "ORDER BY z.code " + validSortOrder;
        } else if ("condition".equalsIgnoreCase(validSortBy)) {
            orderByClause = "ORDER BY `condition` " + validSortOrder;
        } else {
            orderByClause = "ORDER BY p." + validSortBy + " " + validSortOrder;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append("    p.product_id, ");
        sqlBuilder.append("    p.sku, ");
        sqlBuilder.append("    p.name, ");
        sqlBuilder.append("    p.barcode, ");
        sqlBuilder.append("    p.created_at, ");
        sqlBuilder.append("    pv.variant_id, ");
        sqlBuilder.append("    pv.variant_sku, ");
        sqlBuilder.append("    COALESCE(SUM(ib.qty_on_hand), 0) AS total_qty_on_hand, ");
        sqlBuilder.append("    COALESCE(SUM(ib.qty_available), 0) AS total_qty_available, ");
        sqlBuilder.append("    MIN(s.code) AS slot_code, ");
        sqlBuilder.append("    MIN(z.code) AS zone_code, ");
        sqlBuilder.append("    MIN(z.name) AS zone_name, ");
        sqlBuilder.append("    MIN(ib.condition) AS `condition`, ");
        sqlBuilder.append("    MIN(w.code) AS warehouse_code, ");
        sqlBuilder.append("    MIN(w.name) AS warehouse_name ");
        sqlBuilder.append("FROM product p ");
        sqlBuilder.append("INNER JOIN product_variant pv ON pv.product_id = p.product_id ");
        sqlBuilder.append("INNER JOIN inventory_balance ib ON ib.variant_id = pv.variant_id ");
        sqlBuilder.append("INNER JOIN slot s ON s.slot_id = ib.slot_id ");
        sqlBuilder.append("INNER JOIN zone z ON z.zone_id = s.zone_id ");
        sqlBuilder.append("INNER JOIN warehouse w ON w.warehouse_id = ib.warehouse_id ");
        sqlBuilder.append(whereClause.toString());
        sqlBuilder.append(
                " GROUP BY p.product_id, p.sku, p.name, p.barcode, p.created_at, pv.variant_id, pv.variant_sku ");
        sqlBuilder.append(" ").append(orderByClause);
        sqlBuilder.append(" LIMIT ? OFFSET ?");

        String sql = sqlBuilder.toString();

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int paramIndex = 1;

            // Set warehouse ID
            ps.setLong(paramIndex++, warehouseId);

            // Set filter parameters
            if (filterSku != null && !filterSku.isBlank()) {
                String skuFilter = "%" + filterSku + "%";
                ps.setString(paramIndex++, skuFilter);
                ps.setString(paramIndex++, skuFilter);
            }
            if (filterName != null && !filterName.isBlank()) {
                ps.setString(paramIndex++, "%" + filterName + "%");
            }
            if (filterBarcode != null && !filterBarcode.isBlank()) {
                String barcodeFilter = "%" + filterBarcode + "%";
                ps.setString(paramIndex++, barcodeFilter);
                ps.setString(paramIndex++, barcodeFilter);
            }
            if (filterZoneCode != null && !filterZoneCode.isBlank()) {
                ps.setString(paramIndex++, filterZoneCode);
            }
            if (filterCondition != null && !filterCondition.isBlank()) {
                ps.setString(paramIndex++, filterCondition);
            }

            // Set limit and offset
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductInventoryListDTO dto = new ProductInventoryListDTO();
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setSku(rs.getString("sku"));
                    dto.setName(rs.getString("name"));
                    dto.setBarcode(rs.getString("barcode"));

                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        dto.setCreatedAt(ts.toLocalDateTime());
                    }

                    dto.setVariantId(rs.getLong("variant_id"));
                    dto.setVariantSku(rs.getString("variant_sku"));

                    BigDecimal qtyOnHand = rs.getBigDecimal("total_qty_on_hand");
                    dto.setTotalQtyOnHand(qtyOnHand != null ? qtyOnHand : BigDecimal.ZERO);

                    BigDecimal qtyAvailable = rs.getBigDecimal("total_qty_available");
                    dto.setTotalQtyAvailable(qtyAvailable != null ? qtyAvailable : BigDecimal.ZERO);

                    dto.setSlotCode(rs.getString("slot_code"));
                    dto.setZoneCode(rs.getString("zone_code"));
                    dto.setZoneName(rs.getString("zone_name"));
                    dto.setCondition(rs.getString("condition"));
                    dto.setWarehouseCode(rs.getString("warehouse_code"));
                    dto.setWarehouseName(rs.getString("warehouse_name"));

                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * Count products in warehouse inventory
     */
    public int countProductsInInventory(Long warehouseId, String filterSku,
            String filterName, String filterBarcode,
            String filterZoneCode, String filterCondition) throws Exception {
        // Build WHERE clause for filters
        StringBuilder whereClause = new StringBuilder();
        java.util.List<String> conditions = new ArrayList<>();

        conditions.add("ib.warehouse_id = ?");
        conditions.add("ib.qty_on_hand > 0");

        if (filterSku != null && !filterSku.isBlank()) {
            conditions.add("(p.sku LIKE ? OR pv.variant_sku LIKE ?)");
        }
        if (filterName != null && !filterName.isBlank()) {
            conditions.add("p.name LIKE ?");
        }
        if (filterBarcode != null && !filterBarcode.isBlank()) {
            conditions.add("(p.barcode LIKE ? OR pv.barcode LIKE ?)");
        }
        if (filterZoneCode != null && !filterZoneCode.isBlank()) {
            conditions.add("z.code = ?");
        }
        if (filterCondition != null && !filterCondition.isBlank()) {
            conditions.add("ib.condition = ?");
        }

        whereClause.append("WHERE ").append(String.join(" AND ", conditions));

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT COUNT(DISTINCT pv.variant_id) AS total ");
        sqlBuilder.append("FROM product p ");
        sqlBuilder.append("INNER JOIN product_variant pv ON pv.product_id = p.product_id ");
        sqlBuilder.append("INNER JOIN inventory_balance ib ON ib.variant_id = pv.variant_id ");
        sqlBuilder.append("INNER JOIN slot s ON s.slot_id = ib.slot_id ");
        sqlBuilder.append("INNER JOIN zone z ON z.zone_id = s.zone_id ");
        sqlBuilder.append(whereClause.toString());

        String sql = sqlBuilder.toString();

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int paramIndex = 1;

            // Set warehouse ID
            ps.setLong(paramIndex++, warehouseId);

            // Set filter parameters
            if (filterSku != null && !filterSku.isBlank()) {
                String skuFilter = "%" + filterSku + "%";
                ps.setString(paramIndex++, skuFilter);
                ps.setString(paramIndex++, skuFilter);
            }
            if (filterName != null && !filterName.isBlank()) {
                ps.setString(paramIndex++, "%" + filterName + "%");
            }
            if (filterBarcode != null && !filterBarcode.isBlank()) {
                String barcodeFilter = "%" + filterBarcode + "%";
                ps.setString(paramIndex++, barcodeFilter);
                ps.setString(paramIndex++, barcodeFilter);
            }
            if (filterZoneCode != null && !filterZoneCode.isBlank()) {
                ps.setString(paramIndex++, filterZoneCode);
            }
            if (filterCondition != null && !filterCondition.isBlank()) {
                ps.setString(paramIndex++, filterCondition);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    public List<ProductListDTO> getAllProducts(int limit, int offset) throws Exception {
        return getAllProducts(limit, offset, null, null, null, "product_id", "DESC");
    }

    public List<ProductListDTO> getAllProducts(int limit, int offset, String filterSku,
            String filterName, String filterBarcode,
            String sortBy, String sortOrder) throws Exception {
        List<ProductListDTO> list = new ArrayList<>();

        // Validate sortBy to prevent SQL injection
        String[] allowedSortColumns = { "product_id", "sku", "name", "barcode", "created_at" };
        String validSortBy = "product_id";
        for (String col : allowedSortColumns) {
            if (col.equalsIgnoreCase(sortBy)) {
                validSortBy = col;
                break;
            }
        }

        // Validate sortOrder
        String validSortOrder = "DESC";
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            validSortOrder = "ASC";
        }

        // Build WHERE clause for filters
        StringBuilder whereClause = new StringBuilder();
        java.util.List<String> conditions = new ArrayList<>();

        if (filterSku != null && !filterSku.isBlank()) {
            conditions.add("p.sku LIKE ?");
        }
        if (filterName != null && !filterName.isBlank()) {
            conditions.add("p.name LIKE ?");
        }
        if (filterBarcode != null && !filterBarcode.isBlank()) {
            conditions.add("p.barcode LIKE ?");
        }

        if (!conditions.isEmpty()) {
            whereClause.append("WHERE ").append(String.join(" AND ", conditions));
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append("    p.product_id, p.sku, p.name, p.barcode, p.category_id, ");
        sqlBuilder.append("    c.name AS category_name, ");
        sqlBuilder.append("    p.created_at ");
        sqlBuilder.append("FROM product p ");
        sqlBuilder.append("LEFT JOIN category c ON c.category_id = p.category_id ");
        sqlBuilder.append(whereClause.toString());
        sqlBuilder.append(" ORDER BY p.").append(validSortBy).append(" ").append(validSortOrder);
        sqlBuilder.append(" LIMIT ? OFFSET ?");

        String sql = sqlBuilder.toString();

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            int paramIndex = 1;

            if (filterSku != null && !filterSku.isBlank()) {
                ps.setString(paramIndex++, "%" + filterSku + "%");
            }
            if (filterName != null && !filterName.isBlank()) {
                ps.setString(paramIndex++, "%" + filterName + "%");
            }
            if (filterBarcode != null && !filterBarcode.isBlank()) {
                ps.setString(paramIndex++, "%" + filterBarcode + "%");
            }

            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductListDTO dto = new ProductListDTO();
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setSku(rs.getString("sku"));
                    dto.setName(rs.getString("name"));
                    dto.setBarcode(rs.getString("barcode"));
                    Long catId = rs.getObject("category_id") != null ? rs.getLong("category_id") : null;
                    dto.setCategoryId(catId);
                    dto.setCategoryName(rs.getString("category_name"));
                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null)
                        dto.setCreatedAt(ts.toLocalDateTime());
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /** Count products for pagination (filters: sku, name). */
    public int countAllProducts(String filterSku, String filterName) throws Exception {
        StringBuilder whereClause = new StringBuilder();
        java.util.List<String> conditions = new ArrayList<>();
        if (filterSku != null && !filterSku.isBlank())
            conditions.add("p.sku LIKE ?");
        if (filterName != null && !filterName.isBlank())
            conditions.add("p.name LIKE ?");
        if (!conditions.isEmpty())
            whereClause.append("WHERE ").append(String.join(" AND ", conditions));
        String sql = "SELECT COUNT(*) AS total FROM product p " + whereClause.toString();
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            if (filterSku != null && !filterSku.isBlank())
                ps.setString(idx++, "%" + filterSku + "%");
            if (filterName != null && !filterName.isBlank())
                ps.setString(idx++, "%" + filterName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Get next SKU for category, e.g. TSH -> TSH-001, TSH-002. Prefix must match
     * existing pattern PREFIX-NNN.
     */
    public String getNextSkuForCategory(String prefix) throws Exception {
        if (prefix == null || prefix.isBlank())
            return "PRD-001";
        String like = prefix + "-%";
        String sql = "SELECT sku FROM product WHERE sku LIKE ? ORDER BY product_id DESC LIMIT 1";
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString("sku");
                    int dash = last != null ? last.lastIndexOf('-') : -1;
                    if (dash >= 0 && dash + 1 < last.length()) {
                        try {
                            int num = Integer.parseInt(last.substring(dash + 1));
                            return prefix + "-" + String.format("%03d", num + 1);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        return prefix + "-001";
    }

    /**
     * Insert product; product.sku must be set (e.g. from getNextSkuForCategory).
     * base_uom_id bắt buộc trong DB nên dùng mặc định nếu null.
     */
    public long insertProduct(model.Product p) throws Exception {
        Long baseUomId = p.getBaseUomId();
        if (baseUomId == null) {
            baseUomId = new dao.UomDAO().getFirstId();
            if (baseUomId != null)
                p.setBaseUomId(baseUomId);
        }
        if (baseUomId == null) {
            throw new IllegalStateException(
                    "Không thể tạo product: bảng UOM trống. Vui lòng thêm ít nhất một đơn vị đo (UOM) trước.");
        }
        String sql = "INSERT INTO product (sku, name, category_id, base_uom_id, weight, length, width, height, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setObject(3, p.getCategoryId());
            ps.setLong(4, baseUomId);
            ps.setObject(5, p.getWeight());
            ps.setObject(6, p.getLength());
            ps.setObject(7, p.getWidth());
            ps.setObject(8, p.getHeight());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getLong(1);
            }
        }
        return -1;
    }

    /**
     * Update product (name, category_id, base_uom_id, weight, dimensions). No
     * delete.
     */
    public boolean updateProduct(model.Product p) throws Exception {
        String sql = "UPDATE product SET name = ?, category_id = ?, base_uom_id = ?, weight = ?, length = ?, width = ?, height = ? WHERE product_id = ?";
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setObject(2, p.getCategoryId());
            ps.setObject(3, p.getBaseUomId());
            ps.setObject(4, p.getWeight());
            ps.setObject(5, p.getLength());
            ps.setObject(6, p.getWidth());
            ps.setObject(7, p.getHeight());
            ps.setLong(8, p.getProductId());
            return ps.executeUpdate() > 0;
        }
    }

    public ProductDetailDTO getProductById(Long productId) throws Exception {
        String sql = """
                    SELECT
                        p.product_id,
                        p.sku,
                        p.name,
                        p.category_id,
                        c.name AS category_name,
                        p.base_uom_id,
                        u.name AS uom_name,
                        p.barcode,
                        p.weight,
                        p.length,
                        p.width,
                        p.height,
                        p.created_at
                    FROM product p
                    LEFT JOIN category c ON c.category_id = p.category_id
                    LEFT JOIN uom u ON u.uom_id = p.base_uom_id
                    WHERE p.product_id = ?
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductDetailDTO dto = new ProductDetailDTO();
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setSku(rs.getString("sku"));
                    dto.setName(rs.getString("name"));

                    Long catId = rs.getObject("category_id") != null ? rs.getLong("category_id") : null;
                    dto.setCategoryId(catId);
                    dto.setCategoryName(rs.getString("category_name"));

                    Long uomId = rs.getObject("base_uom_id") != null ? rs.getLong("base_uom_id") : null;
                    dto.setBaseUomId(uomId);
                    dto.setUomName(rs.getString("uom_name"));

                    dto.setBarcode(rs.getString("barcode"));
                    dto.setWeight(rs.getBigDecimal("weight"));
                    dto.setLength(rs.getBigDecimal("length"));
                    dto.setWidth(rs.getBigDecimal("width"));
                    dto.setHeight(rs.getBigDecimal("height"));

                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        dto.setCreatedAt(ts.toLocalDateTime());
                    }

                    // Lấy danh sách variants (cần warehouseId để lấy quantity)
                    // Note: warehouseId sẽ được truyền từ controller
                    List<ProductVariantDetailDTO> variants = getVariantsByProductId(productId, null);
                    dto.setVariants(variants);

                    return dto;
                }
            }
        }

        return null;
    }

    public ProductDetailDTO getProductById(Long productId, Long warehouseId) throws Exception {
        String sql = """
                    SELECT
                        p.product_id,
                        p.sku,
                        p.name,
                        p.category_id,
                        c.name AS category_name,
                        p.base_uom_id,
                        u.name AS uom_name,
                        p.barcode,
                        p.weight,
                        p.length,
                        p.width,
                        p.height,
                        p.created_at
                    FROM product p
                    LEFT JOIN category c ON c.category_id = p.category_id
                    LEFT JOIN uom u ON u.uom_id = p.base_uom_id
                    WHERE p.product_id = ?
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductDetailDTO dto = new ProductDetailDTO();
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setSku(rs.getString("sku"));
                    dto.setName(rs.getString("name"));

                    Long catId = rs.getObject("category_id") != null ? rs.getLong("category_id") : null;
                    dto.setCategoryId(catId);
                    dto.setCategoryName(rs.getString("category_name"));

                    Long uomId = rs.getObject("base_uom_id") != null ? rs.getLong("base_uom_id") : null;
                    dto.setBaseUomId(uomId);
                    dto.setUomName(rs.getString("uom_name"));

                    dto.setBarcode(rs.getString("barcode"));
                    dto.setWeight(rs.getBigDecimal("weight"));
                    dto.setLength(rs.getBigDecimal("length"));
                    dto.setWidth(rs.getBigDecimal("width"));
                    dto.setHeight(rs.getBigDecimal("height"));

                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        dto.setCreatedAt(ts.toLocalDateTime());
                    }

                    // Lấy danh sách variants với quantity từ inventory
                    List<ProductVariantDetailDTO> variants = null;
                    try {
                        variants = getVariantsByProductId(productId, warehouseId);
                    } catch (Exception e) {
                        // If error getting variants with warehouse, try without warehouseId
                        try {
                            variants = getVariantsByProductId(productId, null);
                        } catch (Exception e2) {
                            // If still error, set empty list
                            variants = new ArrayList<>();
                        }
                    }
                    if (variants == null) {
                        variants = new ArrayList<>();
                    }
                    dto.setVariants(variants);

                    return dto;
                }
            }
        }

        return null;
    }

    private List<ProductVariantDetailDTO> getVariantsByProductId(Long productId, Long warehouseId) throws Exception {
        List<ProductVariantDetailDTO> list = new ArrayList<>();

        try {
            String sql;
            if (warehouseId != null) {
                sql = """
                            SELECT
                                pv.variant_id,
                                pv.variant_sku,
                                pv.color,
                                pv.size,
                                pv.barcode,
                                pv.status,
                                COALESCE(SUM(ib.qty_on_hand), 0) AS total_qty_on_hand,
                                COALESCE(SUM(ib.qty_available), 0) AS total_qty_available
                            FROM product_variant pv
                            LEFT JOIN inventory_balance ib ON ib.variant_id = pv.variant_id AND ib.warehouse_id = ?
                            WHERE pv.product_id = ?
                            GROUP BY pv.variant_id, pv.variant_sku, pv.color, pv.size, pv.barcode, pv.status
                            ORDER BY pv.variant_id
                        """;
            } else {
                sql = """
                            SELECT
                                variant_id,
                                variant_sku,
                                color,
                                size,
                                barcode,
                                status
                            FROM product_variant
                            WHERE product_id = ?
                            ORDER BY variant_id
                        """;
            }

            try (Connection con = DBContext.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {

                if (warehouseId != null) {
                    ps.setLong(1, warehouseId);
                    ps.setLong(2, productId);
                } else {
                    ps.setLong(1, productId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            ProductVariantDetailDTO dto = new ProductVariantDetailDTO();
                            dto.setVariantId(rs.getLong("variant_id"));
                            dto.setVariantSku(rs.getString("variant_sku"));
                            dto.setColor(rs.getString("color"));
                            try {
                                dto.setColorHex(rs.getString("color_hex"));
                            } catch (Exception ignored) {
                            }
                            dto.setSize(rs.getString("size"));
                            dto.setBarcode(rs.getString("barcode"));
                            dto.setStatus(rs.getString("status"));

                            if (warehouseId != null) {
                                BigDecimal qtyOnHand = rs.getBigDecimal("total_qty_on_hand");
                                dto.setTotalQtyOnHand(qtyOnHand != null ? qtyOnHand : BigDecimal.ZERO);

                                BigDecimal qtyAvailable = rs.getBigDecimal("total_qty_available");
                                dto.setTotalQtyAvailable(qtyAvailable != null ? qtyAvailable : BigDecimal.ZERO);
                            } else {
                                // Set to null when no warehouseId (will be handled as 0 in JSON)
                                dto.setTotalQtyOnHand(null);
                                dto.setTotalQtyAvailable(null);
                            }

                            list.add(dto);
                        } catch (Exception e) {
                            // Skip this variant if error, continue with next
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If error, return empty list instead of throwing
            // Log error for debugging
            System.err.println("Error in getVariantsByProductId: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return list;
    }
}
