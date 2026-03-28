package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dto.POLineCreateDTO;
import dto.ProductVariantDTO;
import dto.PurchaseOrderHeaderDTO;
import dto.PurchaseOrderLineDTO;
import dto.PurchaseOrderListDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import service.GoodsReceiptService;
import service.ProductService;
import service.ProductVariantService;
import service.PurchaseOrderImportService;
import service.PurchaseOrderService;
import service.SupplierService;
import util.CurrentUserUtil;
import util.RequestUtil;
import util.RoleUtil;
import util.ToastUtil;
import util.ViewPath;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10485760, maxRequestSize = 20971520)
@WebServlet(name = "PurchaseOrderController", urlPatterns = { "/purchase-orders" })
public class PurchaseOrderController extends HttpServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 5;
    private final SupplierService sService = new SupplierService();
    private final ProductService pService = new ProductService();
    private final PurchaseOrderService poService = new PurchaseOrderService();
    private final PurchaseOrderImportService poImportService = new PurchaseOrderImportService();
    private final GoodsReceiptService grnService = new GoodsReceiptService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if (action == null) {
                action = "";
            }
            switch (action) {
                case "variants":
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    handleGetVariants(request, response);
                    break;
                case "import":
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    forwardImportForm(request, response);
                    break;
                case "edit":
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    forwardEditForm(request, response);
                    break;
                default:
                    forwardList(request, response);
                    break;
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }
        try {
            switch (action) {
                case "detail" ->
                    forwardDetail(request, response);
                case "new" -> {
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    forwardCreateForm(request, response);
                }
                case "processImport" -> {
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    handleProcessImport(request, response);
                }
                case "create" -> {
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    handleCreate(request, response);
                }
                case "update" -> {
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    handleUpdate(request, response);
                }
                case "delete" -> {
                    if (blockWarehouseStaffPoMutation(request, response)) {
                        return;
                    }
                    handleDelete(request, response);
                }
                default ->
                    forwardList(request, response);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleGetVariants(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String raw = request.getParameter("productId");
        long productId;
        try {
            productId = Long.parseLong(raw);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ProductVariantService vService = new ProductVariantService();
        // lấy danh sách variant theo productid
        List<ProductVariantDTO> list = vService.listByProductId(productId);
        // khai báo json
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        // tạo json thủ công
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            dto.ProductVariantDTO v = list.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{")
                    .append("\"variantId\":").append(v.getVariantId()).append(",")
                    .append("\"variantSku\":\"").append(esc(v.getVariantSku())).append("\",")
                    .append("\"color\":\"").append(esc(v.getColor())).append("\",")
                    .append("\"size\":\"").append(esc(v.getSize())).append("\"")
                    .append("}");
        }
        sb.append("]");

        response.getWriter().write(sb.toString());
    }

    private void forwardList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int page = RequestUtil.parseInt(request.getParameter("page"), DEFAULT_PAGE);
        // size = số dòng hiển thị mỗi trang
        int size = RequestUtil.parseInt(request.getParameter("size"), DEFAULT_SIZE);
        if (page < 1) {
            page = 1;
        }
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String expectedFromStr = request.getParameter("expectedFrom");
        String expectedToStr = request.getParameter("expectedTo");
        if (keyword != null) {
            keyword = keyword.trim();
        }
        // status nếu rỗng thì đổi sang null để DAO hiểu là không filter theo status
        if (status != null && status.isBlank()) {
            status = null;
        }
        Date expectedFrom = RequestUtil.parseSqlDate(expectedFromStr);
        Date expectedTo = RequestUtil.parseSqlDate(expectedToStr);
        int totalRecords = poService.countPurchaseOrders(keyword, status, expectedFrom, expectedTo);
        int totalPages = (int) Math.ceil((double) totalRecords / size);
        if (totalPages < 1) {
            totalPages = 1;
        }
        // tránh user nhập tay
        if (page > totalPages) {
            page = totalPages;
        }
        // offset = số lượng bản ghi cần bỏ qua trước khi lấy dữ liệu
        int offset = (page - 1) * size;
        List<PurchaseOrderListDTO> pos = poService.searchPurchaseOrders(keyword, status, expectedFrom, expectedTo, size,
                offset);

        // UI dùng <t:pagination ... include="..."> để tự giữ filter khi bấm page
        request.setAttribute("pos", pos);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("size", size);
        request.setAttribute("total", totalRecords);
        request.setAttribute("canManagePurchaseOrders",
                !RoleUtil.isPurchaseOrderReadOnlyForWarehouseStaff(RoleUtil.roleNames(request)));
        request.getRequestDispatcher(ViewPath.PO_LIST).forward(request, response);
    }

    /** @return true if request was blocked (redirect sent). */
    private boolean blockWarehouseStaffPoMutation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!RoleUtil.isPurchaseOrderReadOnlyForWarehouseStaff(RoleUtil.roleNames(request))) {
            return false;
        }
        ToastUtil.setToast(request, "error", "You do not have permission to modify purchase orders.");
        response.sendRedirect(request.getContextPath() + "/purchase-orders");
        return true;
    }

    private void forwardDetail(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("id");
        long poId = (idStr == null || idStr.isBlank()) ? -1L : Long.parseLong(idStr);
        if (poId <= 0) {
            ToastUtil.setToast(request, "error", "Invalid Purchase Order id.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders");
            return;
        }
        PurchaseOrderHeaderDTO POheader = poService.getPurchaseOrderHeader(poId);
        if (POheader == null) {
            ToastUtil.setToast(request, "error", "Purchase Order not found.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders");
            return;
        }
        List<PurchaseOrderLineDTO> lines = poService.getPurchaseOrderDetailLines(poId);
        request.setAttribute("poId", poId);
        request.setAttribute("POheader", POheader);
        request.setAttribute("lines", lines);
        request.getRequestDispatcher(ViewPath.PO_DETAIL).forward(request, response);
    }

    private void forwardImportForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getRequestDispatcher(ViewPath.PO_FORM_IMPORT).forward(request, response);
    }

    private void handleProcessImport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                ToastUtil.setToast(request, "error", "Please select a file to upload.");
                response.sendRedirect(request.getContextPath() + "/purchase-orders?action=import");
                return;
            }
            Long userId = CurrentUserUtil.getUserId(request);
            if (userId == null) {
                ToastUtil.setToast(request, "error", "Unable to identify the logged-in user. Please sign in again.");
                response.sendRedirect(request.getContextPath() + ViewPath.VIEW_LOGIN);
                return;
            }
            PurchaseOrderImportService.ImportResult result = poImportService.importFromExcel(filePart, userId);

            if (result.hasErrors()) {
                StringBuilder errMsg = new StringBuilder("Import failed due to the following errors: <ul>");
                for (String err : result.getFieldErrors().values()) {
                    errMsg.append("<li>").append(err).append("</li>");
                }
                errMsg.append("</ul>");
                ToastUtil.setToast(request, "error", errMsg.toString());
            } else {
                ToastUtil.setToast(request, "success", "Successfully imported Purchase Order: " + result.getPoNumber());
            }
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=import");
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.setToast(request, "error", "Error processing Excel file: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=import");
        }
    }

    private void forwardCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.setAttribute("suppliers", sService.getActiveSuppliers());
        request.setAttribute("products", pService.getProducts());
        request.getRequestDispatcher(ViewPath.PO_FORM_CREATE).forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.setCharacterEncoding("UTF-8");
        Map<String, String> fieldErrors = new HashMap<>();
        String poNumber = request.getParameter("poNumber");
        // SAFE parse supplier
        String supplierStr = request.getParameter("supplierId");
        long supplierId = (supplierStr == null || supplierStr.isBlank()) ? 0L : Long.parseLong(supplierStr);
        String expected = request.getParameter("expectedDeliveryDate");
        Date expectedDate = null;
        if (expected == null || expected.isBlank()) {
            fieldErrors.put("expectedDeliveryDate", "Expected Delivery Date is required");
        } else {
            try {
                expectedDate = Date.valueOf(expected); // yyyy-MM-dd
                // không được hôm nay hoặc quá khứ => phải > today
                // toLocateDate() bỏ giờ lấy ngày
                if (!expectedDate.toLocalDate().isAfter(java.time.LocalDate.now())) {
                    fieldErrors.put("expectedDeliveryDate", "Expected Delivery Date must be after today");
                }
            } catch (Exception e) {
                fieldErrors.put("expectedDeliveryDate", "Invalid date format");
            }
        }
        String note = request.getParameter("note");
        Long userId = CurrentUserUtil.getUserId(request);
        if (userId == null) {
            ToastUtil.setToast(request, "error", "Unable to identify the logged-in user. Please sign in again.");
            response.sendRedirect(request.getContextPath() + ViewPath.VIEW_LOGIN);
            return;
        }
        // PO Number validate
        if (poNumber == null || poNumber.isBlank()) {
            fieldErrors.put("poNumber", "PO Number is required");
        } else if (poNumber.length() > 20) {
            fieldErrors.put("poNumber", "PO Number must be at most 20 characters");
        } else {
            if (poService.existsByPoNumber(poNumber)) {
                fieldErrors.put("poNumber", "PO Number already exists");
            }
        }
        // Supplier validate
        if (supplierId <= 0) {
            fieldErrors.put("supplierId", "Supplier is required");
        }
        // Lines parse
        List<POLineCreateDTO> lines = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            String vid = request.getParameter("lines[" + i + "].variantId");
            String qtyStr = request.getParameter("lines[" + i + "].qty");
            if (vid == null || vid.isBlank()) {
                continue;
            }
            if (qtyStr == null || qtyStr.isBlank()) {
                continue;
            }

            try {
                long variantId = Long.parseLong(vid);
                BigDecimal qty = new BigDecimal(qtyStr);
                if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Quantity cannot be negative or equal 0");
                    break;
                }
                String unitStr = request.getParameter("lines[" + i + "].unitPrice");
                BigDecimal unitPrice = (unitStr == null || unitStr.isBlank()) ? null : new BigDecimal(unitStr);
                // unit price không âm
                if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Unit Price cannot be negative or equal 0");
                    break;
                }
                String currency = request.getParameter("lines[" + i + "].currency");
                lines.add(new POLineCreateDTO(variantId, qty, unitPrice, currency));
            } catch (Exception ex) {
                fieldErrors.put("lines", "Lines contains invalid numbers");
                break;
            }
        }

        if (lines.isEmpty()) {
            fieldErrors.putIfAbsent("lines", "At least one line is required");
        }
        List<Map<String, String>> oldLines = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            String productId = request.getParameter("lines[" + i + "].productId");
            String variantId = request.getParameter("lines[" + i + "].variantId");
            String qty = request.getParameter("lines[" + i + "].qty");
            String unitPrice = request.getParameter("lines[" + i + "].unitPrice");
            String currency = request.getParameter("lines[" + i + "].currency");

            // nếu row hoàn toàn trống -> bỏ qua
            boolean allBlank = (productId == null || productId.isBlank())
                    && (variantId == null || variantId.isBlank())
                    && (qty == null || qty.isBlank())
                    && (unitPrice == null || unitPrice.isBlank())
                    && (currency == null || currency.isBlank());

            if (allBlank) {
                continue;
            }

            Map<String, String> row = new HashMap<>();
            row.put("productId", productId == null ? "" : productId);
            row.put("variantId", variantId == null ? "" : variantId);
            row.put("qty", qty == null ? "" : qty);
            row.put("unitPrice", unitPrice == null ? "" : unitPrice);
            row.put("currency", (currency == null || currency.isBlank()) ? "VND" : currency);

            oldLines.add(row);
        }

        request.setAttribute("oldLines", oldLines);

        // If errors -> forward (and remember to set suppliers again!)
        if (!fieldErrors.isEmpty()) {
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("oldPoNumber", poNumber);
            request.setAttribute("oldSupplierId", supplierId);
            request.setAttribute("oldExpected", expected);
            request.setAttribute("oldNote", note);
            request.setAttribute("oldLines", oldLines);
            request.setAttribute("suppliers", sService.getActiveSuppliers());
            request.setAttribute("products", pService.getProducts());
            // IMPORTANT: reload suppliers before forward if JSP needs it
            // request.setAttribute("suppliers", supplierDao.getAllSuppliers());
            request.getRequestDispatcher(ViewPath.PO_FORM_CREATE).forward(request, response);
            return;
        }

        poService.createManualPO(poNumber, supplierId, expectedDate, note, userId, lines);
        ToastUtil.setToast(request, "success", "Create Purchase Order successfully: " + poNumber);
        response.sendRedirect(request.getContextPath() + "/purchase-orders");

    }

    private void forwardEditForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long poId = RequestUtil.parseLong(request.getParameter("id"), -1L);
        if (poId <= 0) {
            ToastUtil.setToast(request, "error", "Invalid Purchase Order id.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders");
            return;
        }

        PurchaseOrderHeaderDTO po = poService.getPurchaseOrderHeader(poId);
        if (po == null) {
            ToastUtil.setToast(request, "error", "Purchase Order not found.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders");
            return;
        }
        // Block editing PO when status is CLOSED or IMPORTED
        if ("CLOSED".equalsIgnoreCase(po.getStatus()) || "IMPORTED".equalsIgnoreCase(po.getStatus())) {
            ToastUtil.setToast(request, "error", "Unabel to update PO with status " + po.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=detail&id=" + poId);
            return;
        }
        // Block editing when a non-rejected GRN exists (REJECTED GRN still allows PO update)
        if (poService.hasGrnBlockingPoEdit(poId)) {
            ToastUtil.setToast(request, "error", "Unable to update Purchase Order because GRN is already available.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=detail&id=" + poId);
            return;
        }
        // Block editing PO if there is an incomplete putaway GRN for this PO
        if (grnService.hasIncompletePutawayForPo(poId)) {
            ToastUtil.setToast(request, "error",
                    "Unable to update Purchase Order because GRN is already available.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=detail&id=" + poId);
            return;
        }

        List<PurchaseOrderLineDTO> lines = poService.getPurchaseOrderDetailLines(poId);

        request.setAttribute("po", po); // JSP edit dùng "po"
        request.setAttribute("lines", lines); // JSP edit dùng "lines"
        request.setAttribute("suppliers", sService.getActiveSuppliers());
        request.setAttribute("products", pService.getProducts());

        // Nếu chưa muốn load variants sẵn (vì đã có AJAX
        // /purchase-orders?action=variants)
        // thì không cần set "variants"
        request.getRequestDispatcher(ViewPath.PO_FORM_EDIT).forward(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        request.setCharacterEncoding("UTF-8");
        Map<String, String> fieldErrors = new HashMap<>();

        long poId = RequestUtil.parseLong(request.getParameter("poId"), -1L);
        if (poId <= 0) {
            response.sendRedirect(request.getContextPath() + "/purchase-orders");
            return;
        }

        PurchaseOrderHeaderDTO current = poService.getPurchaseOrderHeader(poId);
        if (current == null) {
            ToastUtil.setToast(request, "error", "Purchase Order not found.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders");
            return;
        }
        if (poService.hasGrnBlockingPoEdit(poId)) {
            ToastUtil.setToast(request, "error", "Unable to update Purchase Order because GRN is already available.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=detail&id=" + poId);
            return;
        }
        if (grnService.hasIncompletePutawayForPo(poId)) {
            ToastUtil.setToast(request, "error",
                    "Unable to update Purchase Order because GRN is already available.");
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=detail&id=" + poId);
            return;
        }
        String poNumber = request.getParameter("poNumber");
        String supplierStr = request.getParameter("supplierId");
        long supplierId = (supplierStr == null || supplierStr.isBlank()) ? 0L : Long.parseLong(supplierStr);

        String expected = request.getParameter("expectedDeliveryDate");
        Date expectedDate = null;
        if (expected == null || expected.isBlank()) {
            fieldErrors.put("expectedDeliveryDate", "Expected Delivery Date is required");
        } else {
            try {
                expectedDate = Date.valueOf(expected); // yyyy-MM-dd
                // không được hôm nay hoặc quá khứ => phải > today
                // toLocateDate() bỏ giờ lấy ngày
                if (!expectedDate.toLocalDate().isAfter(java.time.LocalDate.now())) {
                    fieldErrors.put("expectedDeliveryDate", "Expected Delivery Date must be after today");
                }
            } catch (Exception e) {
                fieldErrors.put("expectedDeliveryDate", "Invalid date format");
            }
        }

        String note = request.getParameter("note");

        // --- Validate header ---
        if (poNumber == null || poNumber.isBlank()) {
            fieldErrors.put("poNumber", "PO Number is required");
        } else if (poNumber.length() > 20) {
            fieldErrors.put("poNumber", "PO Number must be at most 20 characters");
        } else {
            // chỉ check trùng khi user đổi poNumber
            if (!poNumber.equalsIgnoreCase(current.getPoNumber())) {
                if (poService.existsByPoNumber(poNumber)) {
                    fieldErrors.put("poNumber", "PO Number already exists");
                }
            }
        }

        if (supplierId <= 0) {
            fieldErrors.put("supplierId", "Supplier is required");
        }

        // --- Parse lines ---
        List<PurchaseOrderLineDTO> lines = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            String vid = request.getParameter("lines[" + i + "].variantId");
            String qtyStr = request.getParameter("lines[" + i + "].qty"); // giống create
            String unitStr = request.getParameter("lines[" + i + "].unitPrice");

            // row trống -> bỏ
            if ((vid == null || vid.isBlank())
                    && (qtyStr == null || qtyStr.isBlank())
                    && (unitStr == null || unitStr.isBlank())) {
                continue;
            }

            // thiếu bắt buộc
            if (vid == null || vid.isBlank()) {
                fieldErrors.put("lines", "Variant is required");
                break;
            }
            if (qtyStr == null || qtyStr.isBlank()) {
                fieldErrors.put("lines", "Quantity is required");
                break;
            }

            try {
                long variantId = Long.parseLong(vid);
                BigDecimal qty = new BigDecimal(qtyStr);
                if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Quantity must be > 0");
                    break;
                }

                BigDecimal unitPrice = (unitStr == null || unitStr.isBlank()) ? null : new BigDecimal(unitStr);
                if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Unit Price must be > 0");
                    break;
                }

                PurchaseOrderLineDTO line = new PurchaseOrderLineDTO();
                // Lấy ID của dòng hàng cũ (nếu có) để DAO thực hiện update thay vì
                // delete-insert
                String poLineIdStr = request.getParameter("lines[" + i + "].poLineId");
                if (poLineIdStr != null && !poLineIdStr.isBlank()) {
                    line.setPoLineId(Long.parseLong(poLineIdStr));
                }
                line.setVariantId(variantId);
                line.setOrderedQty(qty);
                line.setUnitPrice(unitPrice);
                lines.add(line);

            } catch (Exception ex) {
                fieldErrors.put("lines", "Lines contains invalid numbers");
                break;
            }
        }

        if (lines.isEmpty()) {
            fieldErrors.putIfAbsent("lines", "At least one line is required");
        }

        // oldLines để giữ form khi lỗi
        List<Map<String, String>> oldLines = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            String productId = request.getParameter("lines[" + i + "].productId");
            String variantId = request.getParameter("lines[" + i + "].variantId");
            String qty = request.getParameter("lines[" + i + "].qty");
            String unitPrice = request.getParameter("lines[" + i + "].unitPrice");

            boolean allBlank = (productId == null || productId.isBlank())
                    && (variantId == null || variantId.isBlank())
                    && (qty == null || qty.isBlank())
                    && (unitPrice == null || unitPrice.isBlank());

            if (allBlank) {
                continue;
            }

            Map<String, String> row = new HashMap<>();
            row.put("productId", productId == null ? "" : productId);
            row.put("variantId", variantId == null ? "" : variantId);
            row.put("qty", qty == null ? "" : qty);
            row.put("unitPrice", unitPrice == null ? "" : unitPrice);
            oldLines.add(row);
        }

        // --- If errors -> forward edit form ---
        if (!fieldErrors.isEmpty()) {
            PurchaseOrderHeaderDTO po = new PurchaseOrderHeaderDTO();
            po.setPoId(poId);
            po.setPoNumber(poNumber);
            po.setSupplierId(supplierId);
            po.setExpectedDeliveryDate(expectedDate);
            po.setNote(note);
            po.setStatus(current.getStatus()); // Giữ status để JSP không set readonly/disabled khi validation lỗi

            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("po", po);
            request.setAttribute("lines", poService.getPurchaseOrderDetailLines(poId)); // hoặc bỏ nếu JSP dùng oldLines
            request.setAttribute("oldLines", oldLines);
            request.setAttribute("suppliers", sService.getActiveSuppliers());
            request.setAttribute("products", pService.getProducts());

            request.getRequestDispatcher(ViewPath.PO_FORM_EDIT).forward(request, response);
            return;
        }

        PurchaseOrderHeaderDTO header = new PurchaseOrderHeaderDTO();
        header.setPoId(poId);
        header.setPoNumber(poNumber);
        header.setSupplierId(supplierId);
        header.setExpectedDeliveryDate(expectedDate);
        header.setNote(note);

        try {
            poService.updatePurchaseOrder(header, lines);
            ToastUtil.setToast(request, "success", "Update Purchase Order successfully: " + poNumber);
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=detail&id=" + poId);
        } catch (IllegalArgumentException ex) {
            // DAO throws when status is neither CREATED nor IMPORTED
            ToastUtil.setToast(request, "error", "Purchase Order cannot be updated: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=edit&id=" + poId);
        } catch (Exception ex) {
            ToastUtil.setToast(request, "error", "Error updating PO: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/purchase-orders?action=edit&id=" + poId);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String page = request.getParameter("page");
        String redirectUrl = request.getContextPath() + "/purchase-orders";
        if (page != null && !page.isBlank()) {
            redirectUrl += "?page=" + page;
        }

        long poId;
        try {
            poId = Long.parseLong(request.getParameter("id"));
        } catch (Exception ex) {
            ToastUtil.setToast(request, "error", "Invalid Purchase Order id.");
            response.sendRedirect(redirectUrl);
            return;
        }

        PurchaseOrderHeaderDTO po = poService.getPurchaseOrderHeader(poId);
        if (po != null && "CLOSED".equalsIgnoreCase(po.getStatus())) {
            ToastUtil.setToast(request, "error", "Cannot delete Purchase Order with status CLOSED.");
            response.sendRedirect(redirectUrl);
            return;
        }
        // Business rule: PO cannot be deleted once a GRN exists (regardless of putaway)
        if (poService.hasAnyGrn(poId)) {
            ToastUtil.setToast(request, "error",
                    "Unable to delete PO because GRN is already available.");
            response.sendRedirect(redirectUrl);
            return;
        }

        try {
            boolean ok = poService.deletePurchaseOrder(poId);
            if (ok) {
                String poNumber = (po != null && po.getPoNumber() != null) ? po.getPoNumber() : ("#" + poId);
                ToastUtil.setToast(request, "success", "Delete Purchase Order successfully: " + poNumber);
            } else {
                ToastUtil.setToast(request, "error", "Purchase Order not found.");
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            ToastUtil.setToast(request, "error",
                    "Unable to update Purchase Order because GRN is already available.");
        } catch (java.sql.SQLException ex) {
            ToastUtil.setToast(request, "error",
                    "Cannot delete PO. ERROR IN DB: " + ex.getMessage());
        }

        response.sendRedirect(redirectUrl);
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
