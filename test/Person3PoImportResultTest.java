import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import service.PurchaseOrderImportService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Nhóm 3 — 3 method: {@link PurchaseOrderImportService.ImportResult#hasErrors()},
 * {@link PurchaseOrderImportService.ImportResult#getPoNumber()},
 * {@link PurchaseOrderImportService.ImportResult#getFieldErrors()}.
 * 7 testcase.
 */
public class Person3PoImportResultTest {

    @Test
    public void hasErrors_nullFieldErrors_returnsFalse() {
        assertFalse(new PurchaseOrderImportService.ImportResult(null, "P1").hasErrors());
    }

    @Test
    public void hasErrors_emptyMap_returnsFalse() {
        assertFalse(new PurchaseOrderImportService.ImportResult(new LinkedHashMap<>(), null).hasErrors());
    }

    @Test
    public void hasErrors_singleEntry_returnsTrue() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("poNumber", "required");
        assertTrue(new PurchaseOrderImportService.ImportResult(m, null).hasErrors());
    }

    @Test
    public void hasErrors_multipleEntries_returnsTrue() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("a", "1");
        m.put("b", "2");
        assertTrue(new PurchaseOrderImportService.ImportResult(m, null).hasErrors());
    }

    @Test
    public void getPoNumber_returnsConstructorValue() {
        assertEquals("PO-99", new PurchaseOrderImportService.ImportResult(null, "PO-99").getPoNumber());
    }

    @Test
    public void getPoNumber_mayBeNull() {
        assertEquals(null, new PurchaseOrderImportService.ImportResult(new LinkedHashMap<>(), null).getPoNumber());
    }

    @Test
    public void getFieldErrors_returnsSameMapReference() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("k", "v");
        PurchaseOrderImportService.ImportResult r = new PurchaseOrderImportService.ImportResult(m, "x");
        assertSame(m, r.getFieldErrors());
        assertNotNull(r.getFieldErrors());
        assertEquals("v", r.getFieldErrors().get("k"));
    }
}
