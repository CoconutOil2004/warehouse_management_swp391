package dao;

import context.DBContext;
import dto.SupplierDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Supplier;

public class SupplierDAO extends DBContext implements Dao<Supplier> {

    public List<SupplierDTO> getActiveSuppliers() throws SQLException {
        List<SupplierDTO> list = new ArrayList<>();
        String sql = "SELECT supplier_id, code, name FROM supplier WHERE status = 'ACTIVE' ORDER BY name";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SupplierDTO s = new SupplierDTO();
                s.setSupplierId(rs.getLong("supplier_id"));
                s.setCode(rs.getString("code"));
                s.setName(rs.getString("name"));
                list.add(s);
            }
        }
        return list;
    }

    @Override
    public List<Supplier> getList(Object[] parameters) throws SQLException {
        // This is from Dao interface, but our controller uses a custom signature.
        // For consistency with PermissionDAO, we keep our custom signature and maybe
        // implement this if needed.
        return null;
    }

    public List<Supplier> getList(String search, String sort, Long page, Long size) throws SQLException {
        List<Supplier> list = new ArrayList<>();

        String query = """
                    SELECT * FROM supplier
                    WHERE (name LIKE ? OR code LIKE ? OR email LIKE ? OR phone LIKE ?)
                    ORDER BY
                        CASE WHEN ? = 'name' THEN name END ASC,
                        CASE WHEN ? = 'code' THEN code END ASC,
                        supplier_id DESC
                    LIMIT ? OFFSET ?;
                """;

        var offset = (page - 1) * size;

        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, search);
            statement.setString(2, search);
            statement.setString(3, search);
            statement.setString(4, search);

            statement.setString(5, sort);
            statement.setString(6, sort);

            statement.setLong(7, size);
            statement.setLong(8, offset);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    list.add(mapResultSetToSupplier(result));
                }
            }
        }

        return list;
    }

    public Long getPageCount(String search) throws SQLException {
        String query = """
                    SELECT COUNT(*) FROM supplier
                    WHERE (name LIKE ? OR code LIKE ? OR email LIKE ? OR phone LIKE ?)
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, search);
            statement.setString(2, search);
            statement.setString(3, search);
            statement.setString(4, search);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong(1);
                }
            }
        }

        return 0L;
    }

    @Override
    public Supplier getDetail(Long id) throws SQLException {
        String query = "SELECT * FROM supplier WHERE supplier_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapResultSetToSupplier(result);
                }
            }
        }
        return null;
    }

    @Override
    public boolean create(Supplier supplier) throws SQLException {
        String sql = """
                    INSERT INTO supplier (code, name, email, phone, address, status)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getCode());
            ps.setString(2, supplier.getName());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getPhone());
            ps.setString(5, supplier.getAddress());
            ps.setString(6, supplier.getStatus() == null ? "ACTIVE" : supplier.getStatus());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        supplier.setSupplierId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Supplier supplier) throws SQLException {
        String sql = """
                    UPDATE supplier
                    SET code = ?, name = ?, email = ?, phone = ?, address = ?, status = ?
                    WHERE supplier_id = ?
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, supplier.getCode());
            ps.setString(2, supplier.getName());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getPhone());
            ps.setString(5, supplier.getAddress());
            ps.setString(6, supplier.getStatus());
            ps.setLong(7, supplier.getSupplierId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "UPDATE supplier SET status = 'INACTIVE' WHERE supplier_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean activate(Long id) throws SQLException {
        String sql = "UPDATE supplier SET status = 'ACTIVE' WHERE supplier_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Long findIdByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return null;
        }
        String sql = "SELECT supplier_id FROM supplier WHERE code = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("supplier_id");
                }
            }
        }
        return null;
    }

    public boolean codeExists(String code, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM supplier WHERE code = ? AND (? IS NULL OR supplier_id != ?)";
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

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getLong("supplier_id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setStatus(rs.getString("status"));
        return s;
    }
    
    public String generateNextCode() throws SQLException {
        // Required format: SUP-0001 (incremental)
        // Also supports migrating legacy codes like SUP001 by considering both patterns.
        String sql = "SELECT code FROM supplier WHERE code IS NOT NULL ORDER BY supplier_id DESC LIMIT 200";

        int max = 0;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^SUP-?(\\d+)$");

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString("code");
                if (code == null) continue;
                code = code.trim();
                java.util.regex.Matcher m = p.matcher(code);
                if (!m.matches()) continue;
                try {
                    int n = Integer.parseInt(m.group(1));
                    if (n > max) max = n;
                } catch (NumberFormatException ignore) {
                }
            }
        }

        return String.format("SUP-%04d", max + 1);
    }
}
