import java.time.LocalDateTime;

import org.junit.Test;

import dto.UserDTO;
import model.Supplier;
import model.User;
import util.PasswordUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test — Supplier (10) và Login (10): model/DTO liên quan xác thực + {@link PasswordUtil}.
 */
public class LuongHuyenChauAnhUnitTest {

    /* ========== Supplier (10) ========== */

    @Test
    public void supplier_code_roundTrip() {
        Supplier s = new Supplier();
        s.setCode("SUP-01");
        assertEquals("SUP-01", s.getCode());
    }

    @Test
    public void supplier_name_roundTrip() {
        Supplier s = new Supplier();
        s.setName("Công ty ABC");
        assertEquals("Công ty ABC", s.getName());
    }

    @Test
    public void supplier_email_roundTrip() {
        Supplier s = new Supplier();
        s.setEmail("contact@supplier.vn");
        assertEquals("contact@supplier.vn", s.getEmail());
    }

    @Test
    public void supplier_phone_roundTrip() {
        Supplier s = new Supplier();
        s.setPhone("0909123456");
        assertEquals("0909123456", s.getPhone());
    }

    @Test
    public void supplier_address_roundTrip() {
        Supplier s = new Supplier();
        s.setAddress("123 Nguyễn Huệ, Q1");
        assertEquals("123 Nguyễn Huệ, Q1", s.getAddress());
    }

    @Test
    public void supplier_status_roundTrip() {
        Supplier s = new Supplier();
        s.setStatus("ACTIVE");
        assertEquals("ACTIVE", s.getStatus());
    }

    @Test
    public void supplier_supplierId_roundTrip() {
        Supplier s = new Supplier();
        s.setSupplierId(42L);
        assertEquals(Long.valueOf(42L), s.getSupplierId());
    }

    @Test
    public void supplier_codeNameStatus_together() {
        Supplier s = new Supplier();
        s.setCode("V-99");
        s.setName("Vendor 99");
        s.setStatus("INACTIVE");
        assertEquals("V-99", s.getCode());
        assertEquals("Vendor 99", s.getName());
        assertEquals("INACTIVE", s.getStatus());
    }

    @Test
    public void supplier_contact_emailAndPhone() {
        Supplier s = new Supplier();
        s.setEmail("sales@vendor.com");
        s.setPhone("+84 28 3822 1111");
        assertEquals("sales@vendor.com", s.getEmail());
        assertEquals("+84 28 3822 1111", s.getPhone());
    }

    @Test
    public void supplier_allFields_roundTrip() {
        Supplier s = new Supplier();
        s.setSupplierId(1000L);
        s.setCode("FULL-1");
        s.setName("Full Supplier");
        s.setEmail("e@x.com");
        s.setPhone("1");
        s.setAddress("Addr");
        s.setStatus("ACTIVE");
        assertEquals(Long.valueOf(1000L), s.getSupplierId());
        assertEquals("FULL-1", s.getCode());
        assertEquals("Full Supplier", s.getName());
        assertEquals("e@x.com", s.getEmail());
        assertEquals("1", s.getPhone());
        assertEquals("Addr", s.getAddress());
        assertEquals("ACTIVE", s.getStatus());
    }

    /* ========== Login (10) ========== */

    @Test
    public void login_hashPassword_returnsNonEmpty() {
        assertNotNull(PasswordUtil.hashPassword("secret123"));
        assertFalse(PasswordUtil.hashPassword("x").isEmpty());
    }

    @Test
    public void login_hashPassword_usesBcryptPrefix() {
        String h = PasswordUtil.hashPassword("mypass");
        assertTrue(h.startsWith("$2a$") || h.startsWith("$2b$"));
    }

    @Test
    public void login_verifyPassword_correct_returnsTrue() {
        String raw = "Passw0rd!";
        String hash = PasswordUtil.hashPassword(raw);
        assertTrue(PasswordUtil.verifyPassword(raw, hash));
    }

    @Test
    public void login_verifyPassword_wrong_returnsFalse() {
        String hash = PasswordUtil.hashPassword("only-one");
        assertFalse(PasswordUtil.verifyPassword("other-one", hash));
    }

    @Test
    public void login_user_usernameAndPasswordHash_roundTrip() {
        User u = new User();
        u.setUsername("staff01");
        u.setPasswordHash("$2a$10dummyhash");
        assertEquals("staff01", u.getUsername());
        assertEquals("$2a$10dummyhash", u.getPasswordHash());
    }

    @Test
    public void login_user_emailAsIdentity_roundTrip() {
        User u = new User();
        u.setEmail("user@company.com");
        assertEquals("user@company.com", u.getEmail());
    }

    @Test
    public void login_user_activeStatus_expectedForSession() {
        User u = new User();
        u.setStatus("ACTIVE");
        assertEquals("ACTIVE", u.getStatus());
    }

    @Test
    public void login_user_lastLoginAudit_fields() {
        User u = new User();
        LocalDateTime at = LocalDateTime.of(2025, 3, 25, 14, 0, 0);
        u.setLastLoginAt(at);
        u.setLastLoginIp("192.168.1.10");
        assertEquals(at, u.getLastLoginAt());
        assertEquals("192.168.1.10", u.getLastLoginIp());
    }

    @Test
    public void login_userDTO_builder_forAuthenticatedPrincipal() {
        UserDTO dto = UserDTO.builder()
                .userId(7L)
                .username("lelogin")
                .fullName("Lê Đăng Nhập")
                .email("le@mail.com")
                .status("ACTIVE")
                .roleNames("WAREHOUSE_STAFF")
                .build();
        assertEquals(Long.valueOf(7L), dto.getUserId());
        assertEquals("lelogin", dto.getUsername());
        assertEquals("Lê Đăng Nhập", dto.getFullName());
        assertEquals("le@mail.com", dto.getEmail());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("WAREHOUSE_STAFF", dto.getRoleNames());
    }

    @Test
    public void login_user_roleNames_afterAuth() {
        User u = new User();
        u.setRoleNames("ADMIN,PURCHASE_STAFF");
        assertEquals("ADMIN,PURCHASE_STAFF", u.getRoleNames());
    }
}
