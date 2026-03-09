package dao;

import context.DBContext;
import dto.CustomerDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Customer;

public class CustomerDAO extends DBContext implements Dao<Customer> {

    public List<CustomerDTO> getActiveCustomers() throws SQLException {
        List<CustomerDTO> list = new ArrayList<>();
        String sql = "SELECT customer_id, code, name FROM customer WHERE status = 'ACTIVE' ORDER BY name";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CustomerDTO c = new CustomerDTO();
                c.setCustomerId(rs.getLong("customer_id"));
                c.setCode(rs.getString("code"));
                c.setName(rs.getString("name"));
                list.add(c);
            }
        }
        return list;
    }

    public List<Customer> getList(String search, String sort, Long page, Long size) throws SQLException {
        List<Customer> list = new ArrayList<>();

        String query = """
                    SELECT * FROM customer
                    WHERE (name LIKE ? OR code LIKE ? OR email LIKE ? OR phone LIKE ?)
                    ORDER BY
                        CASE WHEN ? = 'name' THEN name END ASC,
                        CASE WHEN ? = 'code' THEN code END ASC,
                        customer_id DESC
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
                    list.add(mapResultSetToCustomer(result));
                }
            }
        }

        return list;
    }

    public Long getPageCount(String search) throws SQLException {
        String query = """
                    SELECT COUNT(*) FROM customer
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
    public Customer getDetail(Long id) throws SQLException {
        String query = "SELECT * FROM customer WHERE customer_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapResultSetToCustomer(result);
                }
            }
        }
        return null;
    }

    /**
     * Returns customer_id for the given code, or null if not found.
     */
    public Long findIdByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return null;
        }
        String sql = "SELECT customer_id FROM customer WHERE code = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("customer_id");
                }
            }
        }
        return null;
    }

    @Override
    public boolean create(Customer customer) throws SQLException {
        String sql = """
                    INSERT INTO customer (code, name, email, phone, address, status)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getCode());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getStatus() == null ? "ACTIVE" : customer.getStatus());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setCustomerId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Customer customer) throws SQLException {
        String sql = """
                    UPDATE customer
                    SET code = ?, name = ?, email = ?, phone = ?, address = ?, status = ?
                    WHERE customer_id = ?
                """;

        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customer.getCode());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getStatus());
            ps.setLong(7, customer.getCustomerId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean codeExists(String code, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WHERE code = ? AND (? IS NULL OR customer_id != ?)";
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

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
