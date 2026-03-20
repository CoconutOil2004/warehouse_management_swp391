package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.GoodsDeliveryNoteDAO;
import dao.PickTaskDAO;
import dao.PickWaveDAO;
import dao.UserDAO;
import dto.PickTaskDTO;
import dto.PickTaskLineDTO;
import dto.PickWaveDTO;
import dto.TaskAssignmentSuggestionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

@WebServlet(name = "PickTaskController", urlPatterns = { "/pick-task" })
public class PickTaskController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "myTasks";
        }

        try {
            switch (action) {
                case "myTasks" -> handleMyTasks(request, response);
                case "myLines" -> handleMyLines(request, response);
                case "detail" -> handleDetail(request, response);
                case "assign" -> handleAssignForm(request, response);
                case "assign-lines" -> handleAssignLinesForm(request, response);
                case "pick" -> handlePickLine(request, response);
                default -> response.sendRedirect(
                        request.getContextPath() + "/pick-task?action=myTasks");
            }
        } catch (Exception e) {
            Logger.getLogger(PickTaskController.class.getName()).log(
                    Level.SEVERE,
                    null,
                    e);
            throw new ServletException(e);
        }
    }

    private void handleMyTasks(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        int page = (int) parseLong(request.getParameter("page"), 1);
        int pageSize = (int) parseLong(request.getParameter("size"), 10);
        int offset = (page - 1) * pageSize;

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        String status = request.getParameter("status");
        List<PickTaskDTO> tasks = pickTaskDao.getMyPickTasks(
                user.getUserId(),
                status,
                pageSize,
                offset);
        int totalTasks = pickTaskDao.countMyPickTasks(user.getUserId(), status);
        int totalPages = (int) Math.ceil((double) totalTasks / pageSize);

        request.setAttribute("tasks", tasks);
        request.setAttribute("status", status);
        request.setAttribute("page", (long) page);
        request.setAttribute("pages", (long) totalPages);
        request.setAttribute("size", (long) pageSize);
        request.setAttribute("total", (long) totalTasks);
        request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-task-list.jsp")
                .forward(request, response);
    }

    private void handleDetail(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        Long pickTaskId = parseLong(request.getParameter("id"), -1);
        if (pickTaskId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        PickTaskDTO task = pickTaskDao.getPickTaskById(pickTaskId);
        if (task == null) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        // Check if user has any lines assigned in this task
        List<PickTaskLineDTO> taskLines = pickTaskDao.getPickTaskLines(pickTaskId);
        boolean hasAssignedLine = taskLines.stream()
                .anyMatch(l -> l.getAssignedTo() != null && l.getAssignedTo().equals(user.getUserId()));

        if (!hasAssignedLine) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        request.setAttribute("task", task);
        request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-task-detail.jsp")
                .forward(request, response);
    }

    private void handleAssignForm(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        Long waveId = parseLong(request.getParameter("waveId"), -1);
        if (waveId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        PickTaskDAO pickTaskDao = new PickTaskDAO();
        UserDAO userDao = new UserDAO();

        dto.PickWaveDTO wave = waveDao.getWaveById(waveId);
        if (wave == null) {
            response.sendRedirect(
                    request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        List<PickTaskDTO> tasks = pickTaskDao.getTasksByWaveId(waveId);
        List<model.User> warehouseStaff = userDao.getUsersByRole("WAREHOUSE_STAFF");

        // Get workload for all staff
        List<dto.UserWorkloadDTO> staffWorkload = pickTaskDao.getStaffWorkload();

        // Get auto-assign suggestions
        List<TaskAssignmentSuggestionDTO> suggestions = pickTaskDao.getSuggestedAssignments(waveId);

        // Get unassigned lines
        List<PickTaskLineDTO> unassignedLines = pickTaskDao.getUnassignedLinesByWave(waveId);

        request.setAttribute("wave", wave);
        request.setAttribute("tasks", tasks);
        request.setAttribute("warehouseStaff", warehouseStaff);
        request.setAttribute("staffWorkload", staffWorkload);
        request.setAttribute("suggestions", suggestions);
        request.setAttribute("unassignedLines", unassignedLines);
        request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-task-assign.jsp")
                .forward(request, response);
    }

    /**
     * Show lines assigned to current user (for PDA/Mobile).
     */
    private void handleMyLines(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        List<PickTaskLineDTO> lines = pickTaskDao.getMyAssignedLines(user.getUserId());

        request.setAttribute("lines", lines);
        request.setAttribute("user", user);
        request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-my-lines.jsp")
                .forward(request, response);
    }

    /**
     * Show form for line assignment (new line-level assignment).
     */
    private void handleAssignLinesForm(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        Long waveId = parseLong(request.getParameter("waveId"), -1);
        if (waveId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        PickTaskDAO pickTaskDao = new PickTaskDAO();
        UserDAO userDao = new UserDAO();

        dto.PickWaveDTO wave = waveDao.getWaveById(waveId);
        List<PickTaskLineDTO> unassignedLines = pickTaskDao.getUnassignedLinesByWave(waveId);
        List<model.User> warehouseStaff = userDao.getUsersByRole("WAREHOUSE_STAFF");
        List<dto.UserWorkloadDTO> staffWorkload = pickTaskDao.getStaffWorkload();

        request.setAttribute("wave", wave);
        request.setAttribute("unassignedLines", unassignedLines);
        request.setAttribute("warehouseStaff", warehouseStaff);
        request.setAttribute("staffWorkload", staffWorkload);
        request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-line-assign.jsp")
                .forward(request, response);
    }

    /**
     * Show pick execution page for a single line.
     */
    private void handlePickLine(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        Long lineId = parseLong(request.getParameter("id"), -1);
        if (lineId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myLines");
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        List<PickTaskLineDTO> myLines = pickTaskDao.getMyAssignedLines(user.getUserId());

        PickTaskLineDTO targetLine = null;
        for (PickTaskLineDTO line : myLines) {
            if (line.getPickTaskLineId().equals(lineId)) {
                targetLine = line;
                break;
            }
        }

        if (targetLine == null) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myLines");
            return;
        }

        if (targetLine.getWaveId() != null) {
            PickWaveDAO waveDao = new PickWaveDAO();
            PickWaveDTO wave = waveDao.getWaveById(targetLine.getWaveId());
            if (wave == null || !"RELEASED".equals(wave.getStatus())) {
                request.getSession().setAttribute("error", "Wave is not released yet. Please wait for release.");
                response.sendRedirect(
                        request.getContextPath() + "/pick-task?action=myLines");
                return;
            }
        }

        request.setAttribute("line", targetLine);
        request.setAttribute("myLines", myLines);
        request
                .getRequestDispatcher("/WEB-INF/views/outbound/pick-task-execute.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null)
            action = "";

        try {
            switch (action) {
                case "assign" -> handleAssign(request, response);
                case "assign-lines" -> handleAssignLines(request, response);
                case "assign-lines-batch" ->
                    handleAssignLinesBatch(request, response);
                case "assign-all-batch" ->
                    handleAssignAllBatch(request, response);
                case "batch-assign" -> handleBatchAssign(request, response);
                case "auto-assign" -> handleAutoAssign(request, response);
                case "auto-assign-lines" ->
                    handleAutoAssignLines(request, response);
                case "start" -> handleStart(request, response);
                case "start-line" -> handleStartLine(request, response);
                case "complete" -> handleComplete(request, response);
                case "save-pick" -> handleSavePick(request, response);
                default -> response.sendRedirect(
                        request.getContextPath() + "/pick-task?action=myTasks");
            }
        } catch (Exception e) {
            Logger.getLogger(PickTaskController.class.getName()).log(
                    Level.SEVERE,
                    null,
                    e);
            throw new ServletException(e);
        }
    }

    private void handleAssign(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long pickTaskId = parseLong(request.getParameter("pickTaskId"), -1);
        Long assignedTo = parseLong(request.getParameter("assignedTo"), -1);
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (pickTaskId <= 0 || assignedTo <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.assignTask(pickTaskId, assignedTo, assignedBy);

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        PickTaskDTO task = pickTaskDao.getPickTaskById(pickTaskId);
        if (task != null) {
            gdnDao.updateGDNStatus(task.getGdnId(), "PICKING");
        }

        if (waveId > 0) {
            request
                    .getSession()
                    .setAttribute(
                            "message",
                            "Đã phân công nhân viên xử lý task #" + pickTaskId);
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
        } else {
            request.getSession().setAttribute("message", "Phân công task thành công");
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
        }
    }

    private void handleAssignAllBatch(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] taskIdsParam = request.getParameterValues("taskIds");
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (taskIdsParam == null || taskIdsParam.length == 0 || waveId <= 0) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/pick-task?action=assign&waveId="
                            + waveId
                            + "&error=no_tasks");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();

        // Collect assignments: taskId -> assignedTo
        List<Long> assignedTaskIds = new ArrayList<>();
        List<Long> assignedToIds = new ArrayList<>();

        for (String taskIdStr : taskIdsParam) {
            Long taskId = Long.parseLong(taskIdStr);
            String assignedToParam = request.getParameter("assignedTo_" + taskId);

            if (assignedToParam != null && !assignedToParam.isEmpty()) {
                Long assignedTo = Long.parseLong(assignedToParam);
                if (assignedTo > 0) {
                    assignedTaskIds.add(taskId);
                    assignedToIds.add(assignedTo);
                }
            }
        }

        if (assignedTaskIds.isEmpty()) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/pick-task?action=assign&waveId="
                            + waveId
                            + "&error=no_selection");
            return;
        }

        // Assign tasks in batch
        for (int i = 0; i < assignedTaskIds.size(); i++) {
            Long taskId = assignedTaskIds.get(i);
            Long assignedTo = assignedToIds.get(i);

            List<Long> singleTask = new ArrayList<>();
            singleTask.add(taskId);
            pickTaskDao.batchAssignTasks(singleTask, assignedTo, assignedBy);
        }

        // Update GDN statuses
        List<PickTaskDTO> tasks = pickTaskDao.getTasksByWaveId(waveId);
        for (PickTaskDTO task : tasks) {
            if (task != null && task.getGdnId() != null) {
                GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
                gdnDao.updateGDNStatus(task.getGdnId(), "PICKING");
            }
        }

        request
                .getSession()
                .setAttribute(
                        "message",
                        "Đã phân công " + assignedTaskIds.size() + " tasks thành công");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
    }

    private void handleBatchAssign(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] pickTaskIdsParam = request.getParameterValues("pickTaskIds");
        Long assignedTo = parseLong(request.getParameter("assignedTo"), -1);
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (pickTaskIdsParam == null
                || pickTaskIdsParam.length == 0
                || assignedTo <= 0) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/pick-task?action=assign&waveId="
                            + waveId
                            + "&error=invalid_params");
            return;
        }

        List<Long> pickTaskIds = new java.util.ArrayList<>();
        for (String id : pickTaskIdsParam) {
            try {
                pickTaskIds.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid IDs
            }
        }

        if (pickTaskIds.isEmpty()) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/pick-task?action=assign&waveId="
                            + waveId
                            + "&error=no_tasks");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.batchAssignTasks(pickTaskIds, assignedTo, assignedBy);

        // Update GDN statuses
        for (Long taskId : pickTaskIds) {
            PickTaskDTO task = pickTaskDao.getPickTaskById(taskId);
            if (task != null && task.getGdnId() != null) {
                GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
                gdnDao.updateGDNStatus(task.getGdnId(), "PICKING");
            }
        }

        request
                .getSession()
                .setAttribute(
                        "message",
                        "Đã phân công " + pickTaskIds.size() + " tasks thành công");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
    }

    private void handleAutoAssign(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (waveId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.autoAssignTasks(waveId, assignedBy);

        // Update GDN statuses
        List<PickTaskDTO> tasks = pickTaskDao.getTasksByWaveId(waveId);
        for (PickTaskDTO task : tasks) {
            if (task != null && task.getGdnId() != null) {
                GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
                gdnDao.updateGDNStatus(task.getGdnId(), "ONGOING");
            }
        }

        request
                .getSession()
                .setAttribute(
                        "message",
                        "Auto-assign hoàn thành! Tasks đã được chia đều theo workload.");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
    }

    private void handleStart(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long pickTaskId = parseLong(request.getParameter("id"), -1);
        if (pickTaskId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }
        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.startTask(pickTaskId);
        request
                .getSession()
                .setAttribute("message", "Đã bắt đầu thực hiện task #" + pickTaskId);
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=detail&id=" + pickTaskId);
    }

    private void handleComplete(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long pickTaskId = parseLong(request.getParameter("pickTaskId"), -1);
        if (pickTaskId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        Long pickedBy = user.getUserId();

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        PickTaskDTO task = pickTaskDao.getPickTaskById(pickTaskId);
        if (task == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] lineIds = request.getParameterValues("lineIds");
        String[] qtyPickedStrs = request.getParameterValues("qtyPicked");
        if (lineIds == null
                || qtyPickedStrs == null
                || lineIds.length != qtyPickedStrs.length) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=detail&id=" + pickTaskId);
            return;
        }

        List<PickTaskLineDTO> lines = task.getLines();
        for (int i = 0; i < lineIds.length; i++) {
            long lid = Long.parseLong(lineIds[i]);
            BigDecimal qty = BigDecimal.ZERO;
            try {
                qty = new BigDecimal(qtyPickedStrs[i].trim());
            } catch (NumberFormatException ignored) {
            }
            for (PickTaskLineDTO line : lines) {
                if (line.getPickTaskLineId() == lid) {
                    BigDecimal qtyToPick = line.getQtyToPick() != null ? line.getQtyToPick() : BigDecimal.ZERO;
                    if (qty.compareTo(qtyToPick) > 0) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        request.getSession().setAttribute("error",
                                "Số lượng pick không được lớn hơn số lượng cần pick!");
                        response.sendRedirect(
                                request.getContextPath() + "/pick-task?action=detail&id=" + pickTaskId);
                        return;
                    }
                    line.setQtyPicked(qty);
                    break;
                }
            }
        }

        pickTaskDao.completeTask(pickTaskId, lines, pickedBy);

        if (task.getWaveId() != null) {
            if (pickTaskDao.isWaveComplete(task.getWaveId())) {
                PickWaveDAO waveDao = new PickWaveDAO();
                waveDao.updateWaveStatus(task.getWaveId(), "DONE");

                GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
                gdnDao.updateGDNStatus(task.getGdnId(), "PACKING");
                // Ensure packing record exists so it can be assigned
                dao.PackingDAO packingDao = new dao.PackingDAO();
                if (packingDao.getByGdnId(task.getGdnId()) == null) {
                    packingDao.createPackingForGDN(task.getGdnId());
                }
            }
        } else {
            GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
            if (pickTaskDao.isAllTasksCompleteForGDN(task.getGdnId())) {
                gdnDao.updateGDNStatus(task.getGdnId(), "PACKING");
                // Ensure packing record exists so it can be assigned
                dao.PackingDAO packingDao = new dao.PackingDAO();
                if (packingDao.getByGdnId(task.getGdnId()) == null) {
                    packingDao.createPackingForGDN(task.getGdnId());
                }
            }
        }

        request
                .getSession()
                .setAttribute(
                        "message",
                        "Hoàn thành task #" + pickTaskId + " thành công! GDN đã chuyển sang trạng thái PACKING.");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=myTasks");
    }

    /**
     * Assign multiple lines to a staff member (line-level assignment).
     */
    private void handleAssignLines(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] lineIdsParam = request.getParameterValues("lineIds");
        Long assignedTo = parseLong(request.getParameter("assignedTo"), -1);
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (lineIdsParam == null || lineIdsParam.length == 0 || assignedTo <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId + "&error=invalid");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        List<Long> lineIds = new ArrayList<>();
        for (String id : lineIdsParam) {
            try {
                lineIds.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        if (lineIds.isEmpty()) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId + "&error=no_selection");
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.assignLines(lineIds, assignedTo, assignedBy);

        request.getSession().setAttribute("message", "Đã gán " + lineIds.size() + " dòng cho nhân viên");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
    }

    /**
     * Batch assign multiple lines to different staff members (line-level
     * assignment).
     */
    private void handleAssignLinesBatch(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] lineIdsParam = request.getParameterValues("lineIds");
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (lineIdsParam == null || lineIdsParam.length == 0 || waveId <= 0) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/pick-task?action=assign&waveId="
                            + waveId
                            + "&error=no_lines");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();

        // Collect assignments: lineId -> assignedTo
        Map<Long, Long> lineAssignments = new LinkedHashMap<>();

        for (String lineIdStr : lineIdsParam) {
            Long lineId = Long.parseLong(lineIdStr);
            String assignedToParam = request.getParameter("assignedTo_" + lineId);

            if (assignedToParam != null && !assignedToParam.isEmpty()) {
                Long assignedTo = Long.parseLong(assignedToParam);
                if (assignedTo > 0) {
                    lineAssignments.put(lineId, assignedTo);
                }
            }
        }

        if (lineAssignments.isEmpty()) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/pick-task?action=assign&waveId="
                            + waveId
                            + "&error=no_selection");
            return;
        }

        // Group lines by assignedTo for batch assignment
        Map<Long, List<Long>> linesByStaff = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : lineAssignments.entrySet()) {
            linesByStaff.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // Assign lines in batch for each staff
        for (Map.Entry<Long, List<Long>> entry : linesByStaff.entrySet()) {
            Long assignedTo = entry.getKey();
            List<Long> lineIds = entry.getValue();
            pickTaskDao.assignLines(lineIds, assignedTo, assignedBy);
        }

        // Update GDN statuses
        List<PickTaskDTO> tasks = pickTaskDao.getTasksByWaveId(waveId);
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        for (PickTaskDTO task : tasks) {
            if (task != null && task.getGdnId() != null) {
                gdnDao.updateGDNStatus(task.getGdnId(), "PICKING");
            }
        }

        request
                .getSession()
                .setAttribute(
                        "message",
                        "Đã phân công " + lineAssignments.size() + " dòng thành công");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
    }

    /**
     * Auto-assign lines using load balancing.
     */
    private void handleAutoAssignLines(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (waveId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.autoAssignLines(waveId, assignedBy);

        request.getSession().setAttribute("message", "Auto-assign hoàn thành!");
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
    }

    /**
     * Start picking a single line.
     */
    private void handleStartLine(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long lineId = parseLong(request.getParameter("lineId"), -1);

        if (lineId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myLines");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.startLinePicking(lineId, user.getUserId());

        request.getSession().setAttribute("message", "Đã bắt đầu nhặt dòng #" + lineId);
        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=pick&id=" + lineId);
    }

    /**
     * Save pick result for a line.
     */
    private void handleSavePick(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long lineId = parseLong(request.getParameter("lineId"), -1);
        String qtyPickedStr = request.getParameter("qtyPicked");

        if (lineId <= 0) {
            response.sendRedirect(
                    request.getContextPath() + "/pick-task?action=myLines");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        BigDecimal qtyPicked = BigDecimal.ZERO;
        try {
            if (qtyPickedStr != null && !qtyPickedStr.isBlank()) {
                qtyPicked = new BigDecimal(qtyPickedStr.trim());
            }
        } catch (NumberFormatException e) {
            // Use 0
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.completeLinePicking(lineId, qtyPicked, user.getUserId());

        Long gdnId = pickTaskDao.getGdnIdByLineId(lineId);
        if (gdnId != null && pickTaskDao.isAllLinesPickedForGDN(gdnId)) {
            GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
            gdnDao.updateGDNStatus(gdnId, "PACKING");
            request.getSession().setAttribute("message",
                    "Đã lưu kết quả nhặt hàng! GDN đã chuyển sang trạng thái PACKING.");
        } else {
            request.getSession().setAttribute("message", "Đã lưu kết quả nhặt hàng!");
        }

        response.sendRedirect(
                request.getContextPath() + "/pick-task?action=myLines");
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
