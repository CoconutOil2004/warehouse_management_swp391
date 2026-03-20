package controller;

import dao.GoodsReceiptDAO;
import dao.PurchaseOrderDAO;
import dao.SupplierDAO;
import dto.GoodsReceiptListDTO;
import dto.PurchaseOrderHeaderDTO;
import dto.PurchaseOrderLineDTO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    /**
     * Shortage vs PO: ordered − (good + damaged on line + extra good). Extra damaged does not cover the order.
     */
    private static BigDecimal computeQtyMissing(BigDecimal expected, BigDecimal qtyGood, BigDecimal qtyDamaged,
            BigDecimal qtyExtraGood) {
        if (expected == null) {
            expected = BigDecimal.ZERO;
        }
        BigDecimal g = qtyGood != null ? qtyGood : BigDecimal.ZERO;
        BigDecimal d = qtyDamaged != null ? qtyDamaged : BigDecimal.ZERO;
        BigDecimal eg = qtyExtraGood != null ? qtyExtraGood : BigDecimal.ZERO;
        BigDecimal m = expected.subtract(g.add(d).add(eg));
        return m.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : m;
    }
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
                case "checkCapacity" ->
                    handleCheckCapacity(request, response);
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

        // Pass RBAC flags to JSP
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        boolean canMutation = roles != null && (roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"));
        request.setAttribute("canMutation", canMutation);

        request.getRequestDispatcher(ViewPath.GRN_LIST).forward(request, response);
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        if (roles == null || !(roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"))) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list&error=no_permission");
            return;
        }

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
        request.setAttribute("isPutawayComplete", grnDao.isPutawayComplete(id));

        // Get warehouse name
        WarehouseDAO warehouseDao = new WarehouseDAO();
        Warehouse warehouse = warehouseDao.getWarehouseById(grn.getWarehouseId());
        request.setAttribute("warehouseName", warehouse != null ? warehouse.getName() : "N/A");

        // Pass RBAC flags to JSP
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        boolean canMutation = roles != null && (roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"));
        boolean isManager = roles != null && roles.contains("WAREHOUSE_MANAGER");
        request.setAttribute("canMutation", canMutation);
        request.setAttribute("isManager", isManager);

        request.getRequestDispatcher(ViewPath.GRN_DETAIL).forward(request, response);
    }

    private void handleEditForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        if (roles == null || !(roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"))) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list&error=no_permission");
            return;
        }

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
            sb.append("\"qtyExtraGood\":\"")
                    .append(l.getQtyExtraGood() != null ? l.getQtyExtraGood().toBigInteger() : 0).append("\",");
            sb.append("\"qtyExtraDamaged\":\"")
                    .append(l.getQtyExtraDamaged() != null ? l.getQtyExtraDamaged().toBigInteger() : 0).append("\",");

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

    private void handleCheckCapacity(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("id");
        Long warehouseId = null;
        BigDecimal totalGood = BigDecimal.ZERO;
        BigDecimal totalDamaged = BigDecimal.ZERO;
        BigDecimal totalExtra = BigDecimal.ZERO;

        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        SlotDAO slotDao = new SlotDAO();
        ZoneDAO zoneDao = new ZoneDAO();

        if (idStr != null && !idStr.isEmpty()) {
            Long id = Long.parseLong(idStr);
            GoodsReceipt grn = grnDao.getById(id);
            if (grn != null) {
                warehouseId = grn.getWarehouseId();
                List<GoodsReceiptLine> lines = grnDao.getLinesByGrnId(id);
                for (GoodsReceiptLine line : lines) {
                    totalGood = totalGood.add(line.getQtyGood() != null ? line.getQtyGood() : BigDecimal.ZERO);
                    BigDecimal d = line.getQtyDamaged() != null ? line.getQtyDamaged() : BigDecimal.ZERO;
                    BigDecimal ed = line.getQtyExtraDamaged() != null ? line.getQtyExtraDamaged() : BigDecimal.ZERO;
                    totalDamaged = totalDamaged.add(d).add(ed);
                    totalExtra = totalExtra.add(line.getQtyExtraGood() != null ? line.getQtyExtraGood() : BigDecimal.ZERO);
                }
            }
        } else {
            String whIdStr = request.getParameter("warehouseId");
            if (whIdStr != null && !whIdStr.isEmpty()) {
                warehouseId = Long.parseLong(whIdStr);
            }
            String qGoodStr = request.getParameter("totalGood");
            String qDamagedStr = request.getParameter("totalDamaged");
            String qExtraStr = request.getParameter("totalExtra");

            if (qGoodStr != null && !qGoodStr.isEmpty()) totalGood = new BigDecimal(qGoodStr);
            if (qDamagedStr != null && !qDamagedStr.isEmpty()) totalDamaged = new BigDecimal(qDamagedStr);
            if (qExtraStr != null && !qExtraStr.isEmpty()) totalExtra = new BigDecimal(qExtraStr);
        }

        if (warehouseId == null) {
            response.setStatus(400); // Bad Request
            return;
        }

        List<Zone> zones = zoneDao.getZonesByWarehouseId(warehouseId);
        
        BigDecimal stoAvail = BigDecimal.ZERO;
        BigDecimal damAvail = BigDecimal.ZERO;
        BigDecimal excAvail = BigDecimal.ZERO;

        for (Zone z : zones) {
            if ("STORAGE".equals(z.getZoneType())) {
                BigDecimal cap = slotDao.getTotalAvailableCapacityByZoneId(z.getZoneId());
                if (cap != null) stoAvail = stoAvail.add(cap);
            } else if ("DAMAGE".equals(z.getZoneType())) {
                BigDecimal cap = slotDao.getTotalAvailableCapacityByZoneId(z.getZoneId());
                if (cap != null) damAvail = damAvail.add(cap);
            } else if ("EXCESS".equals(z.getZoneType())) {
                BigDecimal cap = slotDao.getTotalAvailableCapacityByZoneId(z.getZoneId());
                if (cap != null) excAvail = excAvail.add(cap);
            }
        }

        boolean goodOk = totalGood.compareTo(stoAvail) <= 0;
        boolean damOk = totalDamaged.compareTo(damAvail) <= 0;
        boolean excOk = totalExtra.compareTo(excAvail) <= 0;
        boolean sufficient = goodOk && damOk && excOk;

        response.setContentType("application/json;charset=UTF-8");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":true,");
        sb.append("\"sufficient\":").append(sufficient).append(",");
        sb.append("\"details\":{");
        sb.append("\"good\":{\"required\":").append(totalGood).append(",\"available\":").append(stoAvail).append(",\"isSufficient\":").append(goodOk).append("},");
        sb.append("\"damaged\":{\"required\":").append(totalDamaged).append(",\"available\":").append(damAvail).append(",\"isSufficient\":").append(damOk).append("},");
        sb.append("\"excess\":{\"required\":").append(totalExtra).append(",\"available\":").append(excAvail).append(",\"isSufficient\":").append(excOk).append("}");
        sb.append("}");
        sb.append("}");

        response.getWriter().write(sb.toString());
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
            rowData.put("qtyExtraGood", request.getParameter("lines[" + i + "].qtyExtraGood"));
            rowData.put("qtyExtraDamaged", request.getParameter("lines[" + i + "].qtyExtraDamaged"));
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

                java.math.BigDecimal g = new java.math.BigDecimal(rowData.get("qtyGood"));
                java.math.BigDecimal d = (qDam != null && !qDam.isBlank()) ? new java.math.BigDecimal(qDam)
                        : java.math.BigDecimal.ZERO;
                String qEg = rowData.get("qtyExtraGood");
                String qEd = rowData.get("qtyExtraDamaged");
                java.math.BigDecimal eg = (qEg != null && !qEg.isBlank()) ? new java.math.BigDecimal(qEg)
                        : java.math.BigDecimal.ZERO;
                java.math.BigDecimal ed = (qEd != null && !qEd.isBlank()) ? new java.math.BigDecimal(qEd)
                        : java.math.BigDecimal.ZERO;

                java.math.BigDecimal m = computeQtyMissing(line.getQtyExpected(), g, d, eg);

                if (g.add(d).compareTo(line.getQtyExpected()) > 0) {
                    fieldErrors.put("lines",
                            "Good (thực tế) + Damaged (thực tế) không được vượt số đặt (Ordered). Phần vượt hãy nhập vào Extra (good) hoặc Extra (dmg).");
                    continue;
                }

                if (g.add(d).add(m).add(eg).add(ed).compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines",
                            "Mỗi dòng hàng phải có ít nhất một giá trị Số lượng (Good/Damaged/Missing/Extra good/Extra damaged) lớn hơn 0.");
                }

                line.setQtyGood(g);
                line.setQtyReceived(g.add(d).add(eg).add(ed)); // Total physical items received (excludes missing)
                line.setQtyDamaged(d);
                line.setQtyMissing(m);
                line.setQtyExtraGood(eg);
                line.setQtyExtraDamaged(ed);

                line.setNote(rowData.get("note"));
                validLines.add(line);
            } catch (Exception e) {
                fieldErrors.put("lines", "Invalid line item data: " + e.getMessage());
            }
        }

        if (allSubmittedLines.isEmpty()) {
            fieldErrors.put("lines", "Please select a Purchase Order to load items.");
        } else if (validLines.isEmpty() || hasIncompleteRow) {
            if (!fieldErrors.containsKey("lines")) {
                if (validLines.isEmpty()) {
                    fieldErrors.put("lines", "No products selected.");
                } else {
                    fieldErrors.put("lines", "Please ensure all rows have a selected product.");
                }
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
                    BigDecimal dg = new BigDecimal(m.get("qtyGood") != null && !m.get("qtyGood").isBlank() ? m.get("qtyGood") : "0");
                    String dDam = m.get("qtyDamaged");
                    BigDecimal dd = new BigDecimal(dDam != null && !dDam.isBlank() ? dDam : "0");
                    String egStr = m.get("qtyExtraGood");
                    String edStr = m.get("qtyExtraDamaged");
                    BigDecimal deg = new BigDecimal(egStr != null && !egStr.isBlank() ? egStr : "0");
                    BigDecimal ded = new BigDecimal(edStr != null && !edStr.isBlank() ? edStr : "0");
                    dl.setQtyGood(dg);
                    dl.setQtyDamaged(dd);
                    dl.setQtyExtraGood(deg);
                    dl.setQtyExtraDamaged(ded);
                    dl.setQtyMissing(computeQtyMissing(dl.getQtyExpected(), dg, dd, deg));
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
        Long warehouseId = (whIdStr != null && !whIdStr.isBlank()) ? Long.parseLong(whIdStr) : 1L;
        Warehouse wh = new WarehouseDAO().getWarehouseById(warehouseId);
        if (wh == null) {
            fieldErrors.put("warehouseId", "Warehouse does not exist.");
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("grnId", existingId);
            request.setAttribute("oldGrnNumber", grnNumber);
            request.setAttribute("oldPoId", poIdStr);
            request.setAttribute("oldSupplierId", supplierIdStr);
            request.setAttribute("oldNote", note);
            request.setAttribute("oldLinesJson", packageLinesToJson(new ArrayList<>(validLines)));
            handleCreateForm(request, response);
            return;
        }
        if (!"ACTIVE".equals(wh.getStatus())) {
            fieldErrors.put("warehouseId", "Only ACTIVE warehouses can be selected.");
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("grnId", existingId);
            request.setAttribute("oldGrnNumber", grnNumber);
            request.setAttribute("oldPoId", poIdStr);
            request.setAttribute("oldSupplierId", supplierIdStr);
            request.setAttribute("oldNote", note);
            request.setAttribute("oldLinesJson", packageLinesToJson(new ArrayList<>(validLines)));
            handleCreateForm(request, response);
            return;
        }
        grn.setWarehouseId(warehouseId);

        // Validate PO exists and status = CREATED when creating new GRN (or when changing PO)
        PurchaseOrderDAO poDaoForValidation = new PurchaseOrderDAO();
        PurchaseOrderHeaderDTO poHeader = poDaoForValidation.getPurchaseOrderHeader(poId);
        if (poHeader == null) {
            fieldErrors.put("poId", "Purchase Order does not exist.");
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("grnId", existingId);
            request.setAttribute("oldGrnNumber", grnNumber);
            request.setAttribute("oldPoId", poIdStr);
            request.setAttribute("oldSupplierId", supplierIdStr);
            request.setAttribute("oldNote", note);
            request.setAttribute("oldLinesJson", packageLinesToJson(new ArrayList<>(validLines)));
            handleCreateForm(request, response);
            return;
        }
        // Cho phép tạo phiếu nhập từ PO trạng thái CREATED hoặc IMPORTED (PO từ Excel)
        if (!"CREATED".equals(poHeader.getStatus()) && !"IMPORTED".equals(poHeader.getStatus())) {
            fieldErrors.put("poId", "Chỉ được tạo/sửa phiếu nhập từ Purchase Order có trạng thái CREATED hoặc IMPORTED. PO hiện tại: " + poHeader.getStatus());
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("grnId", existingId);
            request.setAttribute("oldGrnNumber", grnNumber);
            request.setAttribute("oldPoId", poIdStr);
            request.setAttribute("oldSupplierId", supplierIdStr);
            request.setAttribute("oldNote", note);
            request.setAttribute("oldLinesJson", packageLinesToJson(new ArrayList<>(validLines)));
            handleCreateForm(request, response);
            return;
        }

        grn.setCreatedBy(user.getUserId());
        grn.setNote(note);

        Long resultGrnId = null;
        if (existingId != null) {
            grnDao.updateGRN(grn, validLines);
            resultGrnId = existingId;
            request.getSession().setAttribute("message", "Goods Receipt updated successfully.");
        } else {
            resultGrnId = grnDao.createGRN(grn, validLines);
            request.getSession().setAttribute("message", "Goods Receipt created successfully.");
        }

        // Sau khi lưu thành công, chuyển đến màn hình Putaway
        response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + resultGrnId);
    }

    private void handlePutaway(HttpServletRequest request, HttpServletResponse response) throws Exception {
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        if (roles == null || !(roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"))) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list&error=no_permission");
            return;
        }

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

        // Tìm các Zone theo zoneType để lấy Slot gợi ý
        List<Zone> zones = zoneDao.getZonesByWarehouseId(grn.getWarehouseId());
        
        List<Object> storageSlots = new java.util.ArrayList<>();
        List<Object> damageSlots = new java.util.ArrayList<>();
        List<Object> excessSlots = new java.util.ArrayList<>();

        for (Zone z : zones) {
            if ("STORAGE".equals(z.getZoneType())) {
                storageSlots.addAll(slotDao.getSlotsWithInventoryByZoneId(z.getZoneId(), grn.getWarehouseId()));
            } else if ("DAMAGE".equals(z.getZoneType())) {
                damageSlots.addAll(slotDao.getSlotsWithInventoryByZoneId(z.getZoneId(), grn.getWarehouseId()));
            } else if ("EXCESS".equals(z.getZoneType())) {
                excessSlots.addAll(slotDao.getSlotsWithInventoryByZoneId(z.getZoneId(), grn.getWarehouseId()));
            }
        }
        
        request.setAttribute("storageSlots", storageSlots);
        request.setAttribute("damageSlots", damageSlots);
        request.setAttribute("excessSlots", excessSlots);

        // Pass RBAC flags to JSP
        request.setAttribute("canMutation", roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"));
        request.setAttribute("isManager", roles.contains("WAREHOUSE_MANAGER"));

        request.getRequestDispatcher(ViewPath.GRN_PUTAWAY).forward(request, response);
    }


    private void handleConfirmPutaway(HttpServletRequest request, HttpServletResponse response) throws Exception {
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        if (roles == null || !(roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"))) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list&error=no_permission");
            return;
        }

        GoodsReceiptDAO grnDao = new GoodsReceiptDAO();
        SlotDAO slotDao = new SlotDAO();
        Long grnId = Long.parseLong(request.getParameter("grnId"));

        GoodsReceipt grn = grnDao.getById(grnId);
        if (grn == null) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
            return;
        }

        // Prevent double-confirm putaway (idempotency guard)
        if (grnDao.isPutawayComplete(grnId)) {
            request.getSession().setAttribute("message", "Putaway was already confirmed for this GRN.");
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=detail&id=" + grnId + "&putaway=already_confirmed");
            return;
        }
        Long warehouseId = grn.getWarehouseId();

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
                            long sid = Long.parseLong(sStr);
                            if (!"STORAGE".equals(slotDao.getZoneTypeBySlotId(sid))) {
                                response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + grnId + "&error=invalid_slot_zone");
                                return;
                            }
                            model.PutAwayLine pl = new model.PutAwayLine();
                            pl.setGrnLineId(grnLineId);
                            pl.setToSlotId(sid);
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
                            long sid = Long.parseLong(sStr);
                            if (!"DAMAGE".equals(slotDao.getZoneTypeBySlotId(sid))) {
                                response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + grnId + "&error=invalid_slot_zone");
                                return;
                            }
                            model.PutAwayLine pl = new model.PutAwayLine();
                            pl.setGrnLineId(grnLineId);
                            pl.setToSlotId(sid);
                            pl.setQtyPutaway(qty);
                            putawayLines.add(pl);
                        }
                    }
                }
            }

            // 3. Process EXCESS assignments
            String[] excessQtys = request.getParameterValues("qty_" + grnLineId + "_EXCESS[]");
            String[] excessSlots = request.getParameterValues("slotId_" + grnLineId + "_EXCESS[]");

            if (excessQtys != null && excessSlots != null) {
                for (int i = 0; i < Math.min(excessQtys.length, excessSlots.length); i++) {
                    String qStr = excessQtys[i];
                    String sStr = excessSlots[i];
                    if (qStr != null && !qStr.isEmpty() && sStr != null && !sStr.isEmpty()) {
                        BigDecimal qty = new BigDecimal(qStr);
                        if (qty.compareTo(BigDecimal.ZERO) > 0) {
                            long sid = Long.parseLong(sStr);
                            if (!"EXCESS".equals(slotDao.getZoneTypeBySlotId(sid))) {
                                response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + grnId + "&error=invalid_slot_zone");
                                return;
                            }
                            model.PutAwayLine pl = new model.PutAwayLine();
                            pl.setGrnLineId(grnLineId);
                            pl.setToSlotId(sid);
                            pl.setQtyPutaway(qty);
                            putawayLines.add(pl);
                        }
                    }
                }
            }
        }

        // Validate: warehouse + per-line caps by destination zone (STORAGE <= good, DAMAGE <= damaged+extra damaged, EXCESS <= extra good)
        if (!putawayLines.isEmpty()) {
            java.util.Map<Long, BigDecimal> sumStorage = new java.util.HashMap<>();
            java.util.Map<Long, BigDecimal> sumDamage = new java.util.HashMap<>();
            java.util.Map<Long, BigDecimal> sumExcess = new java.util.HashMap<>();
            for (model.PutAwayLine pl : putawayLines) {
                if (!slotDao.isSlotInWarehouse(pl.getToSlotId(), warehouseId)) {
                    response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + grnId + "&error=Slot+not+in+warehouse");
                    return;
                }
                String zt = slotDao.getZoneTypeBySlotId(pl.getToSlotId());
                if ("STORAGE".equals(zt)) {
                    sumStorage.merge(pl.getGrnLineId(), pl.getQtyPutaway(), BigDecimal::add);
                } else if ("DAMAGE".equals(zt)) {
                    sumDamage.merge(pl.getGrnLineId(), pl.getQtyPutaway(), BigDecimal::add);
                } else if ("EXCESS".equals(zt)) {
                    sumExcess.merge(pl.getGrnLineId(), pl.getQtyPutaway(), BigDecimal::add);
                }
            }
            for (model.GoodsReceiptLine line : lines) {
                Long lid = line.getGrnLineId();
                BigDecimal qtyGood = line.getQtyGood() != null ? line.getQtyGood() : BigDecimal.ZERO;
                BigDecimal qtyDamaged = line.getQtyDamaged() != null ? line.getQtyDamaged() : BigDecimal.ZERO;
                BigDecimal qtyExtraGood = line.getQtyExtraGood() != null ? line.getQtyExtraGood() : BigDecimal.ZERO;
                BigDecimal qtyExtraDamaged = line.getQtyExtraDamaged() != null ? line.getQtyExtraDamaged() : BigDecimal.ZERO;
                BigDecimal maxSto = qtyGood;
                BigDecimal maxDam = qtyDamaged.add(qtyExtraDamaged);
                BigDecimal maxExc = qtyExtraGood;

                if (sumStorage.getOrDefault(lid, BigDecimal.ZERO).compareTo(maxSto) > 0
                        || sumDamage.getOrDefault(lid, BigDecimal.ZERO).compareTo(maxDam) > 0
                        || sumExcess.getOrDefault(lid, BigDecimal.ZERO).compareTo(maxExc) > 0) {
                    response.sendRedirect(request.getContextPath() + "/goods-receipt?action=putaway&id=" + grnId + "&error=Putaway+qty+exceeds+received+qty+for+line");
                    return;
                }
            }
            grnDao.savePutawayInfo(grnId, userId, putawayLines);
        }

        request.getSession().setAttribute("message", "Putaway confirmed successfully.");
        response.sendRedirect(
                request.getContextPath() + "/goods-receipt?action=detail&id=" + grnId + "&putaway=success");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        model.User user = (model.User) request.getSession().getAttribute("USER");
        String roles = user != null ? user.getRoleNames() : "";
        if (roles == null || !(roles.contains("WAREHOUSE_MANAGER") || roles.contains("WAREHOUSE_STAFF"))) {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list&error=no_permission");
            return;
        }

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
                request.getSession().setAttribute("message", "Goods Receipt deleted successfully.");
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
            }

            if (userRoles != null && userRoles.contains("WAREHOUSE_MANAGER")) {
                GoodsReceipt grn = grnDao.getById(id);
                if (grn != null && ("PENDING".equals(grn.getStatus()) || "DRAFT".equals(grn.getStatus()))) {
                    // Check if putaway is complete before approving
                    if ("APPROVED".equals(status) && !grnDao.isPutawayComplete(id)) {
                        response.sendRedirect(request.getContextPath() + "/goods-receipt?action=detail&id=" + id + "&error=putaway_incomplete");
                        return;
                    }
                    
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
                        String msg = "APPROVED".equals(status) ? "Goods Receipt approved successfully." : "Goods Receipt rejected successfully.";
                        request.getSession().setAttribute("message", msg);
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
                // Unauthorized
                response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list&error=no_permission");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/goods-receipt?action=list");
        }
    }
}
