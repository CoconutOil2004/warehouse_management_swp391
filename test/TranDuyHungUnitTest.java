import java.sql.Date;

import org.junit.Test;
import util.RequestUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Nhóm 1 — 3 method: {@link RequestUtil#parseInt(String, int)},
 * {@link RequestUtil#parseLong(String, long)}, {@link RequestUtil#parseSqlDate(String)}.
 * 7 testcase.
 */
public class Person1RequestUtilTest {

    /* --- parseInt (3 testcase) --- */

    @Test
    public void parseInt_nullOrBlank_returnsDefault() {
        assertEquals(10, RequestUtil.parseInt(null, 10));
        assertEquals(10, RequestUtil.parseInt("   ", 10));
    }

    @Test
    public void parseInt_valid_returnsParsed() {
        assertEquals(42, RequestUtil.parseInt("42", 0));
    }

    @Test
    public void parseInt_invalid_returnsDefault() {
        assertEquals(-1, RequestUtil.parseInt("not-a-number", -1));
    }

    /* --- parseLong (2 testcase) --- */

    @Test
    public void parseLong_null_returnsDefault() {
        assertEquals(100L, RequestUtil.parseLong(null, 100L));
    }

    @Test
    public void parseLong_validLarge_returnsParsed() {
        assertEquals(9_000_000_000L, RequestUtil.parseLong("9000000000", 0L));
    }

    /* --- parseSqlDate (2 testcase) --- */

    @Test
    public void parseSqlDate_valid_returnsDate() {
        Date d = RequestUtil.parseSqlDate("2025-06-01");
        assertEquals(Date.valueOf("2025-06-01"), d);
    }

    @Test
    public void parseSqlDate_nullBlankOrInvalid_returnsNull() {
        assertNull(RequestUtil.parseSqlDate(null));
        assertNull(RequestUtil.parseSqlDate(""));
        assertNull(RequestUtil.parseSqlDate("bad-date"));
    }
}
