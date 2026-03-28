import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;

import dto.POLineCreateDTO;
import dto.PurchaseOrderHeaderDTO;
import dto.PurchaseOrderLineDTO;
import dto.PurchaseOrderListDTO;
import dto.SaleOrderHeaderDTO;
import dto.SaleOrderLineDTO;
import dto.SaleOrderListDTO;
import model.PurchaseOrder;
import model.SalesOrder;
import model.SalesOrderLine;

import static org.junit.Assert.assertEquals;

/**
 * Purchase Order: {@link PurchaseOrder}, {@link PurchaseOrderHeaderDTO},
 * {@link PurchaseOrderListDTO}, {@link PurchaseOrderLineDTO} / {@link POLineCreateDTO} (10).
 * Sale Order: {@link SalesOrder}, {@link SaleOrderHeaderDTO}, {@link SaleOrderListDTO},
 * {@link SalesOrderLine} / {@link SaleOrderLineDTO} (10).
 */
public class TranDuyHungUnitTest {

    /* --- PurchaseOrder (model, 3 testcase) --- */

    @Test
    public void purchaseOrder_poNumberAndStatus_roundTrip() {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-2025-001");
        po.setStatus("CREATED");
        assertEquals("PO-2025-001", po.getPoNumber());
        assertEquals("CREATED", po.getStatus());
    }

    @Test
    public void purchaseOrder_datesAndNote_roundTrip() {
        PurchaseOrder po = new PurchaseOrder();
        LocalDate d = LocalDate.of(2025, 6, 15);
        LocalDateTime imported = LocalDateTime.of(2025, 6, 1, 10, 30);
        po.setExpectedDeliveryDate(d);
        po.setImportedAt(imported);
        po.setNote("Giao hàng sớm");
        assertEquals(d, po.getExpectedDeliveryDate());
        assertEquals(imported, po.getImportedAt());
        assertEquals("Giao hàng sớm", po.getNote());
    }

    @Test
    public void purchaseOrder_idsImportedByAndSourceFile_roundTrip() {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoId(9001L);
        po.setSupplierId(55L);
        po.setImportedBy(200L);
        po.setSourceFileName("import_po_march.xlsx");
        assertEquals(Long.valueOf(9001L), po.getPoId());
        assertEquals(Long.valueOf(55L), po.getSupplierId());
        assertEquals(Long.valueOf(200L), po.getImportedBy());
        assertEquals("import_po_march.xlsx", po.getSourceFileName());
    }

    /* --- PurchaseOrderHeaderDTO (3 testcase) --- */

    @Test
    public void purchaseOrderHeader_allArgsConstructor_fieldsSet() {
        Date expected = Date.valueOf("2025-07-01");
        PurchaseOrderHeaderDTO h = new PurchaseOrderHeaderDTO(
                1L,
                "PO-X",
                99L,
                "SUP01",
                "Acme Co",
                expected,
                "IMPORTED",
                "note",
                "addr",
                "0901234567",
                "a@b.com");
        assertEquals(1L, h.getPoId());
        assertEquals("PO-X", h.getPoNumber());
        assertEquals(99L, h.getSupplierId());
        assertEquals(expected, h.getExpectedDeliveryDate());
        assertEquals("IMPORTED", h.getStatus());
    }

    @Test
    public void purchaseOrderHeader_noArg_setterGetter() {
        PurchaseOrderHeaderDTO h = new PurchaseOrderHeaderDTO();
        h.setPoId(5L);
        h.setPoNumber("PO-5");
        h.setStatus("CLOSED");
        assertEquals(5L, h.getPoId());
        assertEquals("PO-5", h.getPoNumber());
        assertEquals("CLOSED", h.getStatus());
    }

    @Test
    public void purchaseOrderHeader_supplierContact_roundTrip() {
        PurchaseOrderHeaderDTO h = new PurchaseOrderHeaderDTO();
        h.setSupplierCode("S-LOC");
        h.setSupplierName("Local Supplier");
        h.setSupplierEmail("buyer@example.com");
        h.setSupplierPhone("0281234567");
        h.setSupplierAddress("Q1, TP.HCM");
        assertEquals("S-LOC", h.getSupplierCode());
        assertEquals("Local Supplier", h.getSupplierName());
        assertEquals("buyer@example.com", h.getSupplierEmail());
        assertEquals("0281234567", h.getSupplierPhone());
        assertEquals("Q1, TP.HCM", h.getSupplierAddress());
    }

    /* --- PurchaseOrderListDTO (1 testcase) --- */

