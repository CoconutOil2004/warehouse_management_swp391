package controller;

import dao.WarehouseDAO;
import dao.ZoneDAO;
import dao.SlotDAO;
import dao.InventoryBalanceDAO;
import dao.ProductVariantDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

@WebServlet(name = "WarehouseLayoutController", urlPatterns = {"/warehouse-layout"})
public class WarehouseLayoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            if ("view-zone".equals(action)) {
                forwardZoneView(request, response);
            } else if ("slot-detail".equals(action)) {
                handleSlotDetail(request, response);
            } else {
                forwardLayoutForm(request, response);
            }
        } catch (Exception ex) {
            Logger.getLogger(WarehouseLayoutController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
    // Chuyển null thành chuỗi trống hoặc "default" để switch không bị lỗi
    String currentAction = (action == null) ? "" : action;

    switch (currentAction) {
        case "create-zone":
            handleCreateZone(request, response);
            break;
            
        case "create-slots":
            handleCreateSlots(request, response);
            break;
            
        case "assign-product":
            handleAssignProduct(request, response);
            break;
            
        case "update-max-capacity":
            handleUpdateMaxCapacity(request, response);
            break;
            
        case "": // Tương đương với trường hợp action == null
        default:
            forwardLayoutForm(request, response);
            break;
    }
        } catch (Exception ex) {
            Logger.getLogger(WarehouseLayoutController.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("error", "An error occurred: " + ex.getMessage());
            try {
                forwardLayoutForm(request, response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    private void forwardLayoutForm(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Get default warehouse ID (from session or first warehouse)
        Long warehouseId = getDefaultWarehouseId(request);
        if (warehouseId == null) {
            request.setAttribute("error", "No warehouse found. Please contact administrator.");
            request.getRequestDispatcher("WEB-INF/views/warehouse/warehouse-layout.jsp").forward(request, response);
            return;
        }
        
        // Kiểm tra error từ URL parameter
        String errorParam = request.getParameter("error");
        if (errorParam != null && !errorParam.isBlank()) {
            request.setAttribute("error", java.net.URLDecoder.decode(errorParam, "UTF-8"));
        }
        
        request.setAttribute("selectedWarehouseId", warehouseId);
        
        ZoneDAO zoneDAO = new ZoneDAO();
        List<model.Zone> zones = zoneDAO.getZonesByWarehouseId(warehouseId);
        request.setAttribute("zones", zones);
        
        // Kiểm tra zone nào đã có slots
        SlotDAO slotDAO = new SlotDAO();
        Map<Long, Boolean> zoneHasSlots = new HashMap<>();
        for (model.Zone zone : zones) {
            zoneHasSlots.put(zone.getZoneId(), slotDAO.hasSlots(zone.getZoneId()));
        }
        request.setAttribute("zoneHasSlots", zoneHasSlots);
        
        request.getRequestDispatcher("WEB-INF/views/warehouse/warehouse-layout.jsp").forward(request, response);
    }
    
    /**
     * Get default warehouse ID from session user or first active warehouse
     */
    private Long getDefaultWarehouseId(HttpServletRequest request) throws SQLException {
        // Try to get from session user
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
        
        // Fallback: get first active warehouse
        WarehouseDAO warehouseDAO = new WarehouseDAO();
        List<model.Warehouse> warehouses = warehouseDAO.getAll();
        if (warehouses != null && !warehouses.isEmpty()) {
            return warehouses.get(0).getWarehouseId();
        }
        
        return null;
    }

    private void handleCreateZone(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Get default warehouse ID
        Long warehouseId = getDefaultWarehouseId(request);
        if (warehouseId == null) {
            request.setAttribute("error", "No warehouse found. Please contact administrator.");
            forwardLayoutForm(request, response);
            return;
        }
        
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String zoneType = request.getParameter("zoneType");
        
        // Validate input
        if (code == null || code.isBlank()) {
            request.setAttribute("error", "Zone code cannot be empty");
            forwardLayoutForm(request, response);
            return;
        }
        
        if (name == null || name.isBlank()) {
            request.setAttribute("error", "Zone name cannot be empty");
            forwardLayoutForm(request, response);
            return;
        }
        
        if (zoneType == null || zoneType.isBlank()) {
            request.setAttribute("error", "Please select zone type");
            forwardLayoutForm(request, response);
            return;
        }
        
        ZoneDAO zoneDAO = new ZoneDAO();
        try {
            zoneDAO.createZone(warehouseId, code, name, zoneType);
            request.getSession().setAttribute("message", "Zone created successfully.");
            response.sendRedirect(request.getContextPath() + "/warehouse-layout");
        } catch (Exception e) {
            // Hiển thị lỗi và quay lại form
            request.setAttribute("error", e.getMessage());
            forwardLayoutForm(request, response);
        }
    }

    private void handleCreateSlots(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        Long zoneId = Long.parseLong(request.getParameter("zoneId"));
        int rows = Integer.parseInt(request.getParameter("rows"));
        int cols = Integer.parseInt(request.getParameter("cols"));
        String codePrefix = request.getParameter("codePrefix");

        // Force prefix to match the zone code to prevent cross-zone codes
        ZoneDAO zoneDAO = new ZoneDAO();
        model.Zone zone = zoneDAO.getZoneById(zoneId);
        if (zone == null) {
            throw new Exception("Zone not found");
        }
        String expectedPrefix = zone.getCode() != null ? zone.getCode().trim().toUpperCase() : null;
        
        // Validate input
        if (rows <= 0 || rows > 100) {
            throw new Exception("Number of rows must be between 1 and 100");
        }
        if (cols <= 0 || cols > 100) {
            throw new Exception("Number of columns must be between 1 and 100");
        }

        if (expectedPrefix == null || expectedPrefix.isBlank()) {
            throw new Exception("Zone code is missing; cannot generate slot codes");
        }

        if (codePrefix == null || codePrefix.isBlank()) {
            codePrefix = expectedPrefix;
        } else {
            codePrefix = codePrefix.trim().toUpperCase();
            if (!expectedPrefix.equals(codePrefix)) {
                throw new Exception("Slot code prefix must match zone code (" + expectedPrefix + ")");
            }
        }
        
        // Validate codePrefix không được chứa ký tự đặc biệt
        if (!codePrefix.matches("^[A-Z0-9_-]+$")) {
            throw new Exception("Prefix can only contain letters, numbers, hyphens and underscores");
        }
        
        SlotDAO slotDAO = new SlotDAO();
        try {
            slotDAO.createSlotsBatch(zoneId, rows, cols, codePrefix);
            request.getSession().setAttribute("message", "Slots created successfully.");
            response.sendRedirect(request.getContextPath() + "/warehouse-layout");
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            response.sendRedirect(request.getContextPath() + "/warehouse-layout?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        }
    }

    private void forwardZoneView(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        Long zoneId = parseLong(request.getParameter("zoneId"), -1);
        
        if (zoneId <= 0) {
            forwardLayoutForm(request, response);
            return;
        }
        
        // Get default warehouse ID
        Long warehouseId = getDefaultWarehouseId(request);
        if (warehouseId == null) {
            request.setAttribute("error", "No warehouse found. Please contact administrator.");
            forwardLayoutForm(request, response);
            return;
        }
        
        ZoneDAO zoneDAO = new ZoneDAO();
        model.Zone zone = zoneDAO.getZoneById(zoneId);
        if (zone == null) {
            forwardLayoutForm(request, response);
            return;
        }
        
        SlotDAO slotDAO = new SlotDAO();
        request.setAttribute("slots", slotDAO.getSlotsWithInventoryByZoneId(zoneId, warehouseId));
        request.setAttribute("zone", zone);
        request.setAttribute("warehouseId", warehouseId);
        
        ProductVariantDAO variantDAO = new ProductVariantDAO();
        request.setAttribute("variants", variantDAO.getActiveVariants());
        
        request.getRequestDispatcher("WEB-INF/views/warehouse/zone-grid-view.jsp").forward(request, response);
    }

    private void handleSlotDetail(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        response.setContentType("application/json;charset=UTF-8");
        
        Long slotId = parseLong(request.getParameter("slotId"), -1);
        
        if (slotId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Invalid slot ID\"}");
            }
            return;
        }
        
        // Get default warehouse ID
        Long warehouseId = getDefaultWarehouseId(request);
        if (warehouseId == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"No warehouse found\"}");
            }
            return;
        }
        
        SlotDAO slotDAO = new SlotDAO();
        dto.SlotDetailDTO slot = slotDAO.getSlotDetail(slotId, warehouseId);
        
        if (slot == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Slot not found\"}");
            }
            return;
        }
        
        // Tạo JSON thủ công
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"slotId\":").append(slot.getSlotId()).append(",");
        json.append("\"slotCode\":\"").append(escapeJson(slot.getSlotCode())).append("\",");
        json.append("\"status\":\"").append(escapeJson(slot.getStatus())).append("\",");
        json.append("\"isEmpty\":").append(slot.getIsEmpty()).append(",");
        json.append("\"usedCapacity\":").append(slot.getUsedCapacity() != null ? slot.getUsedCapacity().toString() : "0").append(",");
        json.append("\"availableCapacity\":").append(slot.getAvailableCapacity() != null ? slot.getAvailableCapacity().toString() : "0").append(",");
        json.append("\"maxCapacity\":").append(slot.getMaxCapacity() != null ? slot.getMaxCapacity().toString() : "null").append(",");
        json.append("\"products\":[");
        
        List<dto.SlotProductDTO> products = slot.getProducts();
        if (products != null) {
            for (int i = 0; i < products.size(); i++) {
                dto.SlotProductDTO p = products.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"variantId\":").append(p.getVariantId()).append(",");
                json.append("\"variantSku\":\"").append(escapeJson(p.getVariantSku())).append("\",");
                json.append("\"productName\":\"").append(escapeJson(p.getProductName())).append("\",");
                json.append("\"productSku\":\"").append(escapeJson(p.getProductSku())).append("\",");
                json.append("\"condition\":\"").append(escapeJson(p.getCondition())).append("\",");
                json.append("\"qtyOnHand\":").append(p.getQtyOnHand() != null ? p.getQtyOnHand().toString() : "0").append(",");
                json.append("\"qtyAvailable\":").append(p.getQtyAvailable() != null ? p.getQtyAvailable().toString() : "0");
                json.append("}");
            }
        }
        
        json.append("]}");
        
        try (PrintWriter out = response.getWriter()) {
            out.write(json.toString());
        }
    }

    private void handleAssignProduct(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Get default warehouse ID
        Long warehouseId = getDefaultWarehouseId(request);
        if (warehouseId == null) {
            request.setAttribute("error", "No warehouse found. Please contact administrator.");
            forwardLayoutForm(request, response);
            return;
        }
        
        Long slotId = Long.parseLong(request.getParameter("slotId"));
        Long variantId = Long.parseLong(request.getParameter("variantId"));
        String condition = request.getParameter("condition");
        if (condition == null || condition.isBlank()) {
            condition = "GOOD";
        }
        BigDecimal qty = new BigDecimal(request.getParameter("qty"));
        
        // Validate capacity before assigning
        InventoryBalanceDAO invDAO = new InventoryBalanceDAO();
        SlotDAO slotDAO = new SlotDAO();
        
        // Get current slot capacity info
        dto.SlotDetailDTO slotDetail = slotDAO.getSlotDetail(slotId, warehouseId);
        if (slotDetail != null && slotDetail.getMaxCapacity() != null) {
            BigDecimal currentUsed = slotDetail.getUsedCapacity() != null ? slotDetail.getUsedCapacity() : BigDecimal.ZERO;
            BigDecimal newTotal = currentUsed.add(qty);
            
            if (newTotal.compareTo(slotDetail.getMaxCapacity()) > 0) {
                request.setAttribute("error", "Cannot assign product: Total quantity (" + newTotal + ") would exceed max capacity (" + slotDetail.getMaxCapacity() + ")");
                forwardLayoutForm(request, response);
                return;
            }
        }
        
        invDAO.assignProductToSlot(warehouseId, slotId, variantId, condition, qty);
        request.getSession().setAttribute("message", "Product assigned to slot successfully.");
        response.sendRedirect(request.getContextPath() + "/warehouse-layout?action=view-zone&zoneId=" 
            + request.getParameter("zoneId"));
    }
    
    private void handleUpdateMaxCapacity(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        response.setContentType("application/json;charset=UTF-8");
        
        Long slotId = parseLong(request.getParameter("slotId"), -1);
        if (slotId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"success\":false,\"error\":\"Invalid slot ID\"}");
            }
            return;
        }
        
        String maxCapacityStr = request.getParameter("maxCapacity");
        BigDecimal maxCapacity = null;
        if (maxCapacityStr != null && !maxCapacityStr.isBlank()) {
            try {
                maxCapacity = new BigDecimal(maxCapacityStr);
                if (maxCapacity.compareTo(BigDecimal.ZERO) < 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.write("{\"success\":false,\"error\":\"Max capacity must be greater than or equal to 0\"}");
                    }
                    return;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.write("{\"success\":false,\"error\":\"Invalid max capacity value\"}");
                }
                return;
            }
        }
        
        // Check if new capacity is less than used capacity
        InventoryBalanceDAO invDAO = new InventoryBalanceDAO();
        BigDecimal usedCapacity = invDAO.getTotalCapacityUsed(slotId);
        if (maxCapacity != null && usedCapacity != null && usedCapacity.compareTo(maxCapacity) > 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"success\":false,\"error\":\"Max capacity cannot be less than used capacity (" + usedCapacity + ")\"}");
            }
            return;
        }
        
        SlotDAO slotDAO = new SlotDAO();
        boolean success = slotDAO.updateMaxCapacity(slotId, maxCapacity);
        
        if (success) {
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"success\":true,\"message\":\"Max capacity updated successfully\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"success\":false,\"error\":\"Failed to update max capacity\"}");
            }
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
