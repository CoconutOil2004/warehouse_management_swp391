import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Test;

import model.GoodsDeliveryLine;
import model.GoodsDeliveryNote;
import model.Slot;
import model.Zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Unit test — Zone (10), Slot (10), phiếu xuất kho {@link GoodsDeliveryNote} / {@link GoodsDeliveryLine} (10).
 */
public class PhamNhatNamUnitTest {

    /* ========== Zone (10) ========== */

    @Test
    public void zone_zoneId_roundTrip() {
        Zone z = new Zone();
        z.setZoneId(501L);
        assertEquals(Long.valueOf(501L), z.getZoneId());
    }

    @Test
    public void zone_warehouseId_roundTrip() {
        Zone z = new Zone();
        z.setWarehouseId(3L);
        assertEquals(Long.valueOf(3L), z.getWarehouseId());
    }

    @Test
    public void zone_code_roundTrip() {
        Zone z = new Zone();
        z.setCode("A-RECV");
        assertEquals("A-RECV", z.getCode());
    }

    @Test
    public void zone_name_roundTrip() {
        Zone z = new Zone();
        z.setName("Khu tiếp nhận");
        assertEquals("Khu tiếp nhận", z.getName());
    }

    @Test
    public void zone_zoneType_roundTrip() {
        Zone z = new Zone();
        z.setZoneType("PICK_FACE");
        assertEquals("PICK_FACE", z.getZoneType());
    }

    @Test
    public void zone_status_roundTrip() {
        Zone z = new Zone();
        z.setStatus("ACTIVE");
        assertEquals("ACTIVE", z.getStatus());
    }

    @Test
    public void zone_codeNameWarehouse_together() {
        Zone z = new Zone();
        z.setWarehouseId(1L);
        z.setCode("B-STORAGE");
        z.setName("Lưu trữ B");
        assertEquals(Long.valueOf(1L), z.getWarehouseId());
        assertEquals("B-STORAGE", z.getCode());
        assertEquals("Lưu trữ B", z.getName());
    }

    @Test
    public void zone_typeAndStatus_together() {
        Zone z = new Zone();
        z.setZoneType("RESERVE");
        z.setStatus("INACTIVE");
        assertEquals("RESERVE", z.getZoneType());
        assertEquals("INACTIVE", z.getStatus());
    }

    @Test
    public void zone_defaults_beforeSet() {
        Zone z = new Zone();
        assertNull(z.getZoneId());
        assertNull(z.getCode());
    }

    @Test
    public void zone_allCoreFields_roundTrip() {
        Zone z = new Zone();
        z.setZoneId(99L);
        z.setWarehouseId(2L);
        z.setCode("Z-99");
        z.setName("Full zone");
        z.setZoneType("BULK");
        z.setStatus("ACTIVE");
        assertEquals(Long.valueOf(99L), z.getZoneId());
        assertEquals(Long.valueOf(2L), z.getWarehouseId());
        assertEquals("Z-99", z.getCode());
        assertEquals("Full zone", z.getName());
        assertEquals("BULK", z.getZoneType());
        assertEquals("ACTIVE", z.getStatus());
    }

    /* ========== Slot (10) ========== */

    @Test
    public void slot_slotId_roundTrip() {
        Slot s = new Slot();
        s.setSlotId(8001L);
        assertEquals(Long.valueOf(8001L), s.getSlotId());
    }

    @Test
    public void slot_zoneId_roundTrip() {
        Slot s = new Slot();
        s.setZoneId(501L);
        assertEquals(Long.valueOf(501L), s.getZoneId());
    }

    @Test
    public void slot_code_roundTrip() {
        Slot s = new Slot();
        s.setCode("A-01-02");
        assertEquals("A-01-02", s.getCode());
    }

    @Test
    public void slot_rowAndCol_roundTrip() {
        Slot s = new Slot();
        s.setRowNo(4);
        s.setColNo(7);
        assertEquals(Integer.valueOf(4), s.getRowNo());
        assertEquals(Integer.valueOf(7), s.getColNo());
    }

    @Test
    public void slot_maxCapacity_roundTrip() {
        Slot s = new Slot();
        BigDecimal cap = new BigDecimal("120.50");
        s.setMaxCapacity(cap);
        assertSame(cap, s.getMaxCapacity());
    }

    @Test
    public void slot_capacityUom_roundTrip() {
        Slot s = new Slot();
        s.setCapacityUom("PALLET");
        assertEquals("PALLET", s.getCapacityUom());
    }

    @Test
    public void slot_status_roundTrip() {
        Slot s = new Slot();
        s.setStatus("ACTIVE");
        assertEquals("ACTIVE", s.getStatus());
    }

    @Test
    public void slot_locationIdentity_zoneCodeGrid() {
        Slot s = new Slot();
        s.setZoneId(10L);
        s.setCode("R01C03");
        s.setRowNo(1);
        s.setColNo(3);
        assertEquals(Long.valueOf(10L), s.getZoneId());
        assertEquals("R01C03", s.getCode());
        assertEquals(Integer.valueOf(1), s.getRowNo());
        assertEquals(Integer.valueOf(3), s.getColNo());
    }

