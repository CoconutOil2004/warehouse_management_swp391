import org.junit.Test;
import util.PasswordUtil;
import util.ValidationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Nhóm 2 — 3 method: {@link PasswordUtil#hashPassword(String)},
 * {@link PasswordUtil#verifyPassword(String, String)},
 * {@link ValidationException#ValidationException(String)} / {@link Throwable#getMessage()}.
 * 7 testcase.
 */
public class Person2PasswordAndValidationTest {

    @Test
    public void hashPassword_returnsNonNullString() {
        assertNotNull(PasswordUtil.hashPassword("abc"));
    }

    @Test
    public void hashPassword_producesLongBcryptPrefix() {
        String h = PasswordUtil.hashPassword("secret");
        assertTrue(h.length() > 20);
        assertTrue(h.startsWith("$2a$") || h.startsWith("$2b$"));
    }

    @Test
    public void verifyPassword_correct_returnsTrue() {
        String raw = "passW0rd!";
        String hash = PasswordUtil.hashPassword(raw);
        assertTrue(PasswordUtil.verifyPassword(raw, hash));
    }

    @Test
    public void verifyPassword_wrong_returnsFalse() {
        String hash = PasswordUtil.hashPassword("one");
        assertFalse(PasswordUtil.verifyPassword("two", hash));
    }

    @Test
    public void verifyPassword_sameHashTwice_stillOk() {
        String hash = PasswordUtil.hashPassword("x");
        assertTrue(PasswordUtil.verifyPassword("x", hash));
        assertTrue(PasswordUtil.verifyPassword("x", hash));
    }

    @Test
    public void validationException_storesMessage() {
        ValidationException ex = new ValidationException("Email không hợp lệ");
        assertEquals("Email không hợp lệ", ex.getMessage());
    }

    @Test
    public void validationException_isRuntimeException() {
        assertTrue(new ValidationException("x") instanceof RuntimeException);
    }
}
