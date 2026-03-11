package controller;

import dao.SupplierDAO;
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
import model.Supplier;
import util.ViewPath;

@WebServlet(name = "SupplierController", urlPatterns = {"/admin/supplier", "/admin/supplier/*"})
public class SupplierController extends HttpServlet {

    private static final Long DEFAULT_PAGE = 1L;
    private static final Long DEFAULT_SIZE = 10L;

    private final SupplierDAO supplierDao = new SupplierDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            viewList(request, response);
            return;
        }

        switch (path) {
            case "/create" ->
                viewCreate(request, response);
            case "/update" ->
                viewUpdate(request, response);
            case "/detail" ->
                viewDetail(request, response);
            default ->
                viewList(request, response);
        }
    }

    private void viewList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pageRaw = request.getParameter("page");
            Long page = pageRaw == null ? DEFAULT_PAGE : Long.valueOf(pageRaw);

            String sizeRaw = request.getParameter("size");
            Long size = sizeRaw == null ? DEFAULT_SIZE : Long.valueOf(sizeRaw);

            String sortRaw = request.getParameter("sort");
            String sort = (sortRaw == null || sortRaw.isEmpty()) ? "name" : sortRaw;

            String searchRaw = request.getParameter("search");
            String search = searchRaw == null ? "%%" : "%" + searchRaw + "%";

            Long total = supplierDao.getPageCount(search);
            Long pages = (total + size - 1) / size;
            var suppliers = supplierDao.getList(search, sort, page, size);

            request.setAttribute("page", page);
            request.setAttribute("size", size);
            request.setAttribute("sort", sortRaw);
            request.setAttribute("search", searchRaw);
            request.setAttribute("pages", pages);
            request.setAttribute("total", total);
            request.setAttribute("suppliers", suppliers);

            request.getRequestDispatcher(ViewPath.SUPPLIER_LIST).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading suppliers");
        }
    }

    private void viewCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
    }

    private void viewDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
                return;
            }
            Long id = Long.valueOf(idRaw);
            Supplier supplier = supplierDao.getDetail(id);
            if (supplier == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Supplier not found");
                return;
            }
            request.setAttribute("supplier", supplier);
            request.getRequestDispatcher(ViewPath.SUPPLIER_DETAIL).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading supplier detail");
        }
    }

    private void viewUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
                return;
            }
            Long id = Long.valueOf(idRaw);
            Supplier supplier = supplierDao.getDetail(id);
            if (supplier == null) {
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
                return;
            }
            request.setAttribute("supplier", supplier);
            request.getRequestDispatcher(ViewPath.SUPPLIER_UPDATE).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        Supplier s = new Supplier();
        s.setCode(code);
        s.setName(name);
        s.setEmail(email);
        s.setPhone(phone);
        s.setAddress(address);
        s.setStatus("ACTIVE");

        try {
            if (!supplierDao.codeExists(code, null)) {
                request.setAttribute("error", "Supplier Code already exists");
                request.setAttribute("supplier", s); // keep input
                request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
                return;
            }

            supplierDao.create(s);
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, String> params = parseFormBody(request);

            String idRaw = params.get("id");
            if (idRaw == null) {
                idRaw = request.getParameter("id");
            }

            Long id = Long.valueOf(idRaw);
            String code = params.get("code");
            String name = params.get("name");
            String email = params.get("email");
            String phone = params.get("phone");
            String address = params.get("address");
            String status = params.get("status");

            Supplier s = new Supplier();
            s.setSupplierId(id);
            s.setCode(code);
            s.setName(name);
            s.setEmail(email);
            s.setPhone(phone);
            s.setAddress(address);
            s.setStatus(status);

            if (supplierDao.codeExists(code, id)) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Supplier Code already exists");
                return;
            }

            supplierDao.update(s);
            response.setHeader("HX-Location", request.getContextPath() + "/admin/supplier");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                Long id = Long.valueOf(idRaw); // kieu dl cua id
                supplierDao.delete(id);
            }
            response.setHeader("HX-Location", request.getContextPath() + "/admin/supplier");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Delete failed");
        }
    }

    private Map<String, String> parseFormBody(HttpServletRequest request) throws IOException {
        Map<String, String> params = new HashMap<>();
        byte[] bytes = request.getInputStream().readAllBytes();
        String body = new String(bytes, StandardCharsets.UTF_8);

        if (!body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return params;
    }
}
