package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.GoodsDeliveryNoteDAO;
import dao.PickTaskDAO;
import dao.PickWaveDAO;
import dao.UserDAO;
import dto.PickTaskDTO;
import dto.PickTaskLineDTO;
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
                case "detail" -> handleDetail(request, response);
                case "assign" -> handleAssignForm(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            }
        } catch (Exception e) {
            Logger.getLogger(PickTaskController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    private void handleMyTasks(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        List<PickTaskDTO> tasks = pickTaskDao.getMyPickTasks(user.getUserId(), status, pageSize, offset);
        int totalTasks = pickTaskDao.countMyPickTasks(user.getUserId(), status);
        int totalPages = (int) Math.ceil((double) totalTasks / pageSize);

        request.setAttribute("tasks", tasks);
        request.setAttribute("status", status);
        request.setAttribute("page", (long) page);
        request.setAttribute("pages", (long) totalPages);
        request.setAttribute("size", (long) pageSize);
        request.setAttribute("total", (long) totalTasks);
        request.getRequestDispatcher("/WEB-INF/views/outbound/pick-task-list.jsp").forward(request, response);
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }

        Long pickTaskId = parseLong(request.getParameter("id"), -1);
        if (pickTaskId <= 0) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        PickTaskDTO task = pickTaskDao.getPickTaskById(pickTaskId);
        if (task == null) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }
        if (!user.getUserId().equals(task.getAssignedTo())) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        request.setAttribute("task", task);
        request.getRequestDispatcher("/WEB-INF/views/outbound/pick-task-detail.jsp").forward(request, response);
    }

    private void handleAssignForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = (User) request.getSession().getAttribute("USER");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=login");
            return;
        }
        String roles = user.getRoleNames() != null ? user.getRoleNames() : "";
        if (!roles.contains("ADMIN") && !roles.contains("WAREHOUSE_MANAGER")) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        Long waveId = parseLong(request.getParameter("waveId"), -1);
        if (waveId <= 0) {
            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        PickWaveDAO waveDao = new PickWaveDAO();
        PickTaskDAO pickTaskDao = new PickTaskDAO();
        UserDAO userDao = new UserDAO();

        dto.PickWaveDTO wave = waveDao.getWaveById(waveId);
        if (wave == null) {
            response.sendRedirect(request.getContextPath() + "/goods-delivery-note?action=list");
            return;
        }

        List<PickTaskDTO> tasks = pickTaskDao.getTasksByWaveId(waveId);
        List<model.User> warehouseStaff = userDao.getUsersByRole("WAREHOUSE_STAFF");

        request.setAttribute("wave", wave);
        request.setAttribute("tasks", tasks);
        request.setAttribute("warehouseStaff", warehouseStaff);
        request.getRequestDispatcher("/WEB-INF/views/outbound/pick-task-assign.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "assign" -> handleAssign(request, response);
                case "start" -> handleStart(request, response);
                case "complete" -> handleComplete(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            }
        } catch (Exception e) {
            Logger.getLogger(PickTaskController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    private void handleAssign(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long pickTaskId = parseLong(request.getParameter("pickTaskId"), -1);
        Long assignedTo = parseLong(request.getParameter("assignedTo"), -1);
        Long waveId = parseLong(request.getParameter("waveId"), -1);

        if (pickTaskId <= 0 || assignedTo <= 0) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long assignedBy = user != null ? user.getUserId() : null;

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.assignTask(pickTaskId, assignedTo, assignedBy);

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        PickTaskDTO task = pickTaskDao.getPickTaskById(pickTaskId);
        if (task != null) {
            gdnDao.updateGDNStatus(task.getGdnId(), "ONGOING");
        }

        if (waveId > 0) {
            request.getSession().setAttribute("message", "Đã phân công nhân viên xử lý task #" + pickTaskId);
            response.sendRedirect(request.getContextPath() + "/pick-task?action=assign&waveId=" + waveId);
        } else {
            request.getSession().setAttribute("message", "Phân công task thành công");
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
        }
    }

    private void handleStart(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long pickTaskId = parseLong(request.getParameter("id"), -1);
        if (pickTaskId <= 0) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
            return;
        }
        PickTaskDAO pickTaskDao = new PickTaskDAO();
        pickTaskDao.startTask(pickTaskId);
        request.getSession().setAttribute("message", "Đã bắt đầu thực hiện task #" + pickTaskId);
        response.sendRedirect(request.getContextPath() + "/pick-task?action=detail&id=" + pickTaskId);
    }

    private void handleComplete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long pickTaskId = parseLong(request.getParameter("pickTaskId"), -1);
        if (pickTaskId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        PickTaskDAO pickTaskDao = new PickTaskDAO();
        PickTaskDTO task = pickTaskDao.getPickTaskById(pickTaskId);
        if (task == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] lineIds = request.getParameterValues("lineIds");
        String[] qtyPickedStrs = request.getParameterValues("qtyPicked");
        if (lineIds == null || qtyPickedStrs == null || lineIds.length != qtyPickedStrs.length) {
            response.sendRedirect(request.getContextPath() + "/pick-task?action=detail&id=" + pickTaskId);
            return;
        }

        List<PickTaskLineDTO> lines = task.getLines();
        for (int i = 0; i < lineIds.length; i++) {
            long lid = Long.parseLong(lineIds[i]);
            BigDecimal qty = BigDecimal.ZERO;
            try {
                qty = new BigDecimal(qtyPickedStrs[i].trim());
            } catch (NumberFormatException ignored) {}
            for (PickTaskLineDTO line : lines) {
                if (line.getPickTaskLineId() == lid) {
                    line.setQtyPicked(qty);
                    line.setPickStatus("DONE");
                    break;
                }
            }
        }

        pickTaskDao.completeTask(pickTaskId, lines);

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        gdnDao.updateGDNStatus(task.getGdnId(), "CONFIRMED");

        request.getSession().setAttribute("message", "Hoàn thành task #" + pickTaskId + " thành công!");
        response.sendRedirect(request.getContextPath() + "/pick-task?action=myTasks");
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
