package controller;

import dao.WarehouseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Warehouse;
import util.ViewPath;

@WebServlet(name = "WarehouseController", urlPatterns = {"/admin/warehouse", "/admin/warehouse/*"})
public class WarehouseController extends HttpServlet {

    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_SIZE = 10;

    private final WarehouseDAO warehouseDao = new WarehouseDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        var path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            viewList(request, response);
            return;
        }
        switch (path) {
            case "/create" -> viewCreate(request, response);
            case "/update" -> viewUpdate(request, response);
            case "/detail" -> viewDetail(request, response);
            default -> viewList(request, response);
        }
    }

    private void viewList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long page = parseLong(request.getParameter("page"), DEFAULT_PAGE);
            long size = parseLong(request.getParameter("size"), DEFAULT_SIZE);
            String sortRaw = request.getParameter("sort");
            String sort = (sortRaw == null || sortRaw.isBlank()) ? "name" : sortRaw;
            String searchRaw = request.getParameter("search");
            String searchForQuery = (searchRaw == null || searchRaw.isBlank()) ? "%%" : "%" + searchRaw.trim() + "%";

            if (page < 1) page = DEFAULT_PAGE;
            if (size < 1) size = DEFAULT_SIZE;

            long total = warehouseDao.getPageCount(searchForQuery);
            long pages = total == 0 ? 1 : (long) Math.ceil((double) total / size);
            if (page > pages) page = pages;

            List<Warehouse> warehouses = warehouseDao.getList(searchForQuery, sort, page, size);

            request.setAttribute("warehouses", warehouses);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
            request.setAttribute("size", size);
            request.setAttribute("total", total);
            request.setAttribute("search", searchRaw);
            request.setAttribute("sort", sort);
            request.getRequestDispatcher(ViewPath.WAREHOUSE_LIST).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading list");
        }
    }

    private void viewCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("warehouse", new Warehouse());
        request.getRequestDispatcher(ViewPath.WAREHOUSE_CREATE).forward(request, response);
    }

    private void viewUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = parseLong(request.getParameter("id"), -1L);
            if (id == null || id <= 0) {
                response.sendRedirect(request.getContextPath() + "/admin/warehouse");
                return;
            }
            Warehouse warehouse = warehouseDao.getDetail(id);
            if (warehouse == null) {
                response.sendRedirect(request.getContextPath() + "/admin/warehouse");
                return;
            }
            request.setAttribute("warehouse", warehouse);
            request.getRequestDispatcher(ViewPath.WAREHOUSE_UPDATE).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/warehouse");
        }
    }

    private void viewDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = parseLong(request.getParameter("id"), -1L);
            if (id == null || id <= 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid warehouse id");
                return;
            }
            Warehouse warehouse = warehouseDao.getDetail(id);
            if (warehouse == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Warehouse not found");
                return;
            }
            request.setAttribute("warehouse", warehouse);
            request.getRequestDispatcher(ViewPath.WAREHOUSE_DETAIL).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading detail");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        var path = request.getPathInfo();
        if (path == null || path.equals("/") || path.equals("/create")) {
            create(request, response);
            return;
        }
        if (path.equals("/update")) {
            performUpdate(request, response, null);
            return;
        }
        if (path.equals("/delete")) {
            performDelete(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = trim(request.getParameter("code"));
        String name = trim(request.getParameter("name"));
        String address = trim(request.getParameter("address"));

        Warehouse warehouse = new Warehouse();
        warehouse.setCode(code);
        warehouse.setName(name);
        warehouse.setAddress(address);

        String error = validateWarehouse(warehouse, true);
        if (error == null) {
            try {
                if (warehouseDao.codeExists(code, null)) {
                    error = "Warehouse code already exists.";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                error = "Failed to create warehouse. Please try again.";
            }
        }
        if (error == null) {
            try {
                if (warehouseDao.create(warehouse)) {
                    response.sendRedirect(request.getContextPath() + "/admin/warehouse");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            error = "Failed to create warehouse. Please try again.";
        }
        request.setAttribute("error", error);
        request.setAttribute("warehouse", warehouse);
        viewCreate(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            Map<String, String> params = parseFormBody(request);
            performUpdate(request, response, params);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cập nhật thất bại");
        }
    }

    private void performUpdate(HttpServletRequest request, HttpServletResponse response, Map<String, String> formBody)
            throws ServletException, IOException {
        try {
            String idRaw = formBody != null ? formBody.get("id") : request.getParameter("id");
            if (idRaw == null) idRaw = request.getParameter("id");
            Long id = Long.parseLong(idRaw);
            if (id <= 0) {
                sendUpdateError(request, response, id, "Invalid warehouse id", null);
                return;
            }

            String code = formBody != null ? formBody.get("code") : request.getParameter("code");
            String name = formBody != null ? formBody.get("name") : request.getParameter("name");
            String address = formBody != null ? formBody.get("address") : request.getParameter("address");
            String status = formBody != null ? formBody.get("status") : request.getParameter("status");

            code = code != null ? code.trim() : "";
            name = name != null ? name.trim() : "";
            address = address != null ? address.trim() : null;
            status = (status == null || status.isBlank()) ? "ACTIVE" : status.trim();

            Warehouse warehouse = new Warehouse();
            warehouse.setWarehouseId(id);
            warehouse.setCode(code);
            warehouse.setName(name);
            warehouse.setAddress(address);
            warehouse.setStatus(status);

            String error = validateWarehouse(warehouse, false);
            if (error == null && warehouseDao.codeExists(code, id)) {
                error = "Warehouse code already exists.";
            }
            if (error != null) {
                sendUpdateError(request, response, id, error, warehouse);
                return;
            }
            if (!warehouseDao.update(warehouse)) {
                sendUpdateError(request, response, id, "Failed to update warehouse. Please try again.", warehouse);
                return;
            }
            if (formBody != null) {
                response.setHeader("HX-Location", request.getContextPath() + "/admin/warehouse");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/warehouse");
            }
        } catch (NumberFormatException e) {
            sendUpdateError(request, response, -1L, "Invalid warehouse id", null);
        } catch (SQLException e) {
            e.printStackTrace();
            sendUpdateError(request, response, -1L, "Failed to update warehouse. Please try again.", null);
        }
    }

    private void sendUpdateError(HttpServletRequest request, HttpServletResponse response, long id, String error, Warehouse warehouse)
            throws ServletException, IOException {
        if (request.getHeader("HX-Request") != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(error);
        } else {
            request.setAttribute("error", error);
            if (warehouse != null) {
                request.setAttribute("warehouse", warehouse);
            } else if (id > 0) {
                try {
                    request.setAttribute("warehouse", warehouseDao.getDetail(id));
                } catch (SQLException e) {
                    request.setAttribute("warehouse", new Warehouse());
                }
            } else {
                request.setAttribute("warehouse", new Warehouse());
            }
            request.getRequestDispatcher(ViewPath.WAREHOUSE_UPDATE).forward(request, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            performDelete(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Xóa thất bại");
        }
    }

    private void performDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                Long id = Long.valueOf(idRaw);
                if (id > 0) {
                    if (warehouseDao.hasDependencies(id)) {
                        if ("POST".equals(request.getMethod())) {
                            request.setAttribute("error", "Cannot delete warehouse: it has zones, goods receipts, delivery notes or users assigned. Remove or reassign them first.");
                            viewList(request, response);
                            return;
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write("Cannot delete warehouse: it has zones, goods receipts, delivery notes or users assigned.");
                            return;
                        }
                    }
                    warehouseDao.delete(id);
                }
            }
            if ("POST".equals(request.getMethod())) {
                response.sendRedirect(request.getContextPath() + "/admin/warehouse");
            } else {
                response.setHeader("HX-Location", request.getContextPath() + "/admin/warehouse");
            }
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            if ("POST".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Xóa thất bại");
            } else {
                response.setHeader("HX-Location", request.getContextPath() + "/admin/warehouse");
            }
        }
    }

    private Map<String, String> parseFormBody(HttpServletRequest request) throws IOException {
        Map<String, String> params = new HashMap<>();
        byte[] bytes = request.getInputStream().readAllBytes();
        String body = new String(bytes, StandardCharsets.UTF_8);
        if (!body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private String validateWarehouse(Warehouse warehouse, boolean isCreate) {
        if (warehouse.getCode() == null || warehouse.getCode().isBlank()) {
            return "Code is required.";
        }
        String code = warehouse.getCode().trim();
        if (code.length() > 50) {
            return "Code must not exceed 50 characters.";
        }
        if (warehouse.getName() == null || warehouse.getName().isBlank()) {
            return "Name is required.";
        }
        if (warehouse.getName() != null && warehouse.getName().trim().length() > 255) {
            return "Name must not exceed 255 characters.";
        }
        return null;
    }

    private long parseLong(String raw, long def) {
        if (raw == null || raw.isBlank()) return def;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
