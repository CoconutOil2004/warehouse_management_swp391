package dao;

import context.DBContext;
import model.Warehouse;
import dto.WarehouseDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO extends DBContext implements Dao<Warehouse> {

    public List<WarehouseDTO> getActiveWarehouses() throws SQLException {
        List<WarehouseDTO> list = new ArrayList<>();
        String sql = "SELECT warehouse_id, code, name FROM warehouse WHERE status = 'ACTIVE' ORDER BY name";
        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                WarehouseDTO w = new WarehouseDTO();
                w.setWarehouseId(rs.getLong("warehouse_id"));
                w.setCode(rs.getString("code"));
                w.setName(rs.getString("name"));
                list.add(w);
            }
        }
        return list;
    }

    @Override
    public List<Warehouse> getList(Object[] parameters) throws SQLException {
        return null;
    }

    public List<Warehouse> getList(String search, String sort, Long page, Long size) throws SQLException {
        List<Warehouse> list = new ArrayList<>();

        String query = """
                    SELECT * FROM warehouse
                    WHERE (name LIKE ? OR code LIKE ?)
                    ORDER BY
                        CASE WHEN ? = 'name' THEN name END ASC,
                        CASE WHEN ? = 'code' THEN code END ASC,
                        warehouse_id DESC
                    LIMIT ? OFFSET ?;
                """;

        var offset = (page - 1) * size;

        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, search);
            statement.setString(2, search);

            statement.setString(3, sort);
            statement.setString(4, sort);

            statement.setLong(5, size);
            statement.setLong(6, offset);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    list.add(mapResultSetToWarehouse(result));
                }
            }
        }

        return list;
    }

    public Long getPageCount(String search) throws SQLException {
        String query = """
                    SELECT COUNT(*) FROM warehouse
                    WHERE (name LIKE ? OR code LIKE ?)
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, search);
            statement.setString(2, search);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong(1);
                }
            }
        }

        return 0L;
    }

    @Override
    public Warehouse getDetail(Long id) throws SQLException {
        String query = "SELECT * FROM warehouse WHERE warehouse_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapResultSetToWarehouse(result);
                }
            }
        }
        return null;
    }

    @Override
    public boolean create(Warehouse warehouse) throws SQLException {
        String sql = """
                    INSERT INTO warehouse (code, name, address, status)
                    VALUES (?, ?, ?, ?)
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, warehouse.getCode());
            ps.setString(2, warehouse.getName());
            ps.setString(3, warehouse.getAddress());
            ps.setString(4, warehouse.getStatus() == null ? "ACTIVE" : warehouse.getStatus());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        warehouse.setWarehouseId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Warehouse warehouse) throws SQLException {
        String sql = """
                    UPDATE warehouse
                    SET code = ?, name = ?, address = ?, status = ?
                    WHERE warehouse_id = ?
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, warehouse.getCode());
            ps.setString(2, warehouse.getName());
            ps.setString(3, warehouse.getAddress());
            ps.setString(4, warehouse.getStatus());
            ps.setLong(5, warehouse.getWarehouseId());

            System.out.println("Executing UPDATE: " + sql);
            System.out.println("Parameters: code=" + warehouse.getCode() + ", name=" + warehouse.getName() 
                + ", address=" + warehouse.getAddress() + ", status=" + warehouse.getStatus() 
                + ", id=" + warehouse.getWarehouseId());

            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM warehouse WHERE warehouse_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            
            System.out.println("Executing DELETE: " + sql);
            System.out.println("Parameter: id=" + id);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
        }
    }

    public boolean codeExists(String code, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM warehouse WHERE code = ? AND (? IS NULL OR warehouse_id != ?)";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setObject(2, excludeId);
            ps.setObject(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        }
        return false;
    }

    private Warehouse mapResultSetToWarehouse(ResultSet rs) throws SQLException {
        Warehouse w = new Warehouse();
        w.setWarehouseId(rs.getLong("warehouse_id"));
        w.setCode(rs.getString("code"));
        w.setName(rs.getString("name"));
        w.setAddress(rs.getString("address"));
        w.setStatus(rs.getString("status"));

        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            w.setCreatedAt(ts.toLocalDateTime());
        }

        return w;
    }

    // Existing methods kept for backward compatibility if needed,
    // although they are now mostly covered by the new methods.

    public List<Warehouse> getAll() throws SQLException {
        return getList("%%", "name", 1L, 1000L); // Shortcut
    }

    public Warehouse getWarehouseById(Long warehouseId) throws SQLException {
        return getDetail(warehouseId);
    }
}
