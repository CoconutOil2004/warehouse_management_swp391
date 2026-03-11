package controller;

import dto.PageResponseDTO;
import dto.UserDTO;
import dto.UserRequest;
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
import java.util.Map;
import service.RoleService;
import service.UserService;
import service.WarehouseService;
import util.ViewPath;

@WebServlet(name = "UserController", urlPatterns = {"/admin/user", "/admin/user/*"})
public class UserController extends HttpServlet {

    private static final Long DEFAULT_PAGE = 1L;
    private static final Long DEFAULT_SIZE = 10L;

    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();
    private final WarehouseService warehouseService = new WarehouseService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        var path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            viewList(request, response);
            return;
        }

        switch (path) {
            case "/create" -> viewCreate(request, response);
            case "/update" -> viewUpdate(request, response);
            case "/detail" -> viewDetail(request, response);
            case "/restore" -> restoreUser(request, response);
            default -> viewList(request, response);
        }
    }

    private void viewList(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            var pageRaw = request.getParameter("page");
            var page = pageRaw == null ? DEFAULT_PAGE : Long.valueOf(pageRaw);

            var sizeRaw = request.getParameter("size");
            var size = sizeRaw == null ? DEFAULT_SIZE : Long.valueOf(sizeRaw);

            var sortRaw = request.getParameter("sort");
            var sort = (sortRaw == null || sortRaw.isEmpty()) ? "user_id" : sortRaw;

            var searchRaw = request.getParameter("search");
            var search = searchRaw == null ? "%%" : "%" + searchRaw + "%";

            var roleIdRaw = request.getParameter("roleId");
            var roleId = (roleIdRaw == null || roleIdRaw.isEmpty()) ? null : Long.valueOf(roleIdRaw);

            var statusRaw = request.getParameter("status");
            var status = (statusRaw == null || statusRaw.isEmpty()) ? null : statusRaw;

            var isDeletedRaw = request.getParameter("isDeleted");
            Boolean isDeleted = null;
            if (isDeletedRaw != null && !isDeletedRaw.isEmpty()) {
                isDeleted = "1".equals(isDeletedRaw) || "true".equalsIgnoreCase(isDeletedRaw);
            }

            Long currentUserId = ((model.User) request.getSession().getAttribute("USER")).getUserId();
            PageResponseDTO<UserDTO> pageResponse = userService.getPagedList(search, sort, page, size, roleId, status, isDeleted, currentUserId);

            request.setAttribute("page", page);
            request.setAttribute("size", size);
            request.setAttribute("sort", sortRaw);
            request.setAttribute("search", searchRaw);
            request.setAttribute("roleId", roleId);
            request.setAttribute("status", status);
            request.setAttribute("isDeleted", isDeletedRaw);
            request.setAttribute("pages", pageResponse.getTotalPages());
            request.setAttribute("total", pageResponse.getTotalItems());
            request.setAttribute("users", pageResponse.getItems());
            request.setAttribute("availableRoles", roleService.getAll());

            request.getRequestDispatcher(ViewPath.USER_LIST).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewDetail(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            UserDTO user = userService.getById(id);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }
            
            request.setAttribute("user", user);
            request.setAttribute("rolePermissionsMap", userService.getRolePermissionsMap(id));
            request.getRequestDispatcher(ViewPath.USER_DETAIL).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading detail");
        }
    }

    private void viewCreate(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            request.setAttribute("roles", roleService.getAll());
            request.setAttribute("warehouses", warehouseService.getAll());
            request.getRequestDispatcher(ViewPath.USER_CREATE).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            viewList(request, response);
        }
    }

    private void viewUpdate(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            UserDTO user = userService.getById(id);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/admin/user");
                return;
            }
            request.setAttribute("user", user);
            request.setAttribute("roles", roleService.getAll());
            request.setAttribute("selectedRoleIds", new java.util.HashSet<>(userService.getRolesByUserId(id)));
            request.setAttribute("warehouses", warehouseService.getAll());
            request.getRequestDispatcher(ViewPath.USER_UPDATE).forward(request, response);
        } catch (ServletException | IOException | NumberFormatException | SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/user");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        UserRequest uReq = new UserRequest();
        uReq.setUsername(request.getParameter("username"));
        uReq.setFullName(request.getParameter("fullName"));
        uReq.setEmail(request.getParameter("email"));
        uReq.setPhone(request.getParameter("phone"));
        uReq.setPassword(request.getParameter("password"));
        uReq.setStatus(request.getParameter("status"));
        
        String wIdRaw = request.getParameter("warehouseId");
        if (wIdRaw != null && !wIdRaw.isBlank()) uReq.setWarehouseId(Long.valueOf(wIdRaw));
        
        String[] rIds = request.getParameterValues("roleIds");
        if (rIds != null) {
            uReq.setRoleIds(java.util.Arrays.stream(rIds).map(Long::valueOf).toList());
        }

        Long createdBy = (Long) request.getSession().getAttribute("userId");

        try {
            userService.create(uReq, createdBy);
            request.getSession().setAttribute("message", "Tạo người dùng thành công");
            response.sendRedirect(request.getContextPath() + "/admin/user");
        } catch (util.ValidationException e) {
            request.getSession().setAttribute("message", e.getMessage());
            request.setAttribute("user", uReq);
            viewCreate(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Lỗi cơ sở dữ liệu khi tạo người dùng");
            viewList(request, response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            Map<String, String[]> params = parseFormBodyExtended(request);

            String idRaw = request.getParameter("id");
            if (idRaw == null && params.containsKey("id"))
                idRaw = params.get("id")[0];
            Long id = Long.valueOf(idRaw);

            UserRequest uReq = new UserRequest();
            uReq.setFullName(params.get("fullName")[0]);
            uReq.setEmail(params.get("email")[0]);
            uReq.setPhone(params.get("phone")[0]);
            uReq.setStatus(params.get("status")[0]);
            
            String wIdRaw = params.containsKey("warehouseId") ? params.get("warehouseId")[0] : null;
            if (wIdRaw != null && !wIdRaw.isBlank()) uReq.setWarehouseId(Long.valueOf(wIdRaw));
            
            String[] rIds = params.get("roleIds");
            if (rIds != null) {
                uReq.setRoleIds(java.util.Arrays.stream(rIds).map(Long::valueOf).toList());
            }

            try {
                userService.update(id, uReq);
                request.getSession().setAttribute("message", "Cập nhật người dùng thành công");
                response.setHeader("HX-Location", request.getContextPath() + "/admin/user");
            } catch (util.ValidationException e) {
                request.getSession().setAttribute("message", e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Cập nhật người dùng thất bại");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cập nhật thất bại");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                Long id = Long.valueOf(idRaw);
                userService.delete(id);
                request.getSession().setAttribute("message", "Xóa người dùng thành công");
            }
            response.setHeader("HX-Location", request.getContextPath() + "/admin/user");
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Xóa người dùng thất bại");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Xóa thất bại");
        }
    }

    private void restoreUser(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                Long id = Long.valueOf(idRaw);
                userService.restore(id);
                request.getSession().setAttribute("message", "Khôi phục người dùng thành công");
            }
            response.setHeader("HX-Redirect", request.getContextPath() + "/admin/user");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Khôi phục người dùng thất bại");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Khôi phục thất bại");
        }
    }

    private Map<String, String[]> parseFormBodyExtended(HttpServletRequest request) throws IOException {
        Map<String, java.util.List<String>> params = new HashMap<>();
        byte[] bytes = request.getInputStream().readAllBytes();
        String body = new String(bytes, StandardCharsets.UTF_8);

        if (!body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    params.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(value);
                }
            }
        }

        Map<String, String[]> resultMap = new HashMap<>();
        params.forEach((k, v) -> resultMap.put(k, v.toArray(new String[0])));
        return resultMap;
    }
}
