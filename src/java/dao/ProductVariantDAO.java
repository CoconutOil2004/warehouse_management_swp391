package dao;

import context.DBContext;
import dto.ProductVariantDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductVariantDAO extends DBContext {

    Connection con = DBContext.getConnection();

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
                    WHERE pv.status = 'ACTIVE'
                    ORDER BY pv.variant_id
                """;

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

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

    /**
     * Returns map variant_sku -> variant_id for the given SKUs.
     * Only includes SKUs that exist in DB. Empty set returns empty map.
     */
    public Map<String, Long> findIdsBySkus(Set<String> skus) throws Exception {
        Map<String, Long> out = new HashMap<>();
        if (skus == null || skus.isEmpty()) {
            return out;
        }
        List<String> list = new ArrayList<>(skus);
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT variant_sku, variant_id FROM product_variant WHERE variant_sku IN (" + placeholders + ")";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < list.size(); i++) {
                ps.setString(i + 1, list.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("variant_sku"), rs.getLong("variant_id"));
                }
            }
        }
        return out;
    }

    public List<ProductVariantDTO> listByProductId(long productId) throws Exception {
        String sql = "SELECT variant_id, variant_sku, color, size FROM product_variant WHERE product_id=? ORDER BY variant_id";
        List<ProductVariantDTO> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductVariantDTO v = new ProductVariantDTO();
                    v.setVariantId(rs.getLong("variant_id"));
                    v.setVariantSku(rs.getString("variant_sku"));
                    v.setColor(rs.getString("color"));
                    v.setSize(rs.getString("size"));
                    out.add(v);
                }
            }
        }
        return out;
    }

    /** Insert one variant. Returns generated variant_id or -1. */
    public long insert(long productId, String variantSku, String color, String colorHex, String size) throws Exception {
        String sql = "INSERT INTO product_variant (product_id, variant_sku, color, size, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, productId);
            ps.setString(2, variantSku);
            ps.setString(3, color);
            ps.setString(4, size);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    /** Set variant status (e.g. ACTIVE, INACTIVE). */
    public boolean updateStatus(long variantId, String status) throws Exception {
        String sql = "UPDATE product_variant SET status = ? WHERE variant_id = ?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, variantId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Check if variant_sku already exists for the product (to avoid duplicate in matrix). */
    public boolean existsByProductIdAndSku(long productId, String variantSku) throws Exception {
        String sql = "SELECT 1 FROM product_variant WHERE product_id = ? AND variant_sku = ? LIMIT 1";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, variantSku);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Get product_id for a variant (e.g. for redirect after inactive). */
    public Long getProductIdByVariantId(long variantId) throws Exception {
        String sql = "SELECT product_id FROM product_variant WHERE variant_id = ?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("product_id");
            }
        }
        return null;
    }

    /** True nếu variant đang có tồn trong kho (qty_on_hand > 0). Không cho inactive khi còn tồn. */
    public boolean hasStockInWarehouse(long variantId) throws Exception {
        String sql = "SELECT COALESCE(SUM(qty_on_hand), 0) AS total FROM inventory_balance WHERE variant_id = ?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal total = rs.getBigDecimal("total");
                    return total != null && total.compareTo(java.math.BigDecimal.ZERO) > 0;
                }
            }
        }
        return false;
    }
}
