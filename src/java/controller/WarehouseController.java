package controller;

import dao.WarehouseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Warehouse;

@WebServlet(
        name = "WarehouseController",
        urlPatterns = {
            "/admin/warehouse",
            "/admin/warehouse/create",
            "/admin/warehouse/update",
            "/admin/warehouse/detail",
            "/admin/warehouse/delete"
        }
)
public class WarehouseController extends HttpServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();

        try {
            switch (servletPath) {
                case "/admin/warehouse" ->
                    handleList(request, response);
                case "/admin/warehouse/create" ->
                    handleShowCreate(request, response);
                case "/admin/warehouse/update" ->
                    handleShowUpdate(request, response);
                case "/admin/warehouse/detail" ->
                    handleShowDetail(request, response);
                default ->
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception ex) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();

        try {
            if ("/admin/warehouse/create".equals(servletPath)) {
                handleCreate(request, response);
            } else if ("/admin/warehouse/update".equals(servletPath)) {
                handleUpdatePost(request, response);
            } else if ("/admin/warehouse/delete".equals(servletPath)) {
                handleDeletePost(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception ex) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "PUT request received");
            handleUpdate(request, response);
        } catch (Exception ex) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.SEVERE, "Error in doPut", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            handleDelete(request, response);
        } catch (Exception ex) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        WarehouseDAO dao = new WarehouseDAO();

        String searchRaw = request.getParameter("search");
        String sort = request.getParameter("sort");
        int page = parseInt(request.getParameter("page"), DEFAULT_PAGE);
        int size = parseInt(request.getParameter("size"), DEFAULT_SIZE);

        if (sort == null || sort.isBlank()) {
            sort = "name";
        }
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        }

        String searchForQuery;
        if (searchRaw == null || searchRaw.isBlank()) {
            searchForQuery = "%%";
        } else {
            searchForQuery = "%" + searchRaw.trim() + "%";
        }

        long total = dao.getPageCount(searchForQuery);
        long pages = total == 0 ? 1 : (long) Math.ceil((double) total / size);
        if (page > pages) {
            page = (int) pages;
        }

        List<Warehouse> warehouses = dao.getList(searchForQuery, sort, (long) page, (long) size);

        request.setAttribute("warehouses", warehouses);
        request.setAttribute("page", page);
        request.setAttribute("pages", pages);
        request.setAttribute("size", size);
        request.setAttribute("total", total);
        request.setAttribute("search", searchRaw);
        request.setAttribute("sort", sort);

        request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/list.jsp").forward(request, response);
    }

    private void handleShowCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Warehouse warehouse = new Warehouse();
        request.setAttribute("warehouse", warehouse);
        request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/create.jsp").forward(request, response);
    }

    private void handleShowUpdate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Long id = parseLong(request.getParameter("id"), -1L);
        if (id == null || id <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
            return;
        }

        WarehouseDAO dao = new WarehouseDAO();
        Warehouse warehouse = dao.getDetail(id);
        if (warehouse == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Warehouse not found");
            return;
        }

        request.setAttribute("warehouse", warehouse);
        request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/update.jsp").forward(request, response);
    }

    private void handleShowDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Long id = parseLong(request.getParameter("id"), -1L);
        if (id == null || id <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
            return;
        }

        WarehouseDAO dao = new WarehouseDAO();
        Warehouse warehouse = dao.getDetail(id);
        if (warehouse == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Warehouse not found");
            return;
        }

        request.setAttribute("warehouse", warehouse);
        request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/detail.jsp").forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String code = trim(request.getParameter("code"));
        String name = trim(request.getParameter("name"));
        String address = trim(request.getParameter("address"));

        Warehouse warehouse = new Warehouse();
        warehouse.setCode(code);
        warehouse.setName(name);
        warehouse.setAddress(address);

        String error = validateWarehouse(warehouse, true);

        WarehouseDAO dao = new WarehouseDAO();
        if (error == null || dao.codeExists(code, null)) {
            error = "Warehouse code already exists.";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("warehouse", warehouse);
            request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/create.jsp").forward(request, response);
            return;
        }

        boolean created = dao.create(warehouse);
        if (!created) {
            request.setAttribute("error", "Failed to create warehouse. Please try again.");
            request.setAttribute("warehouse", warehouse);
            request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/create.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/warehouse");
    }

    private void handleUpdatePost(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Long id = parseLong(request.getParameter("id"), -1L);
        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "Update warehouse ID: " + id);

        if (id == null || id <= 0) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.WARNING, "Invalid warehouse id: " + id);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
            return;
        }

        String code = trim(request.getParameter("code"));
        String name = trim(request.getParameter("name"));
        String address = trim(request.getParameter("address"));
        String status = trim(request.getParameter("status"));

        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO,
                String.format("Update params - code: %s, name: %s, address: %s, status: %s", code, name, address, status));

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseId(id);
        warehouse.setCode(code);
        warehouse.setName(name);
        warehouse.setAddress(address);
        warehouse.setStatus(status == null || status.isBlank() ? "ACTIVE" : status);

        String error = validateWarehouse(warehouse, false);

        WarehouseDAO dao = new WarehouseDAO();
        if (error == null && dao.codeExists(code, id)) {
            error = "Warehouse code already exists.";
        }

        if (error != null) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.WARNING, "Validation error: " + error);
            request.setAttribute("error", error);
            request.setAttribute("warehouse", warehouse);
            request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/update.jsp").forward(request, response);
            return;
        }

        boolean updated = dao.update(warehouse);
        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "Update result: " + updated);

        if (!updated) {
            request.setAttribute("error", "Failed to update warehouse. Please try again.");
            request.setAttribute("warehouse", warehouse);
            request.getRequestDispatcher("/WEB-INF/views/admin/warehouse/update.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/warehouse");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Long id = parseLong(request.getParameter("id"), -1L);
        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "Update warehouse ID: " + id);

        if (id == null || id <= 0) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.WARNING, "Invalid warehouse id: " + id);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
            return;
        }

        String code = trim(request.getParameter("code"));
        String name = trim(request.getParameter("name"));
        String address = trim(request.getParameter("address"));
        String status = trim(request.getParameter("status"));

        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO,
                String.format("Update params - code: %s, name: %s, address: %s, status: %s", code, name, address, status));

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseId(id);
        warehouse.setCode(code);
        warehouse.setName(name);
        warehouse.setAddress(address);
        warehouse.setStatus(status == null || status.isBlank() ? "ACTIVE" : status);

        String error = validateWarehouse(warehouse, false);

        WarehouseDAO dao = new WarehouseDAO();
        if (error == null && dao.codeExists(code, id)) {
            error = "Warehouse code already exists.";
        }

        if (error != null) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.WARNING, "Validation error: " + error);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
            return;
        }

        boolean updated = dao.update(warehouse);
        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "Update result: " + updated);

        if (!updated) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to update warehouse.");
            return;
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleDeletePost(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Long id = parseLong(request.getParameter("id"), -1L);
        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "Delete warehouse ID: " + id);

        if (id == null || id <= 0) {
            Logger.getLogger(WarehouseController.class.getName()).log(Level.WARNING, "Invalid warehouse id: " + id);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
            return;
        }

        WarehouseDAO dao = new WarehouseDAO();
        boolean deleted = dao.delete(id);
        Logger.getLogger(WarehouseController.class.getName()).log(Level.INFO, "Delete result: " + deleted);

        if (!deleted) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to delete warehouse.");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/warehouse");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Long id = parseLong(request.getParameter("id"), -1L);
        if (id == null || id <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
            return;
        }

        WarehouseDAO dao = new WarehouseDAO();
        boolean deleted = dao.delete(id);
        if (!deleted) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to delete warehouse.");
            return;
        }

        // htmx will follow redirect and swap the returned HTML into #wrapper
        response.sendRedirect(request.getContextPath() + "/admin/warehouse");
    }

    private String validateWarehouse(Warehouse warehouse, boolean isCreate) {
        if (warehouse.getCode() == null || warehouse.getCode().isBlank()) {
            return "Code is required.";
        }
        if (warehouse.getName() == null || warehouse.getName().isBlank()) {
            return "Name is required.";
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

    private Long parseLong(String raw, Long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw);
        } catch (Exception e) {
            return def;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    public String getServletInfo() {
        return "Warehouse Controller";
    }
}
