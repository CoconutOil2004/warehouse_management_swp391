package controller;

import dao.GoodsDeliveryNoteDAO;
import dao.PickTaskDAO;
import dao.PickWaveDAO;
import dao.ZoneDAO;
import dto.PickTaskDTO;
import dto.PickWaveDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

@WebServlet(name = "PickWaveController", urlPatterns = {"/pick-wave"})
public class PickWaveController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "list" -> handleList(request, response);
                case "detail" -> handleDetail(request, response);
                case "create" -> handleCreateForm(request, response);
                case "add-gdn" -> handleAddGdn(request, response);
                case "remove-gdn" -> handleRemoveGdn(request, response);
                case "release" -> handleRelease(request, response);
                case "cancel" -> handleCancel(request, response);
                default -> response.sendRedirect(
                    request.getContextPath() + "/pick-wave?action=list"
                    );
            }
        } catch (Exception e) {
            Logger.getLogger(PickWaveController.class.getName()).log(
            Level.SEVERE,
            "Error in doGet",
            e
            );
            throw new ServletException(e);
        }
    }

    private void handleList(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        String status = request.getParameter("status");
        int page = (int) parseLong(request.getParameter("page"), 1);
        int pageSize = (int) parseLong(request.getParameter("size"), 10);
        int offset = (page - 1) * pageSize;

        PickWaveDAO waveDao = new PickWaveDAO();
        List<PickWaveDTO> waves = waveDao.getWaveList(status, pageSize, offset);
        int totalWaves = waveDao.countWaves(status);
        int totalPages = (int) Math.ceil((double) totalWaves / pageSize);

        request.setAttribute("waves", waves);
        request.setAttribute("status", status);
        request.setAttribute("page", (long) page);
        request.setAttribute("pages", (long) totalPages);
        request.setAttribute("size", (long) pageSize);
        request.setAttribute("total", (long) totalWaves);
        request
        .getRequestDispatcher("/WEB-INF/views/outbound/pick-wave-list.jsp")
        .forward(request, response);
    }

    /**
     * Hiển thị màn hình chọn GDN để tạo Pick Wave. Chỉ ADMIN /
     * WAREHOUSE_MANAGER mới được phép truy cập. Cải tiến: Hỗ trợ filter theo
     * zone và chọn nhiều GDN.
     */
    private void handleCreateForm(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        PickWaveDAO waveDao = new PickWaveDAO();
        ZoneDAO zoneDao = new ZoneDAO();

        // Get filter parameters
        String gdnNumber = request.getParameter("gdnNumber");
        String soNumber = request.getParameter("soNumber");
        String status = request.getParameter("status");
        if (status == null || status.isBlank()) {
            // Default to newly created GDNs
            status = "CREATED";
        }

        // Get selected zones (multi-select)
        String[] zoneIdsParam = request.getParameterValues("zoneIds");
        List<Long> selectedZoneIds = new java.util.ArrayList<>();
        if (zoneIdsParam != null) {
            for (String id : zoneIdsParam) {
                try {
                    selectedZoneIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // Ignore invalid zone IDs
                }
            }
        }

        int page = (int) parseLong(request.getParameter("page"), 1);
        int pageSize = 100;
        int offset = (page - 1) * pageSize;

        // Get GDNs filtered by zones if zones are selected
        java.util.List<dto.GDNListDTO> gdns;
        int totalGdns;
        if (!selectedZoneIds.isEmpty()) {
            gdns = waveDao.getGdnsByZoneFilter(
            selectedZoneIds,
            status,
            pageSize,
            offset
            );
            totalGdns = waveDao.countGdnsByZoneFilter(selectedZoneIds, status);
        } else {
            gdns = gdnDao.getGDNList(gdnNumber, soNumber, status, pageSize, offset);
            totalGdns = gdnDao.countGDN(gdnNumber, soNumber, status);
        }

        int totalPages = (int) Math.ceil((double) totalGdns / pageSize);

        // Get all zones for filter dropdown
        request.setAttribute("zones", zoneDao.getAllZones());
        request.setAttribute("selectedZoneIds", selectedZoneIds);

        request.setAttribute("gdns", gdns);
        request.setAttribute("gdnNumber", gdnNumber);
        request.setAttribute("soNumber", soNumber);
        request.setAttribute("status", status);
        request.setAttribute("page", (long) page);
        request.setAttribute("pages", (long) totalPages);
        request.setAttribute("total", (long) totalGdns);

        request
        .getRequestDispatcher("/WEB-INF/views/outbound/pick-wave-create.jsp")
        .forward(request, response);
    }

    private void handleDetail(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        long waveId = parseLong(request.getParameter("id"), -1);
        if (waveId <= 0) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }
        PickWaveDAO waveDao = new PickWaveDAO();
        PickWaveDTO wave = waveDao.getWaveById(waveId);
        if (wave == null) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }
        PickTaskDAO taskDao = new PickTaskDAO();
        request.setAttribute("wave", wave);
        request.setAttribute("tasks", taskDao.getTasksByWaveId(waveId));
        request.setAttribute("unassignedCount", taskDao.countUnassignedLinesByWave(waveId));
        request
        .getRequestDispatcher("/WEB-INF/views/outbound/pick-wave-detail.jsp")
        .forward(request, response);
    }

    @Override
    protected void doPost(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            try {
                handleCreate(request, response);
            } catch (Exception e) {
                Logger.getLogger(PickWaveController.class.getName()).log(
                Level.SEVERE,
                "Error in doPost (create action)",
                e
                );
                throw new ServletException(e);
            }
        } else if ("release".equals(action)) {
            try {
                handleRelease(request, response);
            } catch (Exception e) {
                Logger.getLogger(PickWaveController.class.getName()).log(
                Level.SEVERE,
                "Error in doPost (release action)",
                e
                );
                throw new ServletException(e);
            }
        } else if ("cancel".equals(action)) {
            try {
                handleCancel(request, response);
            } catch (Exception e) {
                Logger.getLogger(PickWaveController.class.getName()).log(
                Level.SEVERE,
                "Error in doPost (cancel action)",
                e
                );
                throw new ServletException(e);
            }
        } else {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
        }
    }

    /**
     * Create wave from multiple GDNs: insert pick_wave, link GDNs via
     * pick_wave_gdn, create tasks from wave (by zone/slot), update GDN ONGOING,
     * redirect to assign.
     */
    private void handleCreate(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        // Get multiple GDN IDs (checkbox selection)
        String[] gdnIdsParam = request.getParameterValues("gdnId");
        if (gdnIdsParam == null || gdnIdsParam.length == 0) {
            response.sendRedirect(
            request.getContextPath()
            + "/pick-wave?action=create&error=no_gdn_selected"
            );
            return;
        }

        List<Long> gdnIds = new java.util.ArrayList<>();
        for (String id : gdnIdsParam) {
            try {
                gdnIds.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid IDs
            }
        }

        if (gdnIds.isEmpty()) {
            response.sendRedirect(
            request.getContextPath()
            + "/pick-wave?action=create&error=no_gdn_selected"
            );
            return;
        }

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        PickWaveDAO waveDao = new PickWaveDAO();

        // Validate all GDNs
        Long warehouseId = null;
        for (Long gdnId : gdnIds) {
            dto.GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
            if (gdn == null) {
                response.sendRedirect(
                request.getContextPath() + "/goods-delivery-note?action=list"
                );
                return;
            }

            // Validate status: only CREATED GDNs can start picking
            if (!"CREATED".equalsIgnoreCase(gdn.getStatus())) {
                request.setAttribute("gdn", gdn);
                request.setAttribute(
                "error",
                "Pick wave can only be created for GDN in CREATED status."
                );
                request
                .getRequestDispatcher(
                "WEB-INF/views/outbound/goods-delivery-note-detail.jsp"
                )
                .forward(request, response);
                return;
            }

            // Validate warehouse
            if (gdn.getWarehouseId() == null) {
                request.setAttribute("gdn", gdn);
                request.setAttribute(
                "error",
                "GDN has no warehouse assigned. Please check the configuration."
                );
                request
                .getRequestDispatcher(
                "WEB-INF/views/outbound/goods-delivery-note-detail.jsp"
                )
                .forward(request, response);
                return;
            }

            // All GDNs should have same warehouse
            if (warehouseId == null) {
                warehouseId = gdn.getWarehouseId();
            } else if (!warehouseId.equals(gdn.getWarehouseId())) {
                request.setAttribute(
                "error",
                "All GDNs must belong to the same warehouse."
                );
                request.setAttribute("gdns", gdnIds);
                request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-wave-create.jsp")
                .forward(request, response);
                return;
            }

            // Validate inventory
            if (gdn.getLines() == null || gdn.getLines().isEmpty()) {
                request.setAttribute("gdn", gdn);
                request.setAttribute(
                "error",
                "GDN " + gdn.getGdnNumber() + " has no lines to create a pick wave."
                );
                request
                .getRequestDispatcher(
                "WEB-INF/views/outbound/goods-delivery-note-detail.jsp"
                )
                .forward(request, response);
                return;
            }

            boolean insufficientTotalStock = gdn
            .getLines()
            .stream()
            .anyMatch(
            l
            -> l.getQtyRequired() != null
            && l.getQtyRequired().compareTo(java.math.BigDecimal.ZERO) > 0
            && (l.getQtyAvailable() == null
            || l.getQtyAvailable().compareTo(l.getQtyRequired()) < 0)
            );
            if (insufficientTotalStock) {
                request.setAttribute("gdn", gdn);
                request.setAttribute(
                "error",
                "Total inventory is not enough for GDN "
                + gdn.getGdnNumber()
                + ". Please check requested quantity and inventory."
                );
                request
                .getRequestDispatcher(
                "WEB-INF/views/outbound/goods-delivery-note-detail.jsp"
                )
                .forward(request, response);
                return;
            }

            // Check if wave already exists for this GDN (via pick_wave or pick_wave_gdn)
            if (waveDao.getWaveIdByGdnId(gdnId) != null) {
                response.sendRedirect(
                request.getContextPath()
                + "/goods-delivery-note?action=detail&id="
                + gdnId
                );
                return;
            }
        }

        Long createdBy = user.getUserId();

        Logger.getLogger(PickWaveController.class.getName()).log(Level.INFO, 
            "Creating pick wave with " + gdnIds.size() + " GDNs: " + gdnIds);

        // Create wave with multiple GDNs (includes linking GDNs)
        Long waveId = waveDao.createWave(gdnIds, createdBy);
        if (waveId == null) {
            response.sendRedirect(
            request.getContextPath()
            + "/pick-wave?action=create&error=create_failed"
            );
            return;
        }

        // Create tasks from wave
        PickTaskDAO taskDao = new PickTaskDAO();
        boolean created;
        try {
            created = taskDao.createTasksFromWave(waveId);
        } catch (Exception ex) {
            Logger.getLogger(PickWaveController.class.getName()).log(
            Level.SEVERE,
            "Exception during Pick Task creation",
            ex
            );
            waveDao.deleteWaveById(waveId);
            String cause
            = ex.getMessage() != null
            ? ex.getMessage()
            : ex.getClass().getSimpleName();
            request.setAttribute(
            "error",
            "Cannot create pick wave: "
            + cause
            + ". Please ensure inventory exists in slots for this warehouse."
            );
            request
            .getRequestDispatcher("/WEB-INF/views/outbound/pick-wave-create.jsp")
            .forward(request, response);
            return;
        }

        if (!created) {
            waveDao.deleteWaveById(waveId);
            request.setAttribute(
            "error",
            "Insufficient inventory at locations to create pick wave. Please check inventory or adjust quantity."
            );
            request
            .getRequestDispatcher("/WEB-INF/views/outbound/pick-wave-create.jsp")
            .forward(request, response);
            return;
        }

        // Update wave status (tasks already created, wave is CREATED)
        waveDao.updateWaveStatus(waveId, dto.PickWaveDTO.STATUS_CREATED);

        // Update all GDN statuses to PICKING
        for (Long gdnId : gdnIds) {
            gdnDao.updateGDNStatus(gdnId, "PICKING");
        }

        request
        .getSession()
        .setAttribute(
        "message",
        "Pick wave created successfully for " + gdnIds.size() + " GDN(s)"
        );
        response.sendRedirect(
        request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
        );
    }

    private void handleAddGdn(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        Long waveId = parseLong(request.getParameter("waveId"), -1);
        Long gdnId = parseLong(request.getParameter("gdnId"), -1);

        if (waveId <= 0 || gdnId <= 0) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        PickWaveDTO wave = waveDao.getWaveById(waveId);

        if (wave == null || !"CREATED".equals(wave.getStatus())) {
            request.setAttribute(
            "error",
            "Cannot add GDN to wave that is not in CREATED status."
            );
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
            );
            return;
        }

        waveDao.addGdnToWave(waveId, gdnId);

        request
        .getSession()
        .setAttribute("message", "Added GDN to wave successfully.");
        response.sendRedirect(
        request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
        );
    }

    private void handleRemoveGdn(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        Long waveId = parseLong(request.getParameter("waveId"), -1);
        Long gdnId = parseLong(request.getParameter("gdnId"), -1);

        if (waveId <= 0 || gdnId <= 0) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        PickTaskDAO taskDao = new PickTaskDAO();

        // Check if wave has any assigned tasks
        List<PickTaskDTO> tasks = taskDao.getTasksByWaveId(waveId);
        boolean hasAssignedTasks = tasks.stream()
            .anyMatch(t -> t.getStatus() != null && !"CREATED".equals(t.getStatus()));

        if (hasAssignedTasks) {
            request.setAttribute(
            "error",
            "Cannot remove GDN from wave that has assigned or in-progress tasks."
            );
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
            );
            return;
        }

        waveDao.removeGdnFromWave(waveId, gdnId);

        // Check if this was the last GDN - if so, delete the wave
        int remainingGdns = waveDao.countWaveGdns(waveId);
        if (remainingGdns == 0) {
            waveDao.deleteWaveById(waveId);
            request
            .getSession()
            .setAttribute("message", "Wave deleted (no GDNs remaining).");
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
        } else {
            request
            .getSession()
            .setAttribute("message", "Removed GDN from wave successfully.");
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
            );
        }
    }

    /**
     * Release a wave - creates tasks and changes status to RELEASED.
     */
    private void handleRelease(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        Long waveId = parseLong(request.getParameter("id"), -1);
        if (waveId <= 0) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        
        try {
            boolean success = waveDao.releaseWave(waveId);
            if (success) {
                request.getSession().setAttribute("message", "Wave released successfully!");
            } else {
                request.getSession().setAttribute("error", "Cannot release wave: Insufficient inventory.");
            }
        } catch (Exception e) {
            Logger.getLogger(PickWaveController.class.getName()).log(
                Level.SEVERE,
                "Error releasing wave",
                e
            );
            request.getSession().setAttribute("error", "Error releasing wave: " + e.getMessage());
        }

        response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
        );
    }

    /**
     * Cancel a wave.
     */
    private void handleCancel(
    HttpServletRequest request,
    HttpServletResponse response
    ) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        Long waveId = parseLong(request.getParameter("id"), -1);
        if (waveId <= 0) {
            response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=list"
            );
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        
        try {
            waveDao.cancelWave(waveId);
            request.getSession().setAttribute("message", "Wave cancelled successfully!");
        } catch (Exception e) {
            Logger.getLogger(PickWaveController.class.getName()).log(
                Level.SEVERE,
                "Error cancelling wave",
                e
            );
            request.getSession().setAttribute("error", "Error cancelling wave: " + e.getMessage());
        }

        response.sendRedirect(
            request.getContextPath() + "/pick-wave?action=detail&id=" + waveId
        );
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