    @Test
    public void purchaseOrderList_supplierAndImportMeta_roundTrip() {
        PurchaseOrderListDTO row = new PurchaseOrderListDTO();
        row.setPoId(10L);
        row.setPoNumber("PO-LIST-10");
        row.setSupplierName("Vendor A");
        row.setImportedByUsername("importer1");
        LocalDateTime at = LocalDateTime.of(2025, 3, 1, 8, 0);
        row.setImportedAt(at);
        assertEquals(Long.valueOf(10L), row.getPoId());
        assertEquals("PO-LIST-10", row.getPoNumber());
        assertEquals("Vendor A", row.getSupplierName());
        assertEquals("importer1", row.getImportedByUsername());
        assertEquals(at, row.getImportedAt());
    }

    /* --- PurchaseOrderLineDTO + POLineCreateDTO (3 testcase) --- */

    @Test
    public void purchaseOrderLine_bigDecimalAmounts_roundTrip() {
        PurchaseOrderLineDTO line = new PurchaseOrderLineDTO();
        line.setPoLineId(100L);
        line.setVariantSku("SKU-RED-M");
        line.setOrderedQty(new BigDecimal("12.5"));
        line.setUnitPrice(new BigDecimal("100000"));
        line.setLineAmount(new BigDecimal("1250000.0"));
        assertEquals(100L, line.getPoLineId());
        assertEquals("SKU-RED-M", line.getVariantSku());
        assertEquals(new BigDecimal("12.5"), line.getOrderedQty());
        assertEquals(0, new BigDecimal("1250000.0").compareTo(line.getLineAmount()));
    }

    @Test
    public void poLineCreate_allArgsConstructor_values() {
        POLineCreateDTO line = new POLineCreateDTO(
                777L,
                new BigDecimal("3"),
                new BigDecimal("50000"),
                "VND");
        assertEquals(777L, line.getVariantId());
        assertEquals(new BigDecimal("3"), line.getQty());
        assertEquals(new BigDecimal("50000"), line.getUnitPrice());
        assertEquals("VND", line.getCurrency());
    }

    @Test
    public void purchaseOrderLine_productAndVariantDisplay_roundTrip() {
        PurchaseOrderLineDTO line = new PurchaseOrderLineDTO();
        line.setProductId(12L);
        line.setProductName("Áo thun");
        line.setVariantId(340L);
        line.setColor("Đỏ");
        line.setSize("M");
        line.setBarcode("8930123456789");
        line.setVariantStatus("ACTIVE");
        assertEquals(12L, line.getProductId());
        assertEquals("Áo thun", line.getProductName());
        assertEquals(340L, line.getVariantId());
        assertEquals("Đỏ", line.getColor());
        assertEquals("M", line.getSize());
        assertEquals("8930123456789", line.getBarcode());
        assertEquals("ACTIVE", line.getVariantStatus());
    }

    /* --- Sale Order (SO): model + DTO (10 testcase) --- */

    @Test
    public void salesOrder_soNumberAndStatus_roundTrip() {
        SalesOrder so = new SalesOrder();
        so.setSoNumber("SO-2025-0100");
        so.setStatus("CREATED");
        assertEquals("SO-2025-0100", so.getSoNumber());
        assertEquals("CREATED", so.getStatus());
    }

    @Test
    public void salesOrder_shipDateAddressAndNote() {
        SalesOrder so = new SalesOrder();
        LocalDate ship = LocalDate.of(2025, 7, 20);
        so.setRequestedShipDate(ship);
        so.setShipToAddress("78 Lê Lợi, Q1");
        so.setNote("Giao trong giờ hành chính");
        assertEquals(ship, so.getRequestedShipDate());
        assertEquals("78 Lê Lợi, Q1", so.getShipToAddress());
        assertEquals("Giao trong giờ hành chính", so.getNote());
    }

    @Test
    public void salesOrder_customerAndImportMeta() {
        SalesOrder so = new SalesOrder();
        so.setSoId(5000L);
        so.setCustomerId(88L);
        so.setImportedBy(3L);
        LocalDateTime at = LocalDateTime.of(2025, 4, 1, 11, 0);
        so.setImportedAt(at);
        assertEquals(Long.valueOf(5000L), so.getSoId());
        assertEquals(Long.valueOf(88L), so.getCustomerId());
        assertEquals(Long.valueOf(3L), so.getImportedBy());
        assertEquals(at, so.getImportedAt());
    }

    @Test
    public void saleOrderHeader_customerContact_roundTrip() {
        SaleOrderHeaderDTO h = new SaleOrderHeaderDTO();
        h.setCustomerCode("CUS-01");
        h.setCustomerName("Khách VIP");
        h.setCustomerEmail("vip@mail.com");
        h.setCustomerPhone("0909888777");
        h.setCustomerAddress("Hà Nội");
        assertEquals("CUS-01", h.getCustomerCode());
        assertEquals("Khách VIP", h.getCustomerName());
        assertEquals("vip@mail.com", h.getCustomerEmail());
        assertEquals("0909888777", h.getCustomerPhone());
        assertEquals("Hà Nội", h.getCustomerAddress());
    }