    @Test
    public void slot_capacityWithUom() {
        Slot s = new Slot();
        s.setMaxCapacity(new BigDecimal("2"));
        s.setCapacityUom("CBM");
        assertEquals(0, new BigDecimal("2").compareTo(s.getMaxCapacity()));
        assertEquals("CBM", s.getCapacityUom());
    }

    @Test
    public void slot_allFields_roundTrip() {
        Slot s = new Slot();
        s.setSlotId(1L);
        s.setZoneId(2L);
        s.setCode("S-FULL");
        s.setRowNo(0);
        s.setColNo(0);
        s.setMaxCapacity(new BigDecimal("999"));
        s.setCapacityUom("UNIT");
        s.setStatus("BLOCKED");
        assertEquals(Long.valueOf(1L), s.getSlotId());
        assertEquals(Long.valueOf(2L), s.getZoneId());
        assertEquals("S-FULL", s.getCode());
        assertEquals(Integer.valueOf(0), s.getRowNo());
        assertEquals(Integer.valueOf(0), s.getColNo());
        assertEquals(0, new BigDecimal("999").compareTo(s.getMaxCapacity()));
        assertEquals("UNIT", s.getCapacityUom());
        assertEquals("BLOCKED", s.getStatus());
    }

    /* ========== Phiếu xuất kho — GDN / GDN Line (10) ========== */

    @Test
    public void gdn_gdnNumber_roundTrip() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        g.setGdnNumber("GDN-2025-00088");
        assertEquals("GDN-2025-00088", g.getGdnNumber());
    }

    @Test
    public void gdn_status_roundTrip() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        g.setStatus("CONFIRMED");
        assertEquals("CONFIRMED", g.getStatus());
    }

    @Test
    public void gdn_gdnType_roundTrip() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        g.setGdnType("SO_SHIPMENT");
        assertEquals("SO_SHIPMENT", g.getGdnType());
    }

    @Test
    public void gdn_warehouseAndSoLink_roundTrip() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        g.setWarehouseId(5L);
        g.setSoId(300L);
        assertEquals(Long.valueOf(5L), g.getWarehouseId());
        assertEquals(Long.valueOf(300L), g.getSoId());
    }

    @Test
    public void gdn_transferId_whenInternalTransfer() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        g.setTransferId(77L);
        assertEquals(Long.valueOf(77L), g.getTransferId());
    }

    @Test
    public void gdn_audit_createdByAndTimestamps() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        LocalDateTime c = LocalDateTime.of(2025, 3, 25, 9, 0);
        LocalDateTime f = LocalDateTime.of(2025, 3, 25, 10, 30);
        g.setCreatedBy(42L);
        g.setCreatedAt(c);
        g.setConfirmedAt(f);
        assertEquals(Long.valueOf(42L), g.getCreatedBy());
        assertEquals(c, g.getCreatedAt());
        assertEquals(f, g.getConfirmedAt());
    }

    @Test
    public void gdn_header_fullIdentity() {
        GoodsDeliveryNote g = new GoodsDeliveryNote();
        g.setGdnId(1000L);
        g.setGdnNumber("OUT-1");
        g.setWarehouseId(1L);
        g.setSoId(2L);
        g.setGdnType("SALE");
        g.setStatus("DRAFT");
        assertEquals(Long.valueOf(1000L), g.getGdnId());
        assertEquals("OUT-1", g.getGdnNumber());
        assertEquals(Long.valueOf(1L), g.getWarehouseId());
        assertEquals(Long.valueOf(2L), g.getSoId());
        assertEquals("SALE", g.getGdnType());
        assertEquals("DRAFT", g.getStatus());
    }

    @Test
    public void gdnLine_qtyRequiredAndPicked() {
        GoodsDeliveryLine line = new GoodsDeliveryLine();
        line.setQtyRequired(new BigDecimal("100"));
        line.setQtyPicked(new BigDecimal("100"));
        assertEquals(0, new BigDecimal("100").compareTo(line.getQtyRequired()));
        assertEquals(0, new BigDecimal("100").compareTo(line.getQtyPicked()));
    }

    @Test
    public void gdnLine_qtyPackedAndShipped() {
        GoodsDeliveryLine line = new GoodsDeliveryLine();
        line.setQtyPacked(new BigDecimal("50"));
        line.setQtyShipped(new BigDecimal("50"));
        assertEquals(0, new BigDecimal("50").compareTo(line.getQtyPacked()));
        assertEquals(0, new BigDecimal("50").compareTo(line.getQtyShipped()));
    }

    @Test
    public void gdnLine_links_gdnVariantSoLine() {
        GoodsDeliveryLine line = new GoodsDeliveryLine();
        line.setGdnLineId(1L);
        line.setGdnId(200L);
        line.setSoLineId(3000L);
        line.setVariantId(900L);
        line.setTransferLineId(null);
        assertEquals(Long.valueOf(1L), line.getGdnLineId());
        assertEquals(Long.valueOf(200L), line.getGdnId());
        assertEquals(Long.valueOf(3000L), line.getSoLineId());
        assertEquals(Long.valueOf(900L), line.getVariantId());
        assertNull(line.getTransferLineId());
    }
}
