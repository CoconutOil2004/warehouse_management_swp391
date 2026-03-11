package dao;

import context.DBContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO extends DBContext implements Dao<User> {

    private final Connection conn = DBContext.getConnection();
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public User login(String identity, String password) {
        String sql = """
            SELECT user_id, username, full_name, email, phone,
                   password_hash, status, warehouse_id, created_at
            FROM `user`
            WHERE (username = ? OR email = ?)
              AND status = 'ACTIVE'
              AND is_deleted = 0
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identity);
            ps.setString(2, identity);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    String hash = rs.getString("password_hash");

                    // So sánh password (BCrypt)
                    if (!BCrypt.checkpw(password, hash)) {
                        return null;
                    }

                    User u = new User();
                    u.setUserId(rs.getLong("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setStatus(rs.getString("status"));
                    u.setWarehouseId(rs.getLong("warehouse_id"));
                    u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getList(String search, String sort, Long page, Long size, Long roleId, String status, Boolean isDeleted, Long excludeUserId) throws SQLException {
        List<User> list = new ArrayList<>();

        String query = """
            SELECT u.*, GROUP_CONCAT(r.name SEPARATOR ', ') as role_names
            FROM user u
            LEFT JOIN user_role ur ON u.user_id = ur.user_id
            LEFT JOIN role r ON ur.role_id = r.role_id
            WHERE (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?)
              AND (? IS NULL OR u.user_id IN (SELECT ur2.user_id FROM user_role ur2 WHERE ur2.role_id = ?))
              AND (? IS NULL OR u.status = ?)
              AND (? IS NULL OR u.is_deleted = ?)
              AND (? IS NULL OR u.user_id != ?)
            GROUP BY u.user_id
            ORDER BY
                u.is_deleted ASC,
                CASE WHEN ? = 'username' THEN u.username END ASC,
                CASE WHEN ? = 'full_name' THEN u.full_name END ASC,
                CASE WHEN ? = 'email' THEN u.email END ASC,
                CASE WHEN ? = 'user_id' OR ? IS NULL OR ? = '' THEN u.user_id END DESC
            LIMIT ? OFFSET ?;
        """;

        PreparedStatement statement = conn.prepareStatement(query);
        var offset = (page - 1) * size;
        this.prepare(statement, search, search, search, roleId, roleId, status, status, isDeleted, isDeleted, excludeUserId, excludeUserId, sort, sort, sort, sort, sort, sort, size, offset);

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            User user = mapResultSetToUser(result);
            list.add(user);
        }

        return list;
    }

    public Long getPageCount(String search, Long roleId, String status, Boolean isDeleted, Long excludeUserId) throws SQLException {
        String query = """
            SELECT COUNT(DISTINCT u.user_id) FROM user u
            LEFT JOIN user_role ur ON u.user_id = ur.user_id
            WHERE (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?)
              AND (? IS NULL OR ur.role_id = ?)
              AND (? IS NULL OR u.status = ?)
              AND (? IS NULL OR u.is_deleted = ?)
              AND (? IS NULL OR u.user_id != ?)
        """;

        PreparedStatement statement = conn.prepareStatement(query);
        this.prepare(statement, search, search, search, roleId, roleId, status, status, isDeleted, isDeleted, excludeUserId, excludeUserId);

        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getLong(1);
        }

        return 0L;
    }

    /** Get users that have the given role name (e.g. WAREHOUSE_STAFF). */
    public List<User> getUsersByRole(String roleName) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT u.*, (SELECT GROUP_CONCAT(r2.name SEPARATOR ', ') FROM user_role ur2 JOIN role r2 ON ur2.role_id = r2.role_id WHERE ur2.user_id = u.user_id) AS role_names
            FROM `user` u
            WHERE u.user_id IN (SELECT ur.user_id FROM user_role ur JOIN role r ON ur.role_id = r.role_id WHERE r.name = ?)
              AND u.is_deleted = 0 AND u.status = 'ACTIVE'
            ORDER BY u.full_name
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        }
        return list;
    }

    public User getById(Long id) throws SQLException {
        String sql = """
            SELECT u.*, GROUP_CONCAT(r.name SEPARATOR ', ') as role_names
            FROM user u
            LEFT JOIN user_role ur ON u.user_id = ur.user_id
            LEFT JOIN role r ON ur.role_id = r.role_id
            WHERE u.user_id = ?
            GROUP BY u.user_id
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public boolean create(User user) throws SQLException {
        String sql = """
            INSERT INTO user 
            (username, full_name, email, phone, password_hash, status, warehouse_id, created_by, created_at, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), 0)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getStatus());
            ps.setObject(7, user.getWarehouseId());
            ps.setObject(8, user.getCreatedBy());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(User user) throws SQLException {
        String sql = """
            UPDATE user
            SET full_name = ?, email = ?, phone = ?,
                status = ?, warehouse_id = ?
            WHERE user_id = ? AND is_deleted = 0
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getStatus());
            ps.setObject(5, user.getWarehouseId());
            ps.setLong(6, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = """
            UPDATE user
            SET is_deleted = 1
            WHERE user_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean restore(Long id) throws SQLException {
        String sql = """
            UPDATE user
            SET is_deleted = 0
            WHERE user_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public boolean usernameExists(String username, Long excludeId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM user
            WHERE username = ? AND is_deleted = 0 AND (? IS NULL OR user_id != ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
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

    public boolean emailExists(String email, Long excludeId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM user
            WHERE email = ? AND is_deleted = 0 AND (? IS NULL OR user_id != ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
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

    public List<Long> getRolesByUserId(Long userId) throws SQLException {
        String sql = """
            SELECT role_id
            FROM user_role
            WHERE user_id = ?
        """;

        List<Long> roleIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roleIds.add(rs.getLong("role_id"));
                }
            }
        }
        return roleIds;
    }

    public List<model.Role> getRolesDetailByUserId(Long userId) throws SQLException {
        String sql = """
            SELECT r.*
            FROM role r
            JOIN user_role ur ON r.role_id = ur.role_id
            WHERE ur.user_id = ?
        """;

        List<model.Role> roles = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Role role = new model.Role();
                    role.setRoleId(rs.getLong("role_id"));
                    role.setName(rs.getString("name"));
                    role.setDescription(rs.getString("description"));
                    roles.add(role);
                }
            }
        }
        return roles;
    }

    public List<model.Permission> getPermissionsDetailByUserId(Long userId) throws SQLException {
        String sql = """
            SELECT DISTINCT p.*
            FROM permission p
            JOIN role_permission rp ON p.permission_id = rp.permission_id
            JOIN user_role ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = ?
        """;

        List<model.Permission> permissions = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Permission p = new model.Permission();
                    p.setPermissionId(rs.getLong("permission_id"));
                    p.setCode(rs.getString("code"));
                    p.setName(rs.getString("name"));
                    permissions.add(p);
                }
            }
        }
        return permissions;
    }

    public boolean deleteUserRoles(Long userId) throws SQLException {
        String sql = "DELETE FROM user_role WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            return true;
        }
    }

    public boolean deleteUserRolesByRoleId(Long roleId) throws SQLException {
        String sql = "DELETE FROM user_role WHERE role_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roleId);
            ps.executeUpdate();
            return true;
        }
    }

    public void setUserRoles(Long userId, List<Long> roleIds) throws SQLException {
        // 1. Delete existing roles
        deleteUserRoles(userId);

        // 2. Insert new roles
        if (roleIds != null && !roleIds.isEmpty()) {
            String sql = "INSERT INTO user_role (user_id, role_id, assigned_at) VALUES (?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Long roleId : roleIds) {
                    ps.setLong(1, userId);
                    ps.setLong(2, roleId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setStatus(rs.getString("status"));

        Long warehouseId = rs.getObject("warehouse_id", Long.class);
        user.setWarehouseId(warehouseId);

        Long createdBy = rs.getObject("created_by", Long.class);
        user.setCreatedBy(createdBy);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
        if (lastLoginAt != null) {
            user.setLastLoginAt(lastLoginAt.toLocalDateTime());
        }

        user.setLastLoginIp(rs.getString("last_login_ip"));
        user.setIsDeleted(rs.getBoolean("is_deleted"));

        try {
            user.setRoleNames(rs.getString("role_names"));
        } catch (SQLException e) {
            // Column might not exist in some queries
        }

        return user;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = """
            SELECT u.*, GROUP_CONCAT(r.name SEPARATOR ', ') as role_names
            FROM user u
            LEFT JOIN user_role ur ON u.user_id = ur.user_id
            LEFT JOIN role r ON ur.role_id = r.role_id
            WHERE u.email = ?
              AND u.status = 'ACTIVE'
              AND u.is_deleted = 0
            GROUP BY u.user_id
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tạo OTP 6 số để reset password. Lưu SHA-256(otp) vào DB, trả otp raw để
     * gửi email.
     */
    public String createPasswordResetOtpByEmail(String email) throws SQLException {
        User u = findByEmail(email);
        if (u == null) return null;

        String otp = generateOtp6Digits();
        String tokenHash = sha256Hex(otp);
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + RESET_TOKEN_TTL.toMillis());

        String sql = """
            INSERT INTO password_reset_token (user_id, token_hash, expires_at)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, u.getUserId());
            ps.setString(2, tokenHash);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
        }

        return otp;
    }

    /**
     * Verify OTP: - token_hash match - used_at is null - expires_at > now =>
     * return user_id nếu hợp lệ
     */
    public Long verifyResetOtpAndGetUserId(String otp) throws SQLException {
        String tokenHash = sha256Hex(otp);

        String sql = """
            SELECT user_id
            FROM password_reset_token
            WHERE token_hash = ?
              AND used_at IS NULL
              AND expires_at > NOW()
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("user_id");
            }
        }
        return null;
    }

    /**
     * Đổi password_hash cho user (hash BCrypt).
     */
    public boolean updatePasswordHash(Long userId, String newHash) throws SQLException {
        String sql = "UPDATE user SET password_hash = ? WHERE user_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Mark OTP used.
     */
    public boolean markResetOtpUsed(String otp) throws SQLException {
        String tokenHash = sha256Hex(otp);

        String sql = """
            UPDATE password_reset_token
            SET used_at = NOW()
            WHERE token_hash = ?
              AND used_at IS NULL
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Optional: update last login time/ip
     */
    public boolean updateLastLogin(Long userId, String ip) throws SQLException {
        String sql = "UPDATE user SET last_login_at = NOW(), last_login_ip = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ==========================
    // HELPERS
    // ==========================
    private String generateOtp6Digits() {
        int otp = SECURE_RANDOM.nextInt(900000) + 100000; // 100000-999999
        return String.valueOf(otp);
    }

    // (để sau này muốn token dài URL-safe vẫn dùng được)
    @SuppressWarnings("unused")
    private String generateTokenUrlSafe(int numBytes) {
        byte[] bytes = new byte[numBytes];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
