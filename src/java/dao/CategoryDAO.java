package dao;

import context.DBContext;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO extends DBContext {

    public List<Category> getAll() throws Exception {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name, COALESCE(code, CONCAT('C', category_id)) AS code, COALESCE(size_type, 'NUMBER') AS size_type, parent_id FROM category ORDER BY name";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = mapCategory(rs);
                list.add(c);
            }
        } catch (Exception e) {
            // Fallback if code/size_type columns do not exist
            sql = "SELECT category_id, name, parent_id FROM category ORDER BY name";
            try (Connection con = DBContext.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category c = new Category();
                    c.setCategoryId(rs.getLong("category_id"));
                    c.setName(rs.getString("name"));
                    c.setParentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
                    c.setCode("C" + c.getCategoryId());
                    c.setSizeType("NUMBER");
                    list.add(c);
                }
            }
        }
        return list;
    }

    public Category getById(Long categoryId) throws Exception {
        if (categoryId == null) return null;
        String sql = "SELECT category_id, name, COALESCE(code, CONCAT('C', category_id)) AS code, COALESCE(size_type, 'NUMBER') AS size_type, parent_id FROM category WHERE category_id = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCategory(rs);
            }
        } catch (Exception e) {
            sql = "SELECT category_id, name, parent_id FROM category WHERE category_id = ?";
            try (Connection con = DBContext.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Category c = new Category();
                        c.setCategoryId(rs.getLong("category_id"));
                        c.setName(rs.getString("name"));
                        c.setParentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
                        c.setCode("C" + categoryId);
                        c.setSizeType("NUMBER");
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private Category mapCategory(ResultSet rs) throws java.sql.SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getLong("category_id"));
        c.setName(rs.getString("name"));
        c.setCode(rs.getString("code"));
        c.setSizeType(rs.getString("size_type"));
        c.setParentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
        if (c.getCode() == null) c.setCode("C" + c.getCategoryId());
        if (c.getSizeType() == null) c.setSizeType("NUMBER");
        return c;
    }
}
