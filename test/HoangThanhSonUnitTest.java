import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Test;

import dto.GoodsReceiptListDTO;
import model.GoodsReceipt;
import model.GoodsReceiptLine;

import static org.junit.Assert.assertEquals;
/**
 * Unit test — Phiếu nhập kho (GRN): {@link GoodsReceipt}, {@link GoodsReceiptLine}, {@link GoodsReceiptListDTO}.
 */
public class HoangThanhSonUnitTest {

    /* --- GoodsReceipt header (10) --- */

    @Test
    public void grn_grnNumber_roundTrip() {
        GoodsReceipt g = new GoodsReceipt();
        g.setGrnNumber("GRN-2025-0001");
        assertEquals("GRN-2025-0001", g.getGrnNumber());
    }

    @Test
    public void grn_status_roundTrip() {
        GoodsReceipt g = new GoodsReceipt();
        g.setStatus("PENDING");
        assertEquals("PENDING", g.getStatus());
    }

    @Test
    public void grn_poLink_poIdAndPoNumber() {
        GoodsReceipt g = new GoodsReceipt();
        g.setPoId(500L);
        g.setPoNumber("PO-88");
        assertEquals(Long.valueOf(500L), g.getPoId());
        assertEquals("PO-88", g.getPoNumber());
    }

    @Test
    public void grn_supplierName_roundTrip() {
        GoodsReceipt g = new GoodsReceipt();
        g.setSupplierName("Nhà cung cấp Minh An");
        assertEquals("Nhà cung cấp Minh An", g.getSupplierName());
    }

    @Test
    public void grn_warehouseId_roundTrip() {
        GoodsReceipt g = new GoodsReceipt();
        g.setWarehouseId(2L);
        assertEquals(Long.valueOf(2L), g.getWarehouseId());
    }

    @Test
    public void grn_createdByAndCreatedAt() {
        GoodsReceipt g = new GoodsReceipt();
        LocalDateTime t = LocalDateTime.of(2025, 3, 20, 8, 30);
        g.setCreatedBy(11L);
        g.setCreatedAt(t);
        assertEquals(Long.valueOf(11L), g.getCreatedBy());
        assertEquals(t, g.getCreatedAt());
    }

    @Test
    public void grn_approvedByAndApprovedAt() {
        GoodsReceipt g = new GoodsReceipt();
        LocalDateTime t = LocalDateTime.of(2025, 3, 20, 9, 0);
        g.setApprovedBy(22L);
        g.setApprovedAt(t);
        assertEquals(Long.valueOf(22L), g.getApprovedBy());
        assertEquals(t, g.getApprovedAt());
    }

    @Test
    public void grn_delivery_receivedAtAndDeliveredBy() {
        GoodsReceipt g = new GoodsReceipt();
        LocalDateTime t = LocalDateTime.of(2025, 3, 21, 14, 0);
        g.setDeliveredBy("Tài xế A");
        g.setReceivedAt(t);
        assertEquals("Tài xế A", g.getDeliveredBy());
        assertEquals(t, g.getReceivedAt());
    }

    @Test
    public void grn_note_roundTrip() {
        GoodsReceipt g = new GoodsReceipt();
        g.setNote("Kiểm hàng đầy đủ, có niêm phong.");
        assertEquals("Kiểm hàng đầy đủ, có niêm phong.", g.getNote());
    }

    @Test
    public void grn_header_keyIdentityTogether() {
        GoodsReceipt g = new GoodsReceipt();
        g.setGrnId(900L);
        g.setGrnNumber("GRN-X");
        g.setStatus("APPROVED");
        g.setPoId(1L);
        g.setWarehouseId(3L);
        assertEquals(Long.valueOf(900L), g.getGrnId());
        assertEquals("GRN-X", g.getGrnNumber());
        assertEquals("APPROVED", g.getStatus());
        assertEquals(Long.valueOf(1L), g.getPoId());
        assertEquals(Long.valueOf(3L), g.getWarehouseId());
    }

    /* --- GoodsReceiptLine + putaway helpers (9) --- */

    @Test
    public void grnLine_ids_poAndVariantLink() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setGrnLineId(1L);
        line.setGrnId(100L);
        line.setPoLineId(200L);
        line.setVariantId(300L);
        assertEquals(Long.valueOf(1L), line.getGrnLineId());
        assertEquals(Long.valueOf(100L), line.getGrnId());
        assertEquals(Long.valueOf(200L), line.getPoLineId());
        assertEquals(Long.valueOf(300L), line.getVariantId());
    }

    @Test
    public void grnLine_skuAndProductName() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setSku("SKU-BLU-L");
        line.setProductName("Áo sơ mi");
        assertEquals("SKU-BLU-L", line.getSku());
        assertEquals("Áo sơ mi", line.getProductName());
    }

    @Test
    public void grnLine_qtyExpectedAndReceived() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setQtyExpected(new BigDecimal("100"));
        line.setQtyReceived(new BigDecimal("98"));
        assertEquals(0, new BigDecimal("100").compareTo(line.getQtyExpected()));
        assertEquals(0, new BigDecimal("98").compareTo(line.getQtyReceived()));
    }

    @Test
    public void grnLine_qtyGoodAndMissing() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setQtyGood(new BigDecimal("95"));
        line.setQtyMissing(new BigDecimal("2"));
        assertEquals(0, new BigDecimal("95").compareTo(line.getQtyGood()));
        assertEquals(0, new BigDecimal("2").compareTo(line.getQtyMissing()));
    }

    @Test
    public void grnLine_getDamagePutawayQty_sumsDamagedAndExtraDamaged() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setQtyDamaged(new BigDecimal("2"));
        line.setQtyExtraDamaged(new BigDecimal("1"));
        assertEquals(0, new BigDecimal("3").compareTo(line.getDamagePutawayQty()));
    }

    @Test
    public void grnLine_getDamagePutawayQty_nullFieldsCountAsZero() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setQtyDamaged(null);
        line.setQtyExtraDamaged(null);
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getDamagePutawayQty()));
    }

    @Test
    public void grnLine_getExcessPutawayQty_nullReturnsZero() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setQtyExtraGood(null);
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getExcessPutawayQty()));
    }

    @Test
    public void grnLine_getExcessPutawayQty_extraGoodOnly() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setQtyExtraGood(new BigDecimal("4"));
        assertEquals(0, new BigDecimal("4").compareTo(line.getExcessPutawayQty()));
    }

    @Test
    public void grnLine_unitPriceAndLineNote() {
        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setUnitPrice(new BigDecimal("150000"));
        line.setNote("Bao bì hơi móp");
        assertEquals(0, new BigDecimal("150000").compareTo(line.getUnitPrice()));
        assertEquals("Bao bì hơi móp", line.getNote());
    }

    /* --- Danh sách GRN (DTO) (1) — all-args --- */

    @Test
    public void grnListDTO_allArgsConstructor_mapsRow() {
        LocalDateTime at = LocalDateTime.of(2025, 4, 1, 10, 0);
        GoodsReceiptListDTO row = new GoodsReceiptListDTO(
                55L,
                "GRN-L-55",
                "ABC Corp",
                "DRAFT",
                "creator1",
                "PO-9",
                at);
        assertEquals(Long.valueOf(55L), row.getGrnId());
        assertEquals("GRN-L-55", row.getGrnNumber());
        assertEquals("ABC Corp", row.getSupplierName());
        assertEquals("DRAFT", row.getStatus());
        assertEquals("creator1", row.getCreatorName());
        assertEquals("PO-9", row.getPoNumber());
        assertEquals(at, row.getCreatedAt());
    }
}
