import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

import dto.RoleDTO;
import dto.RoleRequest;
import dto.UserDTO;
import dto.UserRequest;
import model.Permission;
import model.Role;
import model.RolePermission;
import model.User;
import model.UserRole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test — User management (10), Permission / role–permission (10).
 */
public class PhamTrungThanhUnitTest {

    /* ========== User management (10) ========== */

    @Test
    public void userManagement_username_roundTrip() {
        User u = new User();
        u.setUsername("staff_hr");
        assertEquals("staff_hr", u.getUsername());
    }

    @Test
    public void userManagement_fullName_roundTrip() {
        User u = new User();
        u.setFullName("Phạm Trung Thành");
        assertEquals("Phạm Trung Thành", u.getFullName());
    }

    @Test
    public void userManagement_emailAndPhone_roundTrip() {
        User u = new User();
        u.setEmail("user@wms.local");
        u.setPhone("0909111222");
        assertEquals("user@wms.local", u.getEmail());
        assertEquals("0909111222", u.getPhone());
    }

    @Test
    public void userManagement_passwordHash_storedNotPlainText() {
        User u = new User();
        u.setPasswordHash("$2a$10$hashedValueHere");
        assertEquals("$2a$10$hashedValueHere", u.getPasswordHash());
    }

    @Test
    public void userManagement_statusAndWarehouse_roundTrip() {
        User u = new User();
        u.setStatus("ACTIVE");
        u.setWarehouseId(3L);
        assertEquals("ACTIVE", u.getStatus());
        assertEquals(Long.valueOf(3L), u.getWarehouseId());
    }

    @Test
    public void userManagement_audit_createdAndLastLogin() {
        User u = new User();
        LocalDateTime created = LocalDateTime.of(2025, 1, 10, 8, 0);
        LocalDateTime last = LocalDateTime.of(2025, 3, 25, 18, 30);
        u.setCreatedBy(1L);
        u.setCreatedAt(created);
        u.setLastLoginAt(last);
        u.setLastLoginIp("10.0.0.5");
        assertEquals(Long.valueOf(1L), u.getCreatedBy());
        assertEquals(created, u.getCreatedAt());
        assertEquals(last, u.getLastLoginAt());
        assertEquals("10.0.0.5", u.getLastLoginIp());
    }

    @Test
    public void userManagement_softDeleteAndRoleNames() {
        User u = new User();
        u.setIsDeleted(Boolean.TRUE);
        u.setRoleNames("ADMIN,WAREHOUSE_MANAGER");
        assertTrue(Boolean.TRUE.equals(u.getIsDeleted()));
        assertEquals("ADMIN,WAREHOUSE_MANAGER", u.getRoleNames());
    }

    @Test
    public void userManagement_userDTO_builderForListOrDetail() {
        LocalDateTime at = LocalDateTime.of(2025, 2, 1, 12, 0);
        UserDTO dto = UserDTO.builder()
                .userId(50L)
                .username("nm_user")
                .fullName("Nguyễn Mẫn")
                .email("nm@co.jp")
                .phone("0912345678")
                .status("ACTIVE")
                .warehouseId(2L)
                .roleNames("PURCHASE_STAFF")
                .createdAt(at)
                .isDeleted(false)
                .build();
        assertEquals(Long.valueOf(50L), dto.getUserId());
        assertEquals("nm_user", dto.getUsername());
        assertEquals("Nguyễn Mẫn", dto.getFullName());
        assertEquals("nm@co.jp", dto.getEmail());
        assertEquals("0912345678", dto.getPhone());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(Long.valueOf(2L), dto.getWarehouseId());
        assertEquals("PURCHASE_STAFF", dto.getRoleNames());
        assertEquals(at, dto.getCreatedAt());
        assertFalse(Boolean.TRUE.equals(dto.getIsDeleted()));
    }

