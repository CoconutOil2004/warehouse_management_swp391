package controller;

import dao.SupplierDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.Supplier;
import util.ViewPath;

@WebServlet(name = "SupplierController", urlPatterns = {"/admin/supplier", "/admin/supplier/*"})
public class SupplierController extends HttpServlet {

    private static final Long DEFAULT_PAGE = 1L;
    private static final Long DEFAULT_SIZE = 10L;
    private static final int MAX_ADDRESS_LENGTH = 255;

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) {
            path = "/create";
        }

        switch (path) {
            case "/create" ->
                handleCreate(request, response);
            case "/update" ->
                handleUpdate(request, response);
            case "/delete" ->
                handleDelete(request, response);
            default ->
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
        }
    }

    // ======================== GET handlers ========================

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

    // ======================== POST handlers ========================

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name") != null ? request.getParameter("name").trim() : "";
        String email = request.getParameter("email") != null ? request.getParameter("email").trim() : "";
        String phone = request.getParameter("phone") != null ? request.getParameter("phone").trim() : "";
        String address = request.getParameter("address") != null ? request.getParameter("address").trim() : "";

        Supplier s = new Supplier();
        s.setName(name);
        s.setEmail(email);
        s.setPhone(phone);
        s.setAddress(address);
        s.setStatus("ACTIVE");

        try {
            // Validate required fields
            if (name.isEmpty()) {
                setToast(request.getSession(true), "Name is required", "error");
                request.setAttribute("supplier", s);
                request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
                return;
            }

            if (address.length() > MAX_ADDRESS_LENGTH) {
                setToast(request.getSession(true),
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
                request.setAttribute("supplier", s);
                request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
                return;
            }

            // Auto-generate code
            String code = supplierDao.generateNextCode();
            s.setCode(code);

            supplierDao.create(s);
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("data too long")
                    && e.getMessage().toLowerCase().contains("address")) {
                setToast(request.getSession(true),
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
            } else {
                setToast(request.getSession(true), "Database error. Please try again.", "error");
            }
            request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
                return;
            }

            Long id = Long.valueOf(idRaw);
            String code = request.getParameter("code") != null ? request.getParameter("code").trim() : "";
            String name = request.getParameter("name") != null ? request.getParameter("name").trim() : "";
            String email = request.getParameter("email") != null ? request.getParameter("email").trim() : "";
            String phone = request.getParameter("phone") != null ? request.getParameter("phone").trim() : "";
            String address = request.getParameter("address") != null ? request.getParameter("address").trim() : "";
            String status = request.getParameter("status") != null ? request.getParameter("status").trim() : "ACTIVE";

            // Validate required fields
            if (code.isEmpty() || name.isEmpty()) {
                Supplier old = supplierDao.getDetail(id);
                setToast(request.getSession(true), "Code and Name are required", "error");
                request.setAttribute("supplier", old);
                request.getRequestDispatcher(ViewPath.SUPPLIER_UPDATE).forward(request, response);
                return;
            }

            Supplier s = new Supplier();
            s.setSupplierId(id);
            s.setCode(code);
            s.setName(name);
            s.setEmail(email);
            s.setPhone(phone);
            s.setAddress(address);
            s.setStatus(status);

            if (address.length() > MAX_ADDRESS_LENGTH) {
                setToast(request.getSession(true),
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
                request.setAttribute("supplier", s);
                request.getRequestDispatcher(ViewPath.SUPPLIER_UPDATE).forward(request, response);
                return;
            }

            // Check if code already exists for another supplier
            if (supplierDao.codeExists(code, id)) {
                setToast(request.getSession(true), "Supplier Code already exists", "error");
                request.setAttribute("supplier", s);
                request.getRequestDispatcher(ViewPath.SUPPLIER_UPDATE).forward(request, response);
                return;
            }

            supplierDao.update(s);
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("data too long")
                    && e.getMessage().toLowerCase().contains("address")) {
                setToast(request.getSession(true),
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
            } else {
                setToast(request.getSession(true), "Database error. Please try again.", "error");
            }
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (Exception e) {
            e.printStackTrace();
            setToast(request.getSession(true), "An error occurred. Please try again.", "error");
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                Long id = Long.valueOf(idRaw);
                supplierDao.delete(id);
            }
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        }
    }

    private void setToast(HttpSession session, String message, String type) {
        session.setAttribute("message", message);
        session.setAttribute("type", type);
    }
}
