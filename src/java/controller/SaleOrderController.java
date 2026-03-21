/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.CustomerDAO;
import dto.ProductVariantDTO;
import dto.SOLineCreateDTO;
import dto.SaleOrderHeaderDTO;
import dto.SaleOrderLineDTO;
import dto.SaleOrderListDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.User;
import service.ProductVariantService;
import service.SaleOrderImportService;
import service.SaleOrderService;
import util.RequestUtil;
import util.ToastUtil;
import util.ViewPath;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10485760, maxRequestSize = 20971520)
@WebServlet(name = "SaleOrderController", urlPatterns = { "/sales-orders" })
public class SaleOrderController extends HttpServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 5;
    private final SaleOrderService soService = new SaleOrderService();
    private final SaleOrderImportService soImportService = new SaleOrderImportService();
    private final CustomerDAO customerDao = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if (action == null) {
                action = "";
            }
            switch (action) {
                case "create" ->
                    forwardCreateForm(request, response);
                case "edit" ->
                    forwardEditForm(request, response);
                case "import" ->
                    forwardImportForm(request, response);
                case "variants" ->
                    handleGetVariants(request, response);
                default ->
                    forwardList(request, response);
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
                case "create" ->
                    handleCreate(request, response);
                case "update" ->
                    handleUpdate(request, response);
                case "detail" ->
                    forwardDetail(request, response);
                case "delete" ->
                    handleDelete(request, response);
                case "processImport" ->
                    handleProcessImport(request, response);
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
        List<ProductVariantDTO> list = vService.listByProductId(productId);

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            ProductVariantDTO v = list.get(i);
            if (i > 0)
                sb.append(",");
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

    private String esc(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void forwardList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int page = RequestUtil.parseInt(request.getParameter("page"), DEFAULT_PAGE);
        int size = RequestUtil.parseInt(request.getParameter("size"), DEFAULT_SIZE);
        if (page < 1) {
            page = 1;
        }

        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        if (keyword != null) {
            keyword = keyword.trim();
        }
        if (status != null && status.isBlank()) {
            status = null;
        }

        Date fromDate = RequestUtil.parseSqlDate(fromDateStr);
        Date toDate = RequestUtil.parseSqlDate(toDateStr);

        int totalRecords = soService.countSalesOrders(keyword, status, fromDate, toDate);
        int totalPages = (int) Math.ceil((double) totalRecords / size);

        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        int offset = (page - 1) * size;
        List<SaleOrderListDTO> sos = soService.searchSalesOrders(keyword, status, fromDate, toDate, size, offset);

        // UI dùng <t:pagination ... include=\"...\"> để giữ filter khi đổi trang
        request.setAttribute("sos", sos);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("size", size);
        request.setAttribute("total", totalRecords);

        request.getRequestDispatcher(ViewPath.SO_LIST).forward(request, response);
    }

    private void forwardDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long soId = RequestUtil.parseLong(request.getParameter("id"), -1L);
        if (soId <= 0) {
            ToastUtil.setToast(request, "error", "Invalid Sales Order id.");
            response.sendRedirect(request.getContextPath() + "/sales-orders?action=list");
            return;
        }

        SaleOrderHeaderDTO header = soService.getSaleOrderHeader(soId);
        if (header == null) {
            ToastUtil.setToast(request, "error", "Sales Order not found.");
            response.sendRedirect(request.getContextPath() + "/sales-orders?action=list");
            return;
        }

        List<SaleOrderLineDTO> lines = soService.getSaleOrderDetailLines(soId);

        request.setAttribute("soId", soId);
        request.setAttribute("soHeader", header);
        request.setAttribute("lines", lines);

        request.getRequestDispatcher(ViewPath.SO_DETAIL).forward(request, response);
    }

    private void forwardCreateForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("customers", customerDao.getActiveCustomers());
        request.setAttribute("products", new service.ProductService().getProducts());
        request.getRequestDispatcher(ViewPath.SO_FORM_CREATE).forward(request, response);
    }

    private void forwardImportForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getRequestDispatcher(ViewPath.SO_FORM_IMPORT).forward(request, response);
    }

    private void handleProcessImport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                ToastUtil.setToast(request, "error", "Please select a file to upload.");
                forwardImportForm(request, response);
                return;
            }

            HttpSession session = request.getSession(false);
            User sessionUser = (session != null) ? (User) session.getAttribute("USER") : null;
            if (sessionUser == null) {
                response.sendRedirect(request.getContextPath() + "/authen");
                return;
            }
            long userId = sessionUser.getUserId();

            SaleOrderImportService.ImportResult result = soImportService.importFromExcel(filePart, userId);

            if (result.hasErrors()) {
                StringBuilder errMsg = new StringBuilder("Import failed due to the following errors: <ul>");
                for (String err : result.getFieldErrors().values()) {
                    errMsg.append("<li>").append(err).append("</li>");
                }
                errMsg.append("</ul>");
                ToastUtil.setToast(request, "error", errMsg.toString());
            } else {
                ToastUtil.setToast(request, "success", "Successfully imported Sale Order: " + result.getSoNumber());
            }
            forwardImportForm(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.setToast(request, "error", "Error processing Excel file: " + e.getMessage());
            forwardImportForm(request, response);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.setCharacterEncoding("UTF-8");
        Map<String, String> fieldErrors = new HashMap<>();
        String soNumber = request.getParameter("soNumber");
        String customerStr = request.getParameter("customerId");
        long customerId = (customerStr == null || customerStr.isBlank()) ? 0L : Long.parseLong(customerStr);
        String shipToAddress = request.getParameter("shipToAddress");
        if (shipToAddress == null || shipToAddress.isBlank()) {
            fieldErrors.put("shipToAddress", "Need enter address");
        } else if (shipToAddress.length() >= 20) {
            fieldErrors.put("shipToAddress", "Address lenght must in range 20");
        }
        String requestedShipStr = request.getParameter("requestedShipDate");
        Date requestedShipDate = null;
        if (requestedShipStr == null || requestedShipStr.isBlank()) {
            fieldErrors.put("requestedShipDates", "Request Delivery Date is required");
        } else {
            try {
                requestedShipDate = Date.valueOf(requestedShipStr); // yyyy-MM-dd
                // không được hôm nay hoặc quá khứ => phải > today
                // toLocateDate() bỏ giờ lấy ngày
                if (!requestedShipDate.toLocalDate().isAfter(java.time.LocalDate.now())) {
                    fieldErrors.put("requestedShipDate", "Requested ship Date must be after today");
                }
            } catch (Exception e) {
                fieldErrors.put("requestedShipDate", "Invalid date format");
            }
        }
        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("USER") : null;
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/authen");
            return;
        }
        long userId = sessionUser.getUserId();

        if (soNumber == null || soNumber.isBlank()) {
            fieldErrors.put("soNumber", "SO Number is required");
        } else if (soNumber.length() > 20) {
            fieldErrors.put("soNumber", "SO Number must be at most 20 characters");
        } else if (soService.existsBySoNumber(soNumber)) {
            fieldErrors.put("soNumber", "SO Number already exists");
        }

        if (customerId <= 0) {
            fieldErrors.put("customerId", "Customer is required");
        }

        List<SOLineCreateDTO> lines = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            String vid = request.getParameter("lines[" + i + "].variantId");
            String qtyStr = request.getParameter("lines[" + i + "].qty");

            if ((vid == null || vid.isBlank())
                    && (qtyStr == null || qtyStr.isBlank())) {
                continue;
            }

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
                java.math.BigDecimal qty = new java.math.BigDecimal(qtyStr);
                if (qty.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Quantity must be > 0");
                    break;
                }

                String unitStr = request.getParameter("lines[" + i + "].unitPrice");
                java.math.BigDecimal unitPrice = (unitStr == null || unitStr.isBlank())
                        ? null
                        : new java.math.BigDecimal(unitStr);
                if (unitPrice != null && unitPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Unit Price must be > 0");
                    break;
                }

                lines.add(new SOLineCreateDTO(variantId, qty, unitPrice, null));
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

        request.setAttribute("oldLines", oldLines);

        if (!fieldErrors.isEmpty()) {
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("oldSoNumber", soNumber);
            request.setAttribute("oldCustomerId", customerId);
            request.setAttribute("oldShipToAddress", shipToAddress);
            request.setAttribute("oldRequestedShipDate", requestedShipStr);
            request.setAttribute("customers", customerDao.getActiveCustomers());
            request.setAttribute("products", new service.ProductService().getProducts());

            request.getRequestDispatcher(ViewPath.SO_FORM_CREATE).forward(request, response);
            return;
        }

        // Tạo SO + giữ chỗ tồn kho (DAO sẽ check inventory & reservation)
        try {
            soService.createManualSO(soNumber, customerId, requestedShipDate, shipToAddress, userId, lines);
            ToastUtil.setToast(request, "success", "Create Sales Order successfully: " + soNumber);
            response.sendRedirect(request.getContextPath() + "/sales-orders");
        } catch (IllegalArgumentException ex) {
            // Các lỗi business như thiếu tồn kho, không có inventory_summary,...
            fieldErrors.put("lines", ex.getMessage());

            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("oldSoNumber", soNumber);
            request.setAttribute("oldCustomerId", customerId);
            request.setAttribute("oldShipToAddress", shipToAddress);
            request.setAttribute("oldRequestedShipDate", requestedShipStr);
            request.setAttribute("customers", customerDao.getActiveCustomers());
            request.setAttribute("products", new service.ProductService().getProducts());
            request.setAttribute("oldLines", oldLines);

            request.getRequestDispatcher(ViewPath.SO_FORM_CREATE).forward(request, response);
        }
    }

    private void forwardEditForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long soId = RequestUtil.parseLong(request.getParameter("id"), -1L);
        if (soId <= 0) {
            ToastUtil.setToast(request, "error", "Invalid Sales Order id.");
            response.sendRedirect(request.getContextPath() + "/sales-orders");
            return;
        }
        SaleOrderHeaderDTO so = soService.getSaleOrderHeader(soId);
        if (so == null) {
            ToastUtil.setToast(request, "error", "Sales Order not found.");
            response.sendRedirect(request.getContextPath() + "/sales-orders");
            return;
        }

        List<SaleOrderLineDTO> lines = soService.getSaleOrderDetailLines(soId);

        request.setAttribute("so", so);
        request.setAttribute("lines", lines);
        request.setAttribute("customers", customerDao.getActiveCustomers());
        request.setAttribute("products", new service.ProductService().getProducts());

        request.getRequestDispatcher(ViewPath.SO_FORM_EDIT).forward(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        request.setCharacterEncoding("UTF-8");
        Map<String, String> fieldErrors = new HashMap<>();

        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("USER") : null;
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/authen");
            return;
        }
        long userId = sessionUser.getUserId();

        long soId = RequestUtil.parseLong(request.getParameter("soId"), -1L);
        if (soId <= 0) {
            response.sendRedirect(request.getContextPath() + "/sales-orders");
            return;
        }

        SaleOrderHeaderDTO current = soService.getSaleOrderHeader(soId);
        if (current == null) {
            ToastUtil.setToast(request, "error", "Sales Order not found.");
            response.sendRedirect(request.getContextPath() + "/sales-orders");
            return;
        }

        String soNumber = request.getParameter("soNumber");
        String customerStr = request.getParameter("customerId");
        long customerId = (customerStr == null || customerStr.isBlank()) ? 0L : Long.parseLong(customerStr);

        String requestedShipStr = request.getParameter("requestedShipDate");
        Date requestedShipDate = null;
        if (requestedShipStr == null || requestedShipStr.isBlank()) {
            fieldErrors.put("requestedShipDate", "Requested Ship Date is required");
        } else {
            try {
                requestedShipDate = Date.valueOf(requestedShipStr);
                if (!requestedShipDate.toLocalDate().isAfter(java.time.LocalDate.now())) {
                    fieldErrors.put("requestedShipDate", "Requested Ship Date must be after today");
                }
            } catch (Exception e) {
                fieldErrors.put("requestedShipDate", "Invalid date format");
            }
        }

        String shipToAddress = request.getParameter("shipToAddress");

        if (soNumber == null || soNumber.isBlank()) {
            fieldErrors.put("soNumber", "SO Number is required");
        } else if (soNumber.length() > 20) {
            fieldErrors.put("soNumber", "SO Number must be at most 20 characters");
        } else {
            if (!soNumber.equalsIgnoreCase(current.getSoNumber())) {
                if (soService.existsBySoNumber(soNumber)) {
                    fieldErrors.put("soNumber", "SO Number already exists");
                }
            }
        }

        if (customerId <= 0) {
            fieldErrors.put("customerId", "Customer is required");
        }

        List<SaleOrderLineDTO> lines = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            String vid = request.getParameter("lines[" + i + "].variantId");
            String qtyStr = request.getParameter("lines[" + i + "].qty");
            String unitStr = request.getParameter("lines[" + i + "].unitPrice");

            if ((vid == null || vid.isBlank())
                    && (qtyStr == null || qtyStr.isBlank())
                    && (unitStr == null || unitStr.isBlank())) {
                continue;
            }

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
                java.math.BigDecimal qty = new java.math.BigDecimal(qtyStr);
                if (qty.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Quantity must be > 0");
                    break;
                }

                java.math.BigDecimal unitPrice = (unitStr == null || unitStr.isBlank())
                        ? null
                        : new java.math.BigDecimal(unitStr);
                if (unitPrice != null && unitPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    fieldErrors.put("lines", "Unit Price must be > 0");
                    break;
                }

                SaleOrderLineDTO line = new SaleOrderLineDTO();
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

        if (!fieldErrors.isEmpty()) {
            SaleOrderHeaderDTO so = new SaleOrderHeaderDTO();
            so.setSoId(soId);
            so.setSoNumber(soNumber);
            so.setCustomerId(customerId);
            if (requestedShipDate != null) {
                so.setRequestedShipDate(requestedShipDate.toLocalDate());
            } else {
                so.setRequestedShipDate(null);
            }
            so.setShipToAddress(shipToAddress);

            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("so", so);
            request.setAttribute("lines", soService.getSaleOrderDetailLines(soId));
            request.setAttribute("oldLines", oldLines);
            request.setAttribute("customers", customerDao.getActiveCustomers());
            request.setAttribute("products", new service.ProductService().getProducts());

            request.getRequestDispatcher(ViewPath.SO_FORM_EDIT).forward(request, response);
            return;
        }

        SaleOrderHeaderDTO header = new SaleOrderHeaderDTO();
        header.setSoId(soId);
        header.setSoNumber(soNumber);
        header.setCustomerId(customerId);
        if (requestedShipDate != null) {
            header.setRequestedShipDate(requestedShipDate.toLocalDate());
        } else {
            header.setRequestedShipDate(null);
        }
        header.setShipToAddress(shipToAddress);

        try {
            // Cập nhật SO + điều chỉnh reservation tồn kho
            soService.updateSalesOrder(header, lines, userId);
            ToastUtil.setToast(request, "success", "Update Sales Order successfully: " + header.getSoNumber());
            response.sendRedirect(request.getContextPath() + "/sales-orders?action=detail&id=" + soId);
        } catch (IllegalArgumentException ex) {
            // Lỗi business: tồn kho không đủ, thiếu inventory_summary,...
            fieldErrors.put("lines", ex.getMessage());

            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("so", header);
            request.setAttribute("lines", soService.getSaleOrderDetailLines(soId));
            request.setAttribute("oldLines", oldLines);
            request.setAttribute("customers", customerDao.getActiveCustomers());
            request.setAttribute("products", new service.ProductService().getProducts());

            request.getRequestDispatcher(ViewPath.SO_FORM_EDIT).forward(request, response);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        long soId = RequestUtil.parseLong(request.getParameter("id"), -1L);
        if (soId <= 0) {
            ToastUtil.setToast(request, "error", "Invalid Sales Order id.");
            response.sendRedirect(request.getContextPath() + "/sales-orders");
            return;
        }

        SaleOrderHeaderDTO so = soService.getSaleOrderHeader(soId);
        if (so != null && "CLOSED".equalsIgnoreCase(so.getStatus())) {
            ToastUtil.setToast(request, "error", "Cannot delete Sales Order with status CLOSED.");
            String page = request.getParameter("page");
            String redirectUrl = request.getContextPath() + "/sales-orders";
            redirectUrl += (page != null && !page.isBlank()) ? "?page=" + page : "";
            response.sendRedirect(redirectUrl);
            return;
        }

        boolean ok = soService.deleteSalesOrder(soId);
        if (ok) {
            ToastUtil.setToast(request, "success", "Delete Sales Order successfully.");
        } else {
            ToastUtil.setToast(request, "error", "Sales Order not found.");
        }
        String page = request.getParameter("page");
        String redirectUrl = request.getContextPath() + "/sales-orders";
        redirectUrl += (page != null && !page.isBlank()) ? "?page=" + page : "";
        response.sendRedirect(redirectUrl);
    }

    @Override
    public String getServletInfo() {
        return "Sale Order Controller";
    }
}
