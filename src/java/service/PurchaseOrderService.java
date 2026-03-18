package service;

import dao.PurchaseOrderDAO;
import dto.POLineCreateDTO;
import dto.PurchaseOrderHeaderDTO;
import dto.PurchaseOrderLineDTO;
import dto.PurchaseOrderListDTO;
import java.sql.Date;
import java.util.List;

public class PurchaseOrderService {
    private final PurchaseOrderDAO poDao;

    public PurchaseOrderService() {
        this.poDao = new PurchaseOrderDAO();
    }

    public int countPurchaseOrders(String keyword, String status, Date expectedFrom, Date expectedTo) throws Exception {
        return poDao.countPurchaseOrders(keyword, status, expectedFrom, expectedTo);
    }

    public List<PurchaseOrderListDTO> searchPurchaseOrders(String keyword, String status, Date expectedFrom, Date expectedTo, int size, int offset) throws Exception {
        return poDao.searchPurchaseOrders(keyword, status, expectedFrom, expectedTo, size, offset);
    }

    public PurchaseOrderHeaderDTO getPurchaseOrderHeader(long poId) throws Exception {
        return poDao.getPurchaseOrderHeader(poId);
    }

    public List<PurchaseOrderLineDTO> getPurchaseOrderDetailLines(long poId) throws Exception {
        return poDao.getPurchaseOrderDetailLines(poId);
    }

    public boolean existsByPoNumber(String poNumber) throws Exception {
        return poDao.existsByPoNumber(poNumber);
    }

    public void createManualPO(String poNumber, long supplierId, Date expectedDate, String note, Long userId, List<POLineCreateDTO> lines) throws Exception {
        poDao.createManualPO(poNumber, supplierId, expectedDate, note, userId, lines);
    }

    public void createImportedPO(String poNumber, long supplierId, Date expectedDate, String note, Long userId, List<POLineCreateDTO> lines) throws Exception {
        poDao.createImportedPO(poNumber, supplierId, expectedDate, note, userId, lines);
    }

    public void updatePurchaseOrder(PurchaseOrderHeaderDTO header, List<PurchaseOrderLineDTO> lines) throws Exception {
        poDao.updatePurchaseOrder(header, lines);
    }

    public boolean deletePurchaseOrder(long poId) throws Exception {
        return poDao.deletePurchaseOrder(poId);
    }

    public boolean hasAnyGrn(long poId) throws Exception {
        return poDao.hasAnyGrn(poId);
    }
}
