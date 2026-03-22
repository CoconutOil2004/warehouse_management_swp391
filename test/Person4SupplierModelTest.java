import org.junit.Test;
import model.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Nhóm 4 — 3 method (Lombok): {@link Supplier#getCode()}/{@link Supplier#setCode(String)},
 * {@link Supplier#getName()}/{@link Supplier#setName(String)},
 * {@link Supplier#getStatus()}/{@link Supplier#setStatus(String)}.
 * 7 testcase.
 */
public class Person4SupplierModelTest {

    @Test
    public void code_roundTrip_simple() {
        Supplier s = new Supplier();
        s.setCode("SUP-01");
        assertEquals("SUP-01", s.getCode());
    }

    @Test
    public void code_setAgain_overwrites() {
        Supplier s = new Supplier();
        s.setCode("A");
        s.setCode("B");
        assertEquals("B", s.getCode());
    }

    @Test
    public void code_mayBeEmptyString() {
        Supplier s = new Supplier();
        s.setCode("");
        assertEquals("", s.getCode());
    }

    @Test
    public void name_roundTrip() {
        Supplier s = new Supplier();
        s.setName("Công ty ABC");
        assertEquals("Công ty ABC", s.getName());
    }

    @Test
    public void name_nullUntilSet() {
        Supplier s = new Supplier();
        assertNull(s.getName());
        s.setName("X");
        assertEquals("X", s.getName());
    }

    @Test
    public void status_active() {
        Supplier s = new Supplier();
        s.setStatus("ACTIVE");
        assertEquals("ACTIVE", s.getStatus());
    }

    @Test
    public void status_inactive() {
        Supplier s = new Supplier();
        s.setStatus("INACTIVE");
        assertEquals("INACTIVE", s.getStatus());
    }
}
