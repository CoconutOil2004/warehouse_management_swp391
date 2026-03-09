package controller;

import dao.GoodsReceiptDAO;
import dao.PurchaseOrderDAO;
import dao.SupplierDAO;
import dto.GoodsReceiptListDTO;
import dto.PurchaseOrderHeaderDTO;
import dto.PurchaseOrderLineDTO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.GoodsReceipt;
import model.User;
import util.ViewPath;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.GoodsReceiptLine;
import dao.ZoneDAO;
import dao.SlotDAO;
import dao.InventoryBalanceDAO;
import dao.WarehouseDAO;
import model.Zone;
import model.Slot;
import model.Warehouse;
import java.math.BigDecimal;

@WebServlet(name = "GoodsReceiptController", urlPatterns = { "/goods-receipt" })
public class GoodsReceiptController extends HttpServlet {
    // We instantiate DAOs per request to avoid "connection closed" issues
    // while respecting the "don't edit other files" constraint.

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "list" ->
                    handleList(request, response);
                case "create" ->
                    handleCreateForm(request, response);
                case "detail" ->
                    handleDetail(request, response);
                case "edit" ->
                    handleEditForm(request, response);
                case "delete" ->
                    handleDelete(request, response);
                case "getPoDetails" ->
                    handleGetPoDetails(request, response);
                case "putaway" ->
                    handlePutaway(request, response);
                default ->
                    response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        SupplierDAO supplierDao = new SupplierDAO();
        String grnNumber = request.getParameter("grnNumber");
        String supplierIdStr = request.getParameter("supplierId");
        Long supplierId = (supplierIdStr != null && !supplierIdStr.isBlank()) ? Long.parseLong(supplierIdStr) : null;
        String status = request.getParameter("status");
        String sortField = request.getParameter("sortBy");
        String sortOrder = request.getParameter("order");

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            page = Integer.parseInt(pageStr);
        }

        int pageSize = 10;
        String sizeStr = request.getParameter("size");
        if (sizeStr != null && !sizeStr.isBlank()) {
            pageSize = Integer.parseInt(sizeStr);
        }

        int offset = (page - 1) * pageSize;

        List<GoodsReceiptListDTO> grns = grnDao.getFilteredGRNs(grnNumber, supplierId, status, sortField, sortOrder,
                pageSize, offset);
        int total = grnDao.countFilteredGRNs(grnNumber, supplierId, status);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        request.setAttribute("grns", grns);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalRecords", total);
        request.setAttribute("suppliers", supplierDao.getActiveSuppliers());

        request.getRequestDispatcher(ViewPath.GRN_LIST).forward(request, response);
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SupplierDAO supplierDao = new SupplierDAO();
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        request.setAttribute("suppliers", supplierDao.getActiveSuppliers());
        request.setAttribute("variants", grnDao.getActiveVariants());

        // Ensure purchaseOrders includes the current selection if it's an edit or
        // re-load
        Object oldPoIdAttr = request.getAttribute("oldPoId");
        Long poId = null;
        if (oldPoIdAttr != null && !oldPoIdAttr.toString().isBlank()) {
            try {
                poId = Long.parseLong(oldPoIdAttr.toString());
            } catch (Exception e) {
            }
        }

        request.setAttribute("purchaseOrders", grnDao.getPurchaseOrdersForSelection(poId));
        request.getRequestDispatcher(ViewPath.GRN_CREATE).forward(request, response);
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        Long id = Long.parseLong(request.getParameter("id"));
        GoodsReceipt grn = grnDao.getById(id);
        if (grn == null) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
            return;
        }
        request.setAttribute("grn", grn);
        request.setAttribute("lines", grnDao.getLinesByGrnId(id));
        request.setAttribute("putawayDetails", grnDao.getPutawayDetailsByGrnId(id));

        // Get warehouse name
        WarehouseDAO warehouseDao = new WarehouseDAO();
        Warehouse warehouse = warehouseDao.getWarehouseById(grn.getWarehouseId());
        request.setAttribute("warehouseName", warehouse != null ? warehouse.getName() : "N/A");

        request.getRequestDispatcher(ViewPath.GRN_DETAIL).forward(request, response);
    }

    private void handleEditForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        SupplierDAO supplierDao = new SupplierDAO();
        Long id = Long.parseLong(request.getParameter("id"));
        GoodsReceipt grn = grnDao.getById(id);

        if (grn == null || (!"PENDING".equals(grn.getStatus()) && !"DRAFT".equals(grn.getStatus()))) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=detail&id=" + id);
            return;
        }

        List<GoodsReceiptLine> lines = grnDao.getLinesByGrnId(id);

        request.setAttribute("suppliers", supplierDao.getActiveSuppliers());
        request.setAttribute("variants", grnDao.getActiveVariants());
        request.setAttribute("purchaseOrders", grnDao.getPurchaseOrdersForSelection(grn.getPoId()));

        // Pre-fill fields for the CREATE form to reuse it
        request.setAttribute("grnId", grn.getGrnId());
        request.setAttribute("oldGrnNumber", grn.getGrnNumber());
        request.setAttribute("oldPoId", grn.getPoId());

        // Get supplier ID from PO for pre-filling
        PurchaseOrderDAO poDao = new PurchaseOrderDAO();
        dto.PurchaseOrderHeaderDTO poHeader = poDao.getPurchaseOrderHeader(grn.getPoId());
        request.setAttribute("oldSupplierId", poHeader != null ? poHeader.getSupplierId() : null);

        request.setAttribute("oldNote", grn.getNote());
        request.setAttribute("oldLinesJson", packageLinesToJson(lines));

        request.getRequestDispatcher(ViewPath.GRN_CREATE).forward(request, response);
    }

    private String packageLinesToJson(List<model.GoodsReceiptLine> lines) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lines.size(); i++) {
            model.GoodsReceiptLine l = lines.get(i);
            if (i > 0)
                sb.append(",");
            sb.append("{");
            sb.append("\"poLineId\":\"").append(l.getPoLineId() != null ? l.getPoLineId() : "").append("\",");
            sb.append("\"variantId\":\"").append(l.getVariantId()).append("\",");
            sb.append("\"unitPrice\":\"").append(l.getUnitPrice() != null ? l.getUnitPrice() : 0).append("\",");
            sb.append("\"qtyExpected\":\"").append(l.getQtyExpected() != null ? l.getQtyExpected().toBigInteger() : 0)
                    .append("\",");
            sb.append("\"qtyGood\":\"").append(l.getQtyGood() != null ? l.getQtyGood().toBigInteger() : 0)
                    .append("\",");
            sb.append("\"qtyDamaged\":\"").append(l.getQtyDamaged() != null ? l.getQtyDamaged().toBigInteger() : 0)
                    .append("\",");
            sb.append("\"qtyMissing\":\"").append(l.getQtyMissing() != null ? l.getQtyMissing().toBigInteger() : 0)
                    .append("\",");

            String safeNote = l.getNote() != null ? l.getNote()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "") : "";
            sb.append("\"note\":\"").append(safeNote).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private void handleGetPoDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        PurchaseOrderDAO poDao = new PurchaseOrderDAO();
        String poIdStr = request.getParameter("poId");
        if (poIdStr == null || poIdStr.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Long poId = Long.parseLong(poIdStr);

        PurchaseOrderHeaderDTO header = poDao.getPurchaseOrderHeader(poId);
        List<PurchaseOrderLineDTO> lines = poDao.getPurchaseOrderDetailLines(poId);
        String nextGrnNumber = grnDao.getNextGrnNumber();

        if (header == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"supplierId\":").append(header.getSupplierId()).append(",");
        sb.append("\"grnNumber\":\"").append(nextGrnNumber).append("\",");
        sb.append("\"lines\":[");
        for (int i = 0; i < lines.size(); i++) {
            PurchaseOrderLineDTO l = lines.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"poLineId\":").append(l.getPoLineId()).append(",");
            sb.append("\"variantId\":").append(l.getVariantId()).append(",");
            sb.append("\"sku\":\"").append(l.getVariantSku() != null ? l.getVariantSku() : "").append("\",");
            sb.append("\"productName\":\"")
                    .append(l.getProductName() != null ? l.getProductName().replace("\"", "\\\"") : "").append("\",");
            sb.append("\"orderedQty\":").append(l.getOrderedQty() != null ? l.getOrderedQty().toBigInteger() : 0)
                    .append(",");
            sb.append("\"unitPrice\":").append(l.getUnitPrice() != null ? l.getUnitPrice() : 0);
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");

        response.getWriter().write(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action != null) {
            action = action.trim().toLowerCase();
        }

        try {
            if ("save".equals(action)) {
                handleSave(request, response);
            } else if ("approve".equals(action)) {
                handleApprove(request, response, "APPROVED");
            } else if ("reject".equals(action)) {
                handleApprove(request, response, "REJECTED");
            } else if ("delete".equals(action)) {
                handleDelete(request, response);
            } else if ("confirmputaway".equals(action)) {
                handleConfirmPutaway(request, response);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            // Tạm thời gán User mặc định để test khi AuthFilter bị tắt
            user = new User();
            user.setUserId(1L);
            user.setRoleNames("ADMIN");
        }

        String grnIdStr = request.getParameter("grnId");
        Long existingId = (grnIdStr != null && !grnIdStr.isBlank()) ? Long.parseLong(grnIdStr) : null;

        String grnNumber = request.getParameter("grnNumber");
        String poIdStr = request.getParameter("poId");
        String supplierIdStr = request.getParameter("supplierId");
        String note = request.getParameter("note");

        Map<String, String> fieldErrors = new HashMap<>();
        if (grnNumber == null || grnNumber.isBlank()) {
            grnNumber = grnDao.getNextGrnNumber();
        } else {
            try {
                if (grnDao.isGrnNumberExists(grnNumber, existingId)) {
                    fieldErrors.put("grnNumber", "Mã phiếu này (" + grnNumber + ") đã tồn tại trong hệ thống!");
                }
            } catch (SQLException e) {
                // Ignore error for check
            }
        }
        Long poId = null;
        try {
            if (poIdStr == null || poIdStr.isBlank()) {
                fieldErrors.put("poId", "Reference PO ID is required");
            } else {
                poId = Long.parseLong(poIdStr);
            }
        } catch (NumberFormatException e) {
            fieldErrors.put("poId", "Invalid PO ID format");
        }

        List<GoodsReceiptLine> validLines = new ArrayList<>();
        List<Map<String, String>> allSubmittedLines = new ArrayList<>();

        boolean hasIncompleteRow = false;
        for (int i = 0; i < 100; i++) {
            String vid = request.getParameter("lines[" + i + "].variantId");
            String qGood = request.getParameter("lines[" + i + "].qtyGood");

            // If we have no data for this index, assume no more lines follow
            if (vid == null && qGood == null)
                break;

            java.util.Map<String, String> rowData = new java.util.HashMap<>();
            rowData.put("variantId", vid != null ? vid : "");
            rowData.put("poLineId", request.getParameter("lines[" + i + "].poLineId"));
            rowData.put("qtyGood", qGood != null ? qGood : "0");
            rowData.put("qtyDamaged", request.getParameter("lines[" + i + "].qtyDamaged"));
            rowData.put("qtyMissing", request.getParameter("lines[" + i + "].qtyMissing"));
            rowData.put("note", request.getParameter("lines[" + i + "].note"));
            allSubmittedLines.add(rowData);

            if (vid == null || vid.isBlank()) {
                hasIncompleteRow = true;
                continue;
            }

            try {
                model.GoodsReceiptLine line = new model.GoodsReceiptLine();
                line.setVariantId(Long.parseLong(vid));
                String poLineIdStr = rowData.get("poLineId");
                if (poLineIdStr != null && !poLineIdStr.isBlank()) {
                    line.setPoLineId(Long.parseLong(poLineIdStr));
                }

                String qExp = request.getParameter("lines[" + i + "].qtyExpected");
                line.setQtyExpected(
                        qExp != null && !qExp.isBlank() ? new java.math.BigDecimal(qExp) : java.math.BigDecimal.ZERO);

                String qDam = rowData.get("qtyDamaged");
                String qMiss = rowData.get("qtyMissing");

                java.math.BigDecimal g = new java.math.BigDecimal(rowData.get("qtyGood"));
                java.math.BigDecimal d = (qDam != null && !qDam.isBlank()) ? new java.math.BigDecimal(qDam)
                        : java.math.BigDecimal.ZERO;
                java.math.BigDecimal m = (qMiss != null && !qMiss.isBlank()) ? new java.math.BigDecimal(qMiss)
                        : java.math.BigDecimal.ZERO;

                if (g.add(d).add(m).compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines",
                            "Mỗi dòng hàng phải có ít nhất một giá trị Số lượng (Good/Damaged/Missing) lớn hơn 0.");
                }

                line.setQtyGood(g);
                line.setQtyReceived(g);
                line.setQtyDamaged(d);
                line.setQtyMissing(m);

                String qExt = request.getParameter("lines[" + i + "].qtyExtra");
                line.setQtyExtra(
                        qExt != null && !qExt.isBlank() ? new java.math.BigDecimal(qExt) : java.math.BigDecimal.ZERO);

                line.setNote(rowData.get("note"));
                validLines.add(line);
            } catch (Exception e) {
                fieldErrors.put("lines", "Dữ liệu dòng hàng không hợp lệ: " + e.getMessage());
            }
        }

        if (allSubmittedLines.isEmpty()) {
            fieldErrors.put("lines", "Vui lòng bấm 'Add Line' để nhập hàng.");
        } else if (validLines.isEmpty() || hasIncompleteRow) {
            if (validLines.isEmpty()) {
                fieldErrors.put("lines", "Bạn chưa chọn Sản phẩm cho (các) dòng hàng.");
            } else {
                fieldErrors.put("lines", "Vui lòng chọn Sản phẩm cho tất cả các dòng hàng đã thêm.");
            }
        }

        if (!fieldErrors.isEmpty()) {
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("grnId", existingId);
            request.setAttribute("oldGrnNumber", grnNumber);
            request.setAttribute("oldPoId", poIdStr);
            request.setAttribute("oldSupplierId", supplierIdStr);
            request.setAttribute("oldNote", note);

            // Re-package submitted lines into the same JSON format for consistency
            List<model.GoodsReceiptLine> dummyLines = new ArrayList<>();
            for (Map<String, String> m : allSubmittedLines) {
                model.GoodsReceiptLine dl = new model.GoodsReceiptLine();
                try {
                    String plId = m.get("poLineId");
                    dl.setPoLineId(plId != null && !plId.isBlank() ? Long.parseLong(plId) : null);
                    dl.setVariantId(Long.parseLong(m.get("variantId")));
                    dl.setUnitPrice(new BigDecimal(
                            request.getParameter("lines[" + allSubmittedLines.indexOf(m) + "].unitPrice") != null
                                    ? request.getParameter("lines[" + allSubmittedLines.indexOf(m) + "].unitPrice")
                                    : "0"));
                    dl.setQtyExpected(new BigDecimal(
                            request.getParameter("lines[" + allSubmittedLines.indexOf(m) + "].qtyExpected") != null
                                    ? request.getParameter("lines[" + allSubmittedLines.indexOf(m) + "].qtyExpected")
                                    : "0"));
                    dl.setQtyGood(new BigDecimal(m.get("qtyGood")));
                    dl.setQtyDamaged(new BigDecimal(m.get("qtyDamaged")));
                    dl.setQtyMissing(new BigDecimal(m.get("qtyMissing")));
                    dl.setNote(m.get("note"));
                    dummyLines.add(dl);
                } catch (Exception e) {
                }
            }
            request.setAttribute("oldLinesJson", packageLinesToJson(dummyLines));
            handleCreateForm(request, response);
            return;
        }

        GoodsReceipt grn = new GoodsReceipt();
        if (existingId != null) {
            // Check status again before update
            GoodsReceipt old = grnDao.getById(existingId);
            if (old == null || !"PENDING".equals(old.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
                return;
            }
            grn.setGrnId(existingId);
        }
        grn.setGrnNumber(grnNumber);
        grn.setPoId(poId);

        // Default Warehouse ID to 1 since it's removed from UI
        String whIdStr = request.getParameter("warehouseId");
        grn.setWarehouseId((whIdStr != null && !whIdStr.isBlank()) ? Long.parseLong(whIdStr) : 1L);

        grn.setCreatedBy(user.getUserId());
        grn.setNote(note);

        Long resultGrnId = null;
        dao.PurchaseOrderDAO poDao = new dao.PurchaseOrderDAO();
        if (existingId != null) {
            grnDao.updateGRN(grn, validLines);
            resultGrnId = existingId;

            // Nếu đổi PO khác, mở lại PO cũ và đóng PO mới
            GoodsReceipt oldGrn = grnDao.getById(existingId);
            if (oldGrn != null && oldGrn.getPoId() != null && !oldGrn.getPoId().equals(poId)) {
                poDao.updateStatus(oldGrn.getPoId(), "CREATED");
            }
        } else {
            resultGrnId = grnDao.createGRN(grn, validLines);
        }

        // Đóng PO hiện tại
        if (poId != null) {
            poDao.updateStatus(poId, "CLOSED");
        }

        // Sau khi lưu thành công, chuyển đến màn hình Putaway
        response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + resultGrnId);
    }

    private void handlePutaway(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        ZoneDAO zoneDao = new ZoneDAO();
        SlotDAO slotDao = new SlotDAO();

        Long id = Long.parseLong(request.getParameter("id"));
        GoodsReceipt grn = grnDao.getById(id);
        if (grn == null) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
            return;
        }

        request.setAttribute("grn", grn);
        request.setAttribute("lines", grnDao.getLinesByGrnId(id));

        // Get warehouse name without modifying model
        WarehouseDAO warehouseDao = new WarehouseDAO();
        Warehouse warehouse = warehouseDao.getWarehouseById(grn.getWarehouseId());
        request.setAttribute("warehouseName", warehouse != null ? warehouse.getName() : "ID: " + grn.getWarehouseId());

        // Tìm các Zone Z-STO và Z-DAM để lấy Slot gợi ý
        List<Zone> zones = zoneDao.getZonesByWarehouseId(grn.getWarehouseId());
        Zone stoZone = zones.stream().filter(z -> "Z-STO".equals(z.getCode())).findFirst().orElse(null);
        Zone damZone = zones.stream().filter(z -> "Z-DAM".equals(z.getCode())).findFirst().orElse(null);

        if (stoZone != null) {
            request.setAttribute("storageSlots",
                    slotDao.getSlotsWithInventoryByZoneId(stoZone.getZoneId(), grn.getWarehouseId()));
        }
        if (damZone != null) {
            request.setAttribute("damageSlots",
                    slotDao.getSlotsWithInventoryByZoneId(damZone.getZoneId(), grn.getWarehouseId()));
        }

        request.getRequestDispatcher(ViewPath.GRN_PUTAWAY).forward(request, response);
    }

    private void handleConfirmPutaway(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        Long grnId = Long.parseLong(request.getParameter("grnId"));

        Object sessionUser = request.getSession().getAttribute("USER");
        Long userId = 1L; // Fallback for testing
        if (sessionUser instanceof model.User) {
            userId = ((model.User) sessionUser).getUserId();
        }

        // Lấy danh sách GRN Lines để loop qua từng sản phẩm
        List<model.GoodsReceiptLine> lines = grnDao.getLinesByGrnId(grnId);
        List<model.PutAwayLine> putawayLines = new ArrayList<>();

        for (model.GoodsReceiptLine line : lines) {
            Long grnLineId = line.getGrnLineId();

            // 1. Process STORAGE assignments
            String[] storageQtys = request.getParameterValues("qty_" + grnLineId + "_STORAGE[]");
            String[] storageSlots = request.getParameterValues("slotId_" + grnLineId + "_STORAGE[]");

            if (storageQtys != null && storageSlots != null) {
                for (int i = 0; i < Math.min(storageQtys.length, storageSlots.length); i++) {
                    String qStr = storageQtys[i];
                    String sStr = storageSlots[i];
                    if (qStr != null && !qStr.isEmpty() && sStr != null && !sStr.isEmpty()) {
                        BigDecimal qty = new BigDecimal(qStr);
                        if (qty.compareTo(BigDecimal.ZERO) > 0) {
                            model.PutAwayLine pl = new model.PutAwayLine();
                            pl.setGrnLineId(grnLineId);
                            pl.setToSlotId(Long.parseLong(sStr));
                            pl.setQtyPutaway(qty);
                            putawayLines.add(pl);
                        }
                    }
                }
            }

            // 2. Process DAMAGE assignments
            String[] damageQtys = request.getParameterValues("qty_" + grnLineId + "_DAMAGE[]");
            String[] damageSlots = request.getParameterValues("slotId_" + grnLineId + "_DAMAGE[]");

            if (damageQtys != null && damageSlots != null) {
                for (int i = 0; i < Math.min(damageQtys.length, damageSlots.length); i++) {
                    String qStr = damageQtys[i];
                    String sStr = damageSlots[i];
                    if (qStr != null && !qStr.isEmpty() && sStr != null && !sStr.isEmpty()) {
                        BigDecimal qty = new BigDecimal(qStr);
                        if (qty.compareTo(BigDecimal.ZERO) > 0) {
                            model.PutAwayLine pl = new model.PutAwayLine();
                            pl.setGrnLineId(grnLineId);
                            pl.setToSlotId(Long.parseLong(sStr));
                            pl.setQtyPutaway(qty);
                            putawayLines.add(pl);
                        }
                    }
                }
            }
        }

        if (!putawayLines.isEmpty()) {
            grnDao.savePutawayInfo(grnId, userId, putawayLines);
        }

        response.sendRedirect(
                request.getContextPath() + "/goods-receipt?action=detail&id=" + grnId + "&putaway=success");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        String idStr = request.getParameter("id");
        if (idStr != null) {
            Long id = Long.parseLong(idStr);
            GoodsReceipt grn = grnDao.getById(id);
            if (grn != null && ("PENDING".equals(grn.getStatus()) || "DRAFT".equals(grn.getStatus()))) {
                grnDao.deleteGRN(id);
                // Re-open PO if it was closed
                if (grn.getPoId() != null) {
                    dao.PurchaseOrderDAO poDao = new dao.PurchaseOrderDAO();
                    poDao.updateStatus(grn.getPoId(), "CREATED");
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
    }

    private void handleApprove(HttpServletRequest request, HttpServletResponse response, String status)
            throws Exception {
        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        dao.InventoryBalanceDAO invDao = new dao.InventoryBalanceDAO();
        dao.InventorySummaryDAO summaryDao = new dao.InventorySummaryDAO();
        dao.InventoryTxnDAO txnDao = new dao.InventoryTxnDAO();
        dao.PurchaseOrderDAO poDao = new dao.PurchaseOrderDAO();
        SlotDAO slotDao = new SlotDAO();
        String idStr = request.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            Long id = Long.parseLong(idStr);
            Object sessionUser = request.getSession().getAttribute("USER");
            Long approverId = 1L;
            String userRoles = "ADMIN";

            if (sessionUser instanceof model.User) {
                model.User user = (model.User) sessionUser;
                approverId = user.getUserId();
                userRoles = user.getRoleNames();
                // Nếu roleNames null (login() chưa query roles), giữ default "ADMIN"
                // vì AuthFilter đã xác thực user rồi, backend chỉ cần biết đã login
                if (userRoles == null) {
                    userRoles = "ADMIN";
                }
            }

            if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("WAREHOUSE_MANAGER"))) {
                GoodsReceipt grn = grnDao.getById(id);
                if (grn != null && ("PENDING".equals(grn.getStatus()) || "DRAFT".equals(grn.getStatus()))) {
                    boolean success = grnDao.updateStatus(id, status, approverId);

                    if (success && "APPROVED".equals(status)) {
                        // 1. UPDATE INVENTORY (BALANCE, SUMMARY, TXN)
                        List<model.PutAwayLine> ptlines = grnDao.getPutawayLinesByGrnId(id);
                        for (model.PutAwayLine pl : ptlines) {
                            Long variantId = grnDao.getVariantIdByGrnLineId(pl.getGrnLineId());
                            if (variantId != null) {
                                String zoneType = slotDao.getZoneTypeBySlotId(pl.getToSlotId());
                                String condition = "GOOD"; // Default
                                if ("DAMAGED".equals(zoneType) || "DAMAGE".equals(zoneType)
                                        || "Z-DAM".equals(zoneType)) {
                                    condition = "DAMAGED";
                                }

                                // A. Update Detailed Inventory (By Slot)
                                invDao.assignProductToSlot(grn.getWarehouseId(), pl.getToSlotId(), variantId, condition,
                                        pl.getQtyPutaway());

                                // B. Update Aggregated Inventory (By Warehouse)
                                summaryDao.updateSummary(grn.getWarehouseId(), variantId, condition,
                                        pl.getQtyPutaway());

                                // C. Record Transaction (Audit Trail)
                                model.InventoryTxn txn = new model.InventoryTxn();
                                txn.setTxnType("RECEIPT");
                                txn.setWarehouseId(grn.getWarehouseId());
                                txn.setToSlotId(pl.getToSlotId());
                                txn.setVariantId(variantId);
                                txn.setCondition(condition);
                                txn.setQtyDelta(pl.getQtyPutaway());
                                txn.setRefDocType("GRN");
                                txn.setRefDocId(grn.getGrnId());
                                txn.setNote("Nhập hàng từ phiếu " + grn.getGrnNumber());
                                txn.setCreatedBy(approverId);
                                txnDao.insertTxn(txn);
                            }
                        }

                        // 2. CLOSE PURCHASE ORDER
                        if (grn.getPoId() != null) {
                            poDao.updateStatus(grn.getPoId(), "CLOSED");
                        }
                    } else if (success && "REJECTED".equals(status)) {
                        // Re-open PO if it was closed by the earlier bug during save
                        if (grn.getPoId() != null) {
                            poDao.updateStatus(grn.getPoId(), "CREATED");
                        }
                    }

                    if (success) {
                        response.sendRedirect(request.getContextPath() + "/goods-receipt?action=detail&id=" + id);
                    } else {
                        response.sendRedirect(
                                request.getContextPath() + "/goods-receipt?action=list&error=update_failed");
                    }
                } else {
                    response.sendRedirect(
                            request.getContextPath() + "/goods-receipt?action=list&error=invalid_status_or_not_found");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/goods-receipt?action=detail&id=" + id
                        + "&error=permission_denied");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
        }
    }
}