    @Test
    public void saleOrderHeader_shipStatusAndIds() {
        SaleOrderHeaderDTO h = new SaleOrderHeaderDTO();
        h.setSoId(10L);
        h.setSoNumber("SO-X");
        h.setCustomerId(20L);
        h.setShipToAddress("Kho Q7");
        LocalDate d = LocalDate.of(2025, 8, 1);
        h.setRequestedShipDate(d);
        h.setStatus("CONFIRMED");
        assertEquals(Long.valueOf(10L), h.getSoId());
        assertEquals("SO-X", h.getSoNumber());
        assertEquals(Long.valueOf(20L), h.getCustomerId());
        assertEquals("Kho Q7", h.getShipToAddress());
        assertEquals(d, h.getRequestedShipDate());
        assertEquals("CONFIRMED", h.getStatus());
    }

    @Test
    public void saleOrderHeader_importAudit() {
        SaleOrderHeaderDTO h = new SaleOrderHeaderDTO();
        h.setImportedBy(99L);
        h.setImportedByUsername("import_user");
        LocalDateTime at = LocalDateTime.of(2025, 5, 5, 15, 45);
        h.setImportedAt(at);
        assertEquals(Long.valueOf(99L), h.getImportedBy());
        assertEquals("import_user", h.getImportedByUsername());
        assertEquals(at, h.getImportedAt());
    }

    @Test
    public void saleOrderList_row_roundTrip() {
        SaleOrderListDTO row = new SaleOrderListDTO();
        LocalDate d = LocalDate.of(2025, 6, 10);
        row.setSoId(1L);
        row.setSoNumber("SO-L-1");
        row.setCustomerName("CTCP ABC");
        row.setRequestedShipDate(d);
        row.setShipToAddress("Đà Nẵng");
        row.setStatus("IMPORTED");
        row.setImportedByUsername("staff_so");
        assertEquals(Long.valueOf(1L), row.getSoId());
        assertEquals("SO-L-1", row.getSoNumber());
        assertEquals("CTCP ABC", row.getCustomerName());
        assertEquals(d, row.getRequestedShipDate());
        assertEquals("Đà Nẵng", row.getShipToAddress());
        assertEquals("IMPORTED", row.getStatus());
        assertEquals("staff_so", row.getImportedByUsername());
    }

    @Test
    public void salesOrderLine_qtyPriceDiscount_model() {
        SalesOrderLine line = new SalesOrderLine();
        line.setSoLineId(700L);
        line.setSoId(50L);
        line.setVariantId(600L);
        line.setQtyOrdered(new BigDecimal("12"));
        line.setUnitPrice(new BigDecimal("89000"));
        line.setDiscount(new BigDecimal("1000"));
        assertEquals(Long.valueOf(700L), line.getSoLineId());
        assertEquals(Long.valueOf(50L), line.getSoId());
        assertEquals(Long.valueOf(600L), line.getVariantId());
        assertEquals(0, new BigDecimal("12").compareTo(line.getQtyOrdered()));
        assertEquals(0, new BigDecimal("89000").compareTo(line.getUnitPrice()));
        assertEquals(0, new BigDecimal("1000").compareTo(line.getDiscount()));
    }

    @Test
    public void saleOrderLineDTO_productVariantDisplay() {
        SaleOrderLineDTO line = new SaleOrderLineDTO();
        line.setSoLineId(1L);
        line.setSoId(2L);
        line.setVariantId(3L);
        line.setVariantSku("V-SKU-9");
        line.setProductId(4L);
        line.setProductSku("P-SKU-9");
        line.setProductName("Quần jean");
        line.setColor("Xanh");
        line.setSize("32");
        line.setBarcode("893000111");
        assertEquals(Long.valueOf(1L), line.getSoLineId());
        assertEquals(Long.valueOf(2L), line.getSoId());
        assertEquals(Long.valueOf(3L), line.getVariantId());
        assertEquals("V-SKU-9", line.getVariantSku());
        assertEquals(Long.valueOf(4L), line.getProductId());
        assertEquals("P-SKU-9", line.getProductSku());
        assertEquals("Quần jean", line.getProductName());
        assertEquals("Xanh", line.getColor());
        assertEquals("32", line.getSize());
        assertEquals("893000111", line.getBarcode());
    }

    @Test
    public void saleOrderLineDTO_amounts() {
        SaleOrderLineDTO line = new SaleOrderLineDTO();
        line.setOrderedQty(new BigDecimal("5.5"));
        line.setUnitPrice(new BigDecimal("200000"));
        line.setDiscount(new BigDecimal("5000"));
        assertEquals(0, new BigDecimal("5.5").compareTo(line.getOrderedQty()));
        assertEquals(0, new BigDecimal("200000").compareTo(line.getUnitPrice()));
        assertEquals(0, new BigDecimal("5000").compareTo(line.getDiscount()));
    }
}
