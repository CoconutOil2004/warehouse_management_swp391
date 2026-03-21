package controller;

import dao.GoodsDeliveryNoteDAO;
import dao.PackingDAO;
import dao.UserDAO;
import dto.GDNDetailDTO;
import dto.GDNLineDTO;
import dto.PackingLineConfigDTO;
import dto.PackingSessionDTO;
import dto.PackingTaskDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.PackingLineConfig;
import model.PackingTask;
import model.User;

@WebServlet(name = "PackingController", urlPatterns = {"/packing"})
public class PackingController extends HttpServlet {

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
                case "create" -> handleCreateWizard(request, response);
                case "getGdnDetail" -> handleGetGdnDetail(request, response);
                case "myTasks" -> handleMyTasks(request, response);
                case "detail" -> handleDetail(request, response);
                default ->
                    response.sendRedirect(request.getContextPath() + "/packing?action=list");
            }
        } catch (Exception e) {
            Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        try {
            switch (action != null ? action : "") {
                case "submit" -> handleSubmitWizard(request, response);
                case "updateTask" -> handleUpdateTask(request, response);
                default ->
                    response.sendRedirect(request.getContextPath() + "/packing?action=list");
            }
        } catch (Exception e) {
            Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    // ================= GET HANDLERS =================
    private void handleList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String status = request.getParameter("status");
        PackingDAO packingDao = new PackingDAO();
        List<PackingSessionDTO> list = packingDao.listPackingSessions(status);
        request.setAttribute("sessions", list);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-list.jsp").forward(request, response);
    }

    private void handleCreateWizard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null || (!hasRole(user, "ADMIN") && !hasRole(user, "WAREHOUSE_MANAGER"))) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        int step = parseInt(request.getParameter("step"), 1);

        if (step == 1) {
            PackingDAO packingDao = new PackingDAO();
            List<PackingSessionDTO> readyGdns = packingDao.listGDNsReadyForPacking();
            request.setAttribute("readyGdns", readyGdns);
            request.setAttribute("step", 1);
            request.getRequestDispatcher("/WEB-INF/views/outbound/packing-create.jsp").forward(request, response);
        } else if (step == 2) {
            long gdnId = parseLong(request.getParameter("gdnId"), -1);
            if (gdnId <= 0) {
                response.sendRedirect(request.getContextPath() + "/packing?action=create&step=1");
                return;
            }
            GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
            GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);

            // Filter out lines with 0 picked qty
            if (gdn != null && gdn.getLines() != null) {
                gdn.getLines().removeIf(line -> line.getQtyPicked() == null || line.getQtyPicked().signum() == 0);
            }

            request.setAttribute("gdn", gdn);
            request.setAttribute("step", 2);
            request.getRequestDispatcher("/WEB-INF/views/outbound/packing-create.jsp").forward(request, response);
        } else if (step == 3) {
            long gdnId = parseLong(request.getParameter("gdnId"), -1);
            if (gdnId <= 0) {
                response.sendRedirect(request.getContextPath() + "/packing?action=create&step=1");
                return;
            }

            GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
            GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);

            // Build configs from step 2 inputs
            List<PackingLineConfig> tempConfigs = new ArrayList<>();
            if (gdn != null && gdn.getLines() != null) {
                for (GDNLineDTO line : gdn.getLines()) {
                    if (line.getQtyPicked() == null || line.getQtyPicked().signum() == 0)
                        continue;

                    int itemsPerPack = parseInt(request.getParameter("itemsPerPack_" + line.getGdnLineId()), 1);
                    int numPacks = parseInt(request.getParameter("numPacks_" + line.getGdnLineId()), 1);

                    PackingLineConfig cfg = new PackingLineConfig();
                    cfg.setGdnLineId(line.getGdnLineId());
                    cfg.setItemsPerPack(itemsPerPack);
                    cfg.setNumPacks(numPacks);
                    tempConfigs.add(cfg);

                    // Attach temp config to request for line rendering
                    request.setAttribute("config_" + line.getGdnLineId(), cfg);
                }
                gdn.getLines().removeIf(line -> line.getQtyPicked() == null || line.getQtyPicked().signum() == 0);
            }

            UserDAO userDao = new UserDAO();
            List<User> staffList = userDao.getUsersByRole("WAREHOUSE_STAFF");

            request.setAttribute("gdn", gdn);
            request.setAttribute("staffList", staffList);
            request.setAttribute("step", 3);
            request.getRequestDispatcher("/WEB-INF/views/outbound/packing-create.jsp").forward(request, response);
        }
    }

    private void handleGetGdnDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        if (gdnId > 0) {
            GoodsDeliveryNoteDAO dao = new GoodsDeliveryNoteDAO();
            GDNDetailDTO detail = dao.getGDNDetailById(gdnId);
            if (detail != null) {
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"gdnId\":").append(detail.getGdnId()).append(",");
                json.append("\"customerName\":\"").append(escapeJson(detail.getCustomerName())).append("\",");
                json.append("\"soNumber\":\"").append(escapeJson(detail.getSoNumber())).append("\",");
                json.append("\"createdAt\":{\"date\":{");
                if (detail.getCreatedAt() != null) {
                    json.append("\"year\":").append(detail.getCreatedAt().getYear()).append(",");
                    json.append("\"month\":").append(detail.getCreatedAt().getMonthValue()).append(",");
                    json.append("\"day\":").append(detail.getCreatedAt().getDayOfMonth());
                }
                json.append("}},");
                json.append("\"lines\":[");
                if (detail.getLines() != null) {
                    for (int i = 0; i < detail.getLines().size(); i++) {
                        json.append("{}");
                        if (i < detail.getLines().size() - 1) json.append(",");
                    }
                }
                json.append("]");
                json.append("}");
                out.print(json.toString());
            } else {
                out.print("{}");
            }
        } else {
            out.print("{}");
        }
        out.flush();
    }

    private void handleMyTasks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        PackingDAO dao = new PackingDAO();
        List<PackingTaskDTO> myTasks = dao.getTasksByUserId(user.getUserId());
        request.setAttribute("tasks", myTasks);
        request.getRequestDispatcher("/WEB-INF/views/outbound/my-packing-tasks.jsp").forward(request, response);
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null || (!hasRole(user, "ADMIN") && !hasRole(user, "WAREHOUSE_MANAGER"))) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        long sessionId = parseLong(request.getParameter("id"), -1);
        if (sessionId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list&error=invalid_session");
            return;
        }

        PackingDAO dao = new PackingDAO();
        PackingSessionDTO session = dao.getPackingSessionById(sessionId);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list&error=session_not_found");
            return;
        }

        List<PackingLineConfigDTO> lines = dao.getLineConfigsBySessionId(sessionId);
        List<PackingTaskDTO> tasks = dao.getTasksBySessionId(sessionId);

        request.setAttribute("sessionDTO", session);
        request.setAttribute("lines", lines);
        request.setAttribute("tasks", tasks);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-detail.jsp").forward(request, response);
    }

    // ================= POST HANDLERS =================
    private void handleSubmitWizard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null || (!hasRole(user, "ADMIN") && !hasRole(user, "WAREHOUSE_MANAGER"))) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list&error=invalid_gdn");
            return;
        }

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
        if (gdn == null) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list&error=not_found");
            return;
        }

        PackingDAO packingDao = new PackingDAO();

        // Ensure no session exists yet
        if (packingDao.getPackingSessionByGdnId(gdnId) != null) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list&error=already_exists");
            return;
        }

        // 1. Create Session
        Long sessionId = packingDao.createPackingSession(gdnId, user.getUserId());

        // 2. Build configs and tasks
        List<PackingLineConfig> configsToSave = new ArrayList<>();
        Map<Long, List<PackingTask>> lineTasksMap = new HashMap<>(); // gdnLineId -> Tasks

        String[] lineIdsStr = request.getParameterValues("gdnLineIds");
        if (lineIdsStr != null) {
            for (String lidStr : lineIdsStr) {
                long gdnLineId = parseLong(lidStr, -1);
                if (gdnLineId <= 0) continue;

                int itemsPerPack = parseInt(request.getParameter("itemsPerPack_" + gdnLineId), 1);
                int numPacks = parseInt(request.getParameter("numPacks_" + gdnLineId), 1);

                PackingLineConfig cfg = new PackingLineConfig();
                cfg.setPackingSessionId(sessionId);
                cfg.setGdnLineId(gdnLineId);
                cfg.setItemsPerPack(itemsPerPack);
                cfg.setNumPacks(numPacks);
                configsToSave.add(cfg);

                // Tasks for this line
                List<PackingTask> lineTasks = new ArrayList<>();
                String[] assignedUsers = request.getParameterValues("taskUser_" + gdnLineId);
                String[] assignedPacks = request.getParameterValues("taskPacks_" + gdnLineId);

                if (assignedUsers != null && assignedPacks != null && assignedUsers.length == assignedPacks.length) {
                    for (int i = 0; i < assignedUsers.length; i++) {
                        long userId = parseLong(assignedUsers[i], -1);
                        int packsCount = parseInt(assignedPacks[i], 0);
                        if (userId > 0 && packsCount > 0) {
                            PackingTask task = new PackingTask();
                            task.setAssignedTo(userId);
                            task.setAssignedPacks(packsCount);
                            lineTasks.add(task);
                        }
                    }
                }
                lineTasksMap.put(gdnLineId, lineTasks);
            }
        }

        // Save Configs
        packingDao.saveLineConfigs(sessionId, configsToSave);

        // Map generated config IDs to tasks and save them
        List<PackingTask> allTasksToSave = new ArrayList<>();
        for (PackingLineConfig cfg : configsToSave) {
            List<PackingTask> tasks = lineTasksMap.get(cfg.getGdnLineId());
            if (tasks != null) {
                for (PackingTask t : tasks) {
                    t.setPackingLineConfigId(cfg.getPackingLineConfigId());
                    allTasksToSave.add(t);
                }
            }
        }

        packingDao.savePackingTasks(allTasksToSave);

        response.sendRedirect(request.getContextPath() + "/packing?action=list&message=Packing configured successfully");
    }

    private void handleUpdateTask(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        long taskId = parseLong(request.getParameter("taskId"), -1);
        int packedPacks = parseInt(request.getParameter("packedPacks"), 0);

        if (taskId > 0) {
            PackingDAO packingDao = new PackingDAO();

            // Get session info before update (to check status)
            String fetchSessionSql = """
                                     SELECT ps.packing_session_id, plc.gdn_line_id, plc.items_per_pack
                                     FROM packing_task pt
                                     JOIN packing_line_config plc ON pt.packing_line_config_id = plc.packing_line_config_id
                                     JOIN packing_session ps ON plc.packing_session_id = ps.packing_session_id
                                     WHERE pt.packing_task_id = ?""";

            Long sessionId = null;
            Long gdnLineId = null;
            Integer itemsPerPack = null;

            try (java.sql.Connection conn = PackingDAO.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(fetchSessionSql)) {
                ps.setLong(1, taskId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getLong("packing_session_id");
                        gdnLineId = rs.getLong("gdn_line_id");
                        itemsPerPack = rs.getInt("items_per_pack");
                    }
                }
            }

            if (sessionId != null) {
                // Update task
                packingDao.updateTaskProgress(taskId, packedPacks);
                packingDao.markSessionInProgress(sessionId);

                // Recalculate total packed qty for this GDN line
                String sumSql = """
                                SELECT SUM(packed_packs) FROM packing_task pt
                                JOIN packing_line_config plc ON pt.packing_line_config_id = plc.packing_line_config_id
                                WHERE plc.gdn_line_id = ?""";
                int totalPackedPacksForLine = 0;
                try (java.sql.Connection conn = PackingDAO.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sumSql)) {
                    ps.setLong(1, gdnLineId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) totalPackedPacksForLine = rs.getInt(1);
                    }
                }

                // Re-calculate qty_packed for the line
                GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();

                // Fetch current line info to cap at qty_picked
                String lineSql = "SELECT qty_picked FROM goods_delivery_line WHERE gdn_line_id = ?";
                BigDecimal qtyPicked = BigDecimal.ZERO;
                try (java.sql.Connection conn = PackingDAO.getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(lineSql)) {
                    ps.setLong(1, gdnLineId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) qtyPicked = rs.getBigDecimal(1);
                    }
                }

                BigDecimal calculatedQtyPacked = new BigDecimal(totalPackedPacksForLine * itemsPerPack);
                // Cap the qty_packed if it exceeds qty_picked (for the last partial pack)
                if (calculatedQtyPacked.compareTo(qtyPicked) > 0) {
                    calculatedQtyPacked = qtyPicked;
                }

                gdnDao.updateGDNLineQuantities(gdnLineId, qtyPicked, calculatedQtyPacked);

                // Check completion
                if (packingDao.isAllTasksDoneForSession(sessionId)) {
                    packingDao.completeSession(sessionId);

                    // Need to find gdnId to update GDN status
                    PackingSessionDTO sessionDTO = packingDao.getPackingSessionById(sessionId);
                    if (sessionDTO != null) {
                        gdnDao.updateGDNStatus(sessionDTO.getGdnId(), "SHIPPING");
                        gdnDao.deductInventoryOnConfirm(sessionDTO.getGdnId());
                    }
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/packing?action=myTasks&message=Task updated");
    }

    // ================= HELPERS =================
    private boolean hasRole(User user, String role) {
        if (user == null || user.getRoleNames() == null) return false;
        return user.getRoleNames().contains(role);
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private int parseInt(String raw, int def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\t", "\\t")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
    }
}
