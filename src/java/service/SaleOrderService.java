package service;

import dao.SaleOrderDAO;
import dto.SOLineCreateDTO;
import dto.SaleOrderHeaderDTO;
import dto.SaleOrderLineDTO;
import dto.SaleOrderListDTO;
import java.sql.Date;
import java.util.List;

public class SaleOrderService {

    private final SaleOrderDAO soDao = new SaleOrderDAO();

    public List<SaleOrderListDTO> searchSalesOrders(
            String keyword,
            String status,
            Date shipFrom,
            Date shipTo,
            int limit,
            int offset) throws Exception {
        return soDao.searchSalesOrders(keyword, status, shipFrom, shipTo, limit, offset);
    }

    public int countSalesOrders(
            String keyword,
            String status,
            Date shipFrom,
            Date shipTo) throws Exception {
        return soDao.countSalesOrders(keyword, status, shipFrom, shipTo);
    }

    public boolean existsBySoNumber(String soNumber) throws Exception {
        return soDao.existsBySoNumber(soNumber);
    }

    public SaleOrderHeaderDTO getSaleOrderHeader(long soId) throws Exception {
        return soDao.getSaleOrderHeader(soId);
    }

    public List<SaleOrderLineDTO> getSaleOrderDetailLines(long soId) throws Exception {
        return soDao.getSaleOrderDetailLines(soId);
    }

    public long createManualSO(
            String soNumber,
            long customerId,
            Date requestedShipDate,
            String shipToAddress,
            long userId,
            List<SOLineCreateDTO> lines) throws Exception {
        return soDao.createManualSO(soNumber, customerId, requestedShipDate, shipToAddress, userId, lines);
    }

    /** Import from Excel: status = IMPORTED. */
    public long createImportedSO(
            String soNumber,
            long customerId,
            Date requestedShipDate,
            String shipToAddress,
            long userId,
            List<SOLineCreateDTO> lines) throws Exception {
        return soDao.createImportedSO(soNumber, customerId, requestedShipDate, shipToAddress, userId, lines);
    }

    public void updateSalesOrder(SaleOrderHeaderDTO header, List<SaleOrderLineDTO> lines, long userId) throws Exception {
        soDao.updateSalesOrderWithReservation(header, lines, userId);
    }

    public boolean deleteSalesOrder(long soId) throws Exception {
        return soDao.deleteSalesOrder(soId);
    }
}
