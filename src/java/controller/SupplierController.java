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
import java.util.regex.Pattern;

@WebServlet(name = "SupplierController", urlPatterns = {"/admin/supplier", "/admin/supplier/*"})
public class SupplierController extends HttpServlet {

    private static final Long DEFAULT_PAGE = 1L;
    private static final Long DEFAULT_SIZE = 10L;
    private static final int MAX_ADDRESS_LENGTH = 255;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_PATTERN = "^[0-9\\+\\-\\(\\)\\s]{10,15}$";

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

            boolean hasSearch = searchRaw != null && !searchRaw.isBlank();
            if (hasSearch && total != null && total == 0) {
                request.setAttribute("emptySearchMessage",
                        "No matching suppliers found for your search.");
            }

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

    private void forwardCreateWithToast(HttpServletRequest request, HttpServletResponse response,
            Supplier s, String message, String toastType)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        setToast(session, message, toastType);
        request.setAttribute("supplier", s);
        request.getRequestDispatcher(ViewPath.SUPPLIER_CREATE).forward(request, response);
    }

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
            if (name.isEmpty()) {
                forwardCreateWithToast(request, response, s, "Name is required", "error");
                return;
            }

            if (email.isEmpty()) {
                forwardCreateWithToast(request, response, s, "Email is required", "error");
                return;
            }

            if (!isValidEmail(email)) {
                forwardCreateWithToast(request, response, s,
                        "Email format is invalid. Please enter a valid email address.", "error");
                return;
            }

            if (phone.isEmpty()) {
                forwardCreateWithToast(request, response, s, "Phone is required", "error");
                return;
            }

            if (!isValidPhone(phone)) {
                forwardCreateWithToast(request, response, s,
                        "Phone format is invalid. Please enter a valid phone number (10–15 characters).", "error");
                return;
            }

            if (supplierDao.phoneExists(phone, null)) {
                forwardCreateWithToast(request, response, s, "Phone number already exists", "error");
                return;
            }

            if (address.length() > MAX_ADDRESS_LENGTH) {
                forwardCreateWithToast(request, response, s,
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
                return;
            }

            String code = supplierDao.generateNextCode();
            s.setCode(code);

            supplierDao.create(s);
            setToast(request.getSession(true), "Supplier created successfully.", "success");
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("data too long")
                    && e.getMessage().toLowerCase().contains("address")) {
                forwardCreateWithToast(request, response, s,
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
            } else {
                forwardCreateWithToast(request, response, s, "Database error. Please try again.", "error");
            }
        }
    }

    private void forwardUpdateWithToast(HttpServletRequest request, HttpServletResponse response,
            Supplier s, String message, String toastType)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        setToast(session, message, toastType);
        request.setAttribute("supplier", s);
        request.getRequestDispatcher(ViewPath.SUPPLIER_UPDATE).forward(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
                return;
            }

            Long id = Long.valueOf(idRaw);
            if (supplierDao.getDetail(id) == null) {
                setToast(session, "Supplier not found.", "error");
                response.sendRedirect(request.getContextPath() + "/admin/supplier");
                return;
            }

            String code = request.getParameter("code") != null ? request.getParameter("code").trim() : "";
            String name = request.getParameter("name") != null ? request.getParameter("name").trim() : "";
            String email = request.getParameter("email") != null ? request.getParameter("email").trim() : "";
            String phone = request.getParameter("phone") != null ? request.getParameter("phone").trim() : "";
            String address = request.getParameter("address") != null ? request.getParameter("address").trim() : "";
            String status = request.getParameter("status") != null ? request.getParameter("status").trim() : "ACTIVE";

            Supplier s = new Supplier();
            s.setSupplierId(id);
            s.setCode(code);
            s.setName(name);
            s.setEmail(email);
            s.setPhone(phone);
            s.setAddress(address);
            s.setStatus(status);

            if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                forwardUpdateWithToast(request, response, s, "Invalid status.", "error");
                return;
            }

            if (code.isEmpty()) {
                forwardUpdateWithToast(request, response, s, "Code is required", "error");
                return;
            }

            if (name.isEmpty()) {
                forwardUpdateWithToast(request, response, s, "Name is required", "error");
                return;
            }

            if (email.isEmpty()) {
                forwardUpdateWithToast(request, response, s, "Email is required", "error");
                return;
            }

            if (!isValidEmail(email)) {
                forwardUpdateWithToast(request, response, s,
                        "Email format is invalid. Please enter a valid email address.", "error");
                return;
            }

            if (phone.isEmpty()) {
                forwardUpdateWithToast(request, response, s, "Phone is required", "error");
                return;
            }

            if (!isValidPhone(phone)) {
                forwardUpdateWithToast(request, response, s,
                        "Phone format is invalid. Please enter a valid phone number (10–15 characters).", "error");
                return;
            }

            if (supplierDao.phoneExists(phone, id)) {
                forwardUpdateWithToast(request, response, s, "Phone number already exists", "error");
                return;
            }

            if (address.length() > MAX_ADDRESS_LENGTH) {
                forwardUpdateWithToast(request, response, s,
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
                return;
            }

            if (supplierDao.codeExists(code, id)) {
                forwardUpdateWithToast(request, response, s, "Supplier code already exists", "error");
                return;
            }

            supplierDao.update(s);
            setToast(session, "Supplier updated successfully.", "success");
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("data too long")
                    && e.getMessage().toLowerCase().contains("address")) {
                setToast(session,
                        "Address is too long (max " + MAX_ADDRESS_LENGTH + " characters). Please shorten it.",
                        "error");
            } else {
                setToast(session, "Database error. Please try again.", "error");
            }
            response.sendRedirect(request.getContextPath() + "/admin/supplier");
        } catch (Exception e) {
            e.printStackTrace();
            setToast(session, "An error occurred. Please try again.", "error");
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

    private boolean isValidEmail(String email) {
        return Pattern.matches(EMAIL_PATTERN, email);
    }

    private boolean isValidPhone(String phone) {
        return Pattern.matches(PHONE_PATTERN, phone);
    }
}
