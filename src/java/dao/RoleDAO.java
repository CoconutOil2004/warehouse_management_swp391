package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import context.DBContext;
import model.Role;

public class RoleDAO extends DBContext implements Dao<Role> {

    private final Connection conn = DBContext.getConnection();

    public List<Role> getList(String search, String sort, Long page, Long size) throws SQLException {
        List<Role> list = new ArrayList<>();

        String query = """
                    SELECT * FROM role
                    WHERE (name LIKE ? OR description LIKE ?)
                    ORDER BY
                        CASE WHEN ? = 'name' THEN name END ASC,
                        CASE WHEN ? = 'description' THEN description END ASC
                    LIMIT ? OFFSET ?;
                """;

        PreparedStatement statement = conn.prepareStatement(query);
        var offset = (page - 1) * size;
        this.prepare(statement, search, search, sort, sort, size, offset);

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            Role role = new Role();
            role.setRoleId(result.getLong("role_id"));
            role.setName(result.getString("name"));
            role.setDescription(result.getString("description"));
            list.add(role);
        }

        return list;
    }

    public Long getPageCount(String search) throws SQLException {
        String query = """
                    SELECT COUNT(*) FROM role
                    WHERE (name LIKE ? OR description LIKE ?)
                """;

        PreparedStatement statement = conn.prepareStatement(query);
        this.prepare(statement, search, search);

        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getLong(1);
        }

        return 0L;
    }

    public Role getById(Long id) throws SQLException {
        String sql = """
                    SELECT role_id, name, description
                    FROM role
                    WHERE role_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role();
                    role.setRoleId(rs.getLong("role_id"));
                    role.setName(rs.getString("name"));
                    role.setDescription(rs.getString("description"));
                    return role;
                }
            }
        }
        return null;
    }

    public boolean create(Role role) throws SQLException {
        String sql = """
                    INSERT INTO role (name, description)
                    VALUES (?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, role.getName());
            ps.setString(2, role.getDescription());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        role.setRoleId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Role role) throws SQLException {
        String sql = """
                    UPDATE role
                    SET name = ?, description = ?
                    WHERE role_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.getName());
            ps.setString(2, role.getDescription());
            ps.setLong(3, role.getRoleId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        // 1. Delete associated permissions
        deleteRolePermissions(id);

        // 2. Delete associated user roles
        UserDAO userDAO = new UserDAO();
        userDAO.deleteUserRolesByRoleId(id);

        // 3. Delete the role itself
        String sql = """
                    DELETE FROM role
                    WHERE role_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM role";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public boolean nameExists(String name, Long excludeId) throws SQLException {
        String sql = """
                    SELECT COUNT(*) FROM role
                    WHERE name = ? AND (? IS NULL OR role_id != ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
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

    public List<Long> getPermissionsByRoleId(Long roleId) throws SQLException {
        String sql = """
                    SELECT permission_id
                    FROM role_permission
                    WHERE role_id = ?
                """;

        List<Long> permissionIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissionIds.add(rs.getLong("permission_id"));
                }
            }
        }
        return permissionIds;
    }

    public List<model.Permission> getPermissionsDetailByRoleId(Long roleId) throws SQLException {
        String sql = """
                    SELECT p.*
                    FROM permission p
                    JOIN role_permission rp ON p.permission_id = rp.permission_id
                    WHERE rp.role_id = ?
                """;

        List<model.Permission> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Permission p = new model.Permission();
                    p.setPermissionId(rs.getLong("permission_id"));
                    p.setCode(rs.getString("code"));
                    p.setName(rs.getString("name"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean deleteRolePermissions(Long roleId) throws SQLException {
        String sql = """
                    DELETE FROM role_permission
                    WHERE role_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roleId);
            ps.executeUpdate();
            return true;
        }
    }

    public void setRolePermissions(Long roleId, List<Long> permissionIds) throws SQLException {
        // 1. Delete existing permissions
        deleteRolePermissions(roleId);

        // 2. Insert new permissions
        if (permissionIds != null && !permissionIds.isEmpty()) {
            String sql = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Long permissionId : permissionIds) {
                    ps.setLong(1, roleId);
                    ps.setLong(2, permissionId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public List<Role> getAll() throws SQLException {
        List<Role> list = new ArrayList<>();
        String query = "SELECT * FROM role ORDER BY name ASC";
        try (PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getLong("role_id"));
                role.setName(rs.getString("name"));
                role.setDescription(rs.getString("description"));
                list.add(role);
            }
        }
        return list;
    }
}
