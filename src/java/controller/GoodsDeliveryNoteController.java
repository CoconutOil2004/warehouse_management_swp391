package controller;

import dao.GoodsDeliveryNoteDAO;
import dao.PickWaveDAO;
import dao.SaleOrderDAO;
import dao.WarehouseDAO;
import dao.UserDAO;
import dto.GDNListDTO;
import dto.GDNDetailDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.ViewPath;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "GoodsDeliveryNoteController", urlPatterns = { "/goods-delivery-note" })
public class GoodsDeliveryNoteController extends HttpServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "list" -> handleList(request, response);
                case "create" -> handleCreateForm(request, response);
                case "detail" -> handleDetail(request, response);
                case "edit" -> response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + request.getParameter("id"));
                case "getSoDetails" -> handleGetSoDetails(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            }
        } catch (Exception e) {
            Logger.getLogger(GoodsDeliveryNoteController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        
        String gdnNumber = request.getParameter("gdnNumber");
        String soNumber = request.getParameter("soNumber");
        String status = request.getParameter("status");

        int page = parseInt(request.getParameter("page"), DEFAULT_PAGE);
        int size = DEFAULT_SIZE;
        int offset = (page - 1) * size;

        List<GDNListDTO> gdns = gdnDao.getGDNList(gdnNumber, soNumber, status, size, offset);
        int total = gdnDao.countGDN(gdnNumber, soNumber, status);
        int totalPages = (int) Math.ceil((double) total / size);
        if (totalPages < 1) {
            totalPages = 1;
        }

        request.setAttribute("gdns", gdns);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("gdnNumber", gdnNumber);
        request.setAttribute("soNumber", soNumber);
        request.setAttribute("status", status);

        request.getRequestDispatcher("WEB-INF/views/outbound/goods-delivery-note-list.jsp")
               .forward(request, response);
    }

    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SaleOrderDAO soDao = new SaleOrderDAO();
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        WarehouseDAO warehouseDao = new WarehouseDAO();

        // SOs with status CREATED that do NOT have a GDN yet (one SO = one GDN only)
        List<dto.SaleOrderListDTO> allCreated = soDao.searchSalesOrders(
            null, "CREATED", null, null, 500, 0);
        java.util.Set<Long> soIdsWithGdn = new java.util.HashSet<>(gdnDao.getSoIdsThatHaveGdn());
        List<dto.SaleOrderListDTO> salesOrders = allCreated.stream()
            .filter(so -> !soIdsWithGdn.contains(so.getSoId()))
            .collect(java.util.stream.Collectors.toList());

        request.setAttribute("salesOrders", salesOrders);
        request.setAttribute("warehouses", warehouseDao.getAll());

        request.getRequestDispatcher("WEB-INF/views/outbound/goods-delivery-note-create.jsp")
               .forward(request, response);
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        Long gdnId = parseLong(request.getParameter("id"), -1);
        
        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
        if (gdn == null) {
            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        request.setAttribute("gdn", gdn);
        request.setAttribute("wave", waveDao.getWaveByGdnId(gdnId));
        request.getRequestDispatcher("WEB-INF/views/outbound/goods-delivery-note-detail.jsp")
               .forward(request, response);
    }

    private void handleEditForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        Long gdnId = parseLong(request.getParameter("id"), -1);
        
        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
//        if (gdn == null || !("PENDING".equals(gdn.getStatus()) || "DRAFT".equals(gdn.getStatus()) || "ONGOING".equals(gdn.getStatus()))) {
//            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + gdnId);
//            return;
//        }

        request.setAttribute("gdn", gdn);
        request.getRequestDispatcher("WEB-INF/views/outbound/goods-delivery-note-edit.jsp")
               .forward(request, response);
    }

    private void handleGetSoDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SaleOrderDAO soDao = new SaleOrderDAO();
        String soNumber = request.getParameter("soNumber");
        
        if (soNumber == null || soNumber.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        dto.SaleOrderHeaderDTO so = soDao.getSaleOrderByNumber(soNumber);
        if (so == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<dto.SaleOrderLineDTO> lines = soDao.getSaleOrderDetailLines(so.getSoId());

        response.setContentType("application/json;charset=UTF-8");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"soId\":").append(so.getSoId()).append(",");
        sb.append("\"soNumber\":\"").append(escapeJson(so.getSoNumber())).append("\",");
        sb.append("\"customerId\":").append(so.getCustomerId() != null ? so.getCustomerId() : "null").append(",");
        sb.append("\"requestedShipDate\":\"").append(so.getRequestedShipDate() != null ? so.getRequestedShipDate().toString() : "").append("\",");
        sb.append("\"shipToAddress\":\"").append(escapeJson(so.getShipToAddress())).append("\",");
        sb.append("\"lines\":[");
        for (int i = 0; i < lines.size(); i++) {
            dto.SaleOrderLineDTO l = lines.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"soLineId\":").append(l.getSoLineId()).append(",");
            sb.append("\"variantId\":").append(l.getVariantId()).append(",");
            sb.append("\"variantSku\":\"").append(escapeJson(l.getVariantSku())).append("\",");
            sb.append("\"productName\":\"").append(escapeJson(l.getProductName())).append("\",");
            sb.append("\"color\":\"").append(escapeJson(l.getColor())).append("\",");
            sb.append("\"size\":\"").append(escapeJson(l.getSize())).append("\",");
            sb.append("\"qtyOrdered\":").append(l.getOrderedQty()).append(",");
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
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "create" -> handleCreate(request, response);
                case "update" -> handleUpdate(request, response);
                case "assign" -> handleAssign(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            }
        } catch (Exception e) {
            Logger.getLogger(GoodsDeliveryNoteController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        SaleOrderDAO soDao = new SaleOrderDAO();

        String soNumber = request.getParameter("soNumber");
        Long warehouseId = getWarehouseId(request);

        if (warehouseId == null) {
            request.setAttribute("error", "No warehouse found");
            handleCreateForm(request, response);
            return;
        }

        if (soNumber == null || soNumber.isBlank()) {
            request.setAttribute("error", "Please select a Sales Order");
            handleCreateForm(request, response);
            return;
        }

        dto.SaleOrderHeaderDTO so = soDao.getSaleOrderByNumber(soNumber.trim());
        if (so == null) {
            request.setAttribute("error", "Sales Order not found.");
            handleCreateForm(request, response);
            return;
        }

        if (gdnDao.getSoIdsThatHaveGdn().contains(so.getSoId())) {
            request.setAttribute("error", "This Sales Order already has a GDN.");
            handleCreateForm(request, response);
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long createdBy = user != null ? user.getUserId() : null;
        Long gdnId = gdnDao.createGDNFromSO(so.getSoId(), warehouseId, createdBy);

        if (gdnId == null) {
            request.setAttribute("error", "Failed to create GDN.");
            handleCreateForm(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + gdnId);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        Long gdnId = parseLong(request.getParameter("gdnId"), -1);
        String status = request.getParameter("status");

        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        String[] lineIds = request.getParameterValues("lineIds");
        String[] qtyPickedStrs = request.getParameterValues("qtyPicked");
        String[] qtyPackedStrs = request.getParameterValues("qtyPacked");

        // 1) Validate line quantities before save: Qty Packed <= Qty Picked, Qty Picked <= Available
        if (lineIds != null && qtyPickedStrs != null && qtyPackedStrs != null
                && lineIds.length == qtyPickedStrs.length && lineIds.length == qtyPackedStrs.length) {
            dto.GDNDetailDTO gdnForValidation = gdnDao.getGDNDetailById(gdnId);
            if (gdnForValidation != null && gdnForValidation.getLines() != null) {
                java.util.Map<Long, dto.GDNLineDTO> lineMap = new java.util.HashMap<>();
                for (dto.GDNLineDTO line : gdnForValidation.getLines()) {
                    lineMap.put(line.getGdnLineId(), line);
                }
                for (int i = 0; i < lineIds.length; i++) {
                    Long lineId = parseLong(lineIds[i], -1);
                    if (lineId <= 0) continue;
                    java.math.BigDecimal qtyPicked = parseBigDecimal(qtyPickedStrs[i], java.math.BigDecimal.ZERO);
                    java.math.BigDecimal qtyPacked = parseBigDecimal(qtyPackedStrs[i], java.math.BigDecimal.ZERO);
                    if (qtyPacked.compareTo(qtyPicked) > 0) {
                        response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + gdnId + "&error=Qty+Packed+cannot+exceed+Qty+Picked");
                        return;
                    }
                    dto.GDNLineDTO line = lineMap.get(lineId);
                    if (line != null) {
                        java.math.BigDecimal available = line.getQtyAvailable() != null ? line.getQtyAvailable() : java.math.BigDecimal.ZERO;
                        if (qtyPicked.compareTo(available) > 0) {
                            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + gdnId + "&error=Insufficient+inventory+for+" + (line.getVariantSku() != null ? line.getVariantSku() : "line"));
                            return;
                        }
                    }
                }
            }

            // 2) Save line quantities
            for (int i = 0; i < lineIds.length; i++) {
                Long lineId = parseLong(lineIds[i], -1);
                if (lineId > 0) {
                    java.math.BigDecimal qtyPicked = parseBigDecimal(qtyPickedStrs[i], java.math.BigDecimal.ZERO);
                    java.math.BigDecimal qtyPacked = parseBigDecimal(qtyPackedStrs[i], java.math.BigDecimal.ZERO);
                    gdnDao.updateGDNLineQuantities(lineId, qtyPicked, qtyPacked);
                }
            }
        }

        // 3) If switching to CONFIRMED: require Qty Picked = Qty Required and Qty Packed = Qty Required for all lines
        if ("CONFIRMED".equals(status)) {
            dto.GDNDetailDTO gdnAfterUpdate = gdnDao.getGDNDetailById(gdnId);
            if (gdnAfterUpdate != null && gdnAfterUpdate.getLines() != null) {
                for (dto.GDNLineDTO line : gdnAfterUpdate.getLines()) {
                    java.math.BigDecimal req = line.getQtyRequired() != null ? line.getQtyRequired() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal picked = line.getQtyPicked() != null ? line.getQtyPicked() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal packed = line.getQtyPacked() != null ? line.getQtyPacked() : java.math.BigDecimal.ZERO;
                    if (picked.compareTo(req) != 0 || packed.compareTo(req) != 0) {
                        response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + gdnId + "&error=Cannot+confirm%3A+Qty+Picked+and+Qty+Packed+must+equal+Qty+Required+for+all+lines");
                        return;
                    }
                }
            }
            gdnDao.updateGDNStatus(gdnId, "CONFIRMED");
            gdnDao.deductInventoryOnConfirm(gdnId);
        } else if (status != null && !status.isBlank()) {
            gdnDao.updateGDNStatus(gdnId, status);
        }

        response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=detail&id=" + gdnId);
    }

    private java.math.BigDecimal parseBigDecimal(String raw, java.math.BigDecimal def) {
        if (raw == null || raw.isBlank()) return def;
        try {
            return new java.math.BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void handleAssign(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // This will be handled by PickTaskController
        response.sendRedirect(request.getContextPath() + "/pick-task?action=assign&gdnId=" + request.getParameter("gdnId"));
    }

    private Long getWarehouseId(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("USER");
            if (userObj instanceof User) {
                User user = (User) userObj;
                if (user.getWarehouseId() != null) {
                    return user.getWarehouseId();
                }
            }
        }
        
        WarehouseDAO warehouseDao = new WarehouseDAO();
        List<model.Warehouse> warehouses = warehouseDao.getAll();
        if (warehouses != null && !warehouses.isEmpty()) {
            return warehouses.get(0).getWarehouseId();
        }
        
        return null;
    }

    private int parseInt(String raw, int def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw);
        } catch (Exception e) {
            return def;
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
