package service;

import dao.GoodsReceiptDAO;

public class GoodsReceiptService {
    private final GoodsReceiptDAO grnDao;

    public GoodsReceiptService() {
        this.grnDao = new GoodsReceiptDAO();
    }

    public boolean hasIncompletePutawayForPo(long poId) throws Exception {
        return grnDao.hasIncompletePutawayForPo(poId);
    }
}