    @Test
    public void userManagement_userRequest_createOrUpdatePayload() {
        UserRequest req = new UserRequest();
        req.setUsername("new_staff");
        req.setFullName("Nhân viên mới");
        req.setEmail("new@wms.local");
        req.setPhone("0987654321");
        req.setPassword("plainOnlyForTransport");
        req.setStatus("ACTIVE");
        req.setWarehouseId(4L);
        req.setRoleIds(List.of(2L, 5L));
        assertEquals("new_staff", req.getUsername());
        assertEquals("Nhân viên mới", req.getFullName());
        assertEquals("new@wms.local", req.getEmail());
        assertEquals("0987654321", req.getPhone());
        assertEquals("plainOnlyForTransport", req.getPassword());
        assertEquals("ACTIVE", req.getStatus());
        assertEquals(Long.valueOf(4L), req.getWarehouseId());
        assertEquals(List.of(2L, 5L), req.getRoleIds());
    }

    @Test
    public void userManagement_userRole_assignment() {
        UserRole ur = new UserRole();
        LocalDateTime at = LocalDateTime.of(2025, 3, 1, 9, 15);
        ur.setUserId(100L);
        ur.setRoleId(7L);
        ur.setAssignedAt(at);
        assertEquals(Long.valueOf(100L), ur.getUserId());
        assertEquals(Long.valueOf(7L), ur.getRoleId());
        assertEquals(at, ur.getAssignedAt());
    }

    /* ========== Permission & role–permission (10) ========== */

    @Test
    public void permission_code_roundTrip() {
        Permission p = new Permission();
        p.setCode("PO_VIEW");
        assertEquals("PO_VIEW", p.getCode());
    }

    @Test
    public void permission_name_roundTrip() {
        Permission p = new Permission();
        p.setName("Xem đơn mua hàng");
        assertEquals("Xem đơn mua hàng", p.getName());
    }

    @Test
    public void permission_permissionId_roundTrip() {
        Permission p = new Permission();
        p.setPermissionId(400L);
        assertEquals(Long.valueOf(400L), p.getPermissionId());
    }

    @Test
    public void permission_allFields_roundTrip() {
        Permission p = new Permission();
        p.setPermissionId(1L);
        p.setCode("USER_MANAGE");
        p.setName("Quản lý người dùng");
        assertEquals(Long.valueOf(1L), p.getPermissionId());
        assertEquals("USER_MANAGE", p.getCode());
        assertEquals("Quản lý người dùng", p.getName());
    }

    @Test
    public void permission_role_nameAndDescription() {
        Role r = new Role();
        r.setName("Kho xưởng");
        r.setDescription("Quyền thao tác kho");
        assertEquals("Kho xưởng", r.getName());
        assertEquals("Quyền thao tác kho", r.getDescription());
    }

    @Test
    public void permission_role_allArgsConstructor() {
        Role r = new Role(99L, "ADMIN", "Toàn quyền hệ thống");
        assertEquals(Long.valueOf(99L), r.getRoleId());
        assertEquals("ADMIN", r.getName());
        assertEquals("Toàn quyền hệ thống", r.getDescription());
    }

    @Test
    public void permission_roleDTO_builder() {
        RoleDTO dto = RoleDTO.builder()
                .roleId(3L)
                .name("SALE_STAFF")
                .description("Bán hàng")
                .build();
        assertEquals(Long.valueOf(3L), dto.getRoleId());
        assertEquals("SALE_STAFF", dto.getName());
        assertEquals("Bán hàng", dto.getDescription());
    }

    @Test
    public void permission_roleRequest_metaFields() {
        RoleRequest req = new RoleRequest();
        req.setName("CUSTOM_ROLE");
        req.setDescription("Vai trò tuỳ chỉnh");
        assertEquals("CUSTOM_ROLE", req.getName());
        assertEquals("Vai trò tuỳ chỉnh", req.getDescription());
    }

    @Test
    public void permission_roleRequest_permissionIds_forGrant() {
        RoleRequest req = new RoleRequest();
        req.setPermissionIds(List.of(10L, 20L, 30L));
        assertNotNull(req.getPermissionIds());
        assertEquals(3, req.getPermissionIds().size());
        assertEquals(Long.valueOf(20L), req.getPermissionIds().get(1));
    }

    @Test
    public void permission_rolePermission_mappingRow() {
        RolePermission rp = new RolePermission();
        rp.setRoleId(5L);
        rp.setPermissionId(101L);
        assertEquals(Long.valueOf(5L), rp.getRoleId());
        assertEquals(Long.valueOf(101L), rp.getPermissionId());
    }
}
