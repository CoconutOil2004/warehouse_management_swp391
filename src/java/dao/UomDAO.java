package dao;

import context.DBContext;
import model.Uom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UomDAO extends DBContext {

    public List<Uom> getAll() throws Exception {
        List<Uom> list = new ArrayList<>();
        String sql = "SELECT uom_id, code, name FROM uom ORDER BY name";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Uom u = new Uom();
                u.setUomId(rs.getLong("uom_id"));
                u.setCode(rs.getString("code"));
                u.setName(rs.getString("name"));
                list.add(u);
            }
        }
        return list;
    }

    /** Trả về uom_id đầu tiên (để dùng làm mặc định khi product không chọn UOM). Null nếu bảng rỗng. */
    public Long getFirstId() throws Exception {
        String sql = "SELECT uom_id FROM uom ORDER BY uom_id LIMIT 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("uom_id");
        }
        return null;
    }
}
