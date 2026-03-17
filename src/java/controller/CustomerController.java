package controller;

import dao.CustomerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import model.Customer;
import util.ViewPath;

@WebServlet(name = "CustomerController", urlPatterns = {"/admin/customer", "/admin/customer/*"})
public class CustomerController extends HttpServlet {

    private static final Long DEFAULT_PAGE = 1L;
    private static final Long DEFAULT_SIZE = 10L;

    private final CustomerDAO customerDao = new CustomerDAO();

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
                response.sendRedirect(request.getContextPath() + "/admin/customer");
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

            Long total = customerDao.getPageCount(search);
            Long pages = (total + size - 1) / size;
            var customers = customerDao.getList(search, sort, page, size);

            request.setAttribute("page", page);
            request.setAttribute("size", size);
            request.setAttribute("sort", sortRaw);
            request.setAttribute("search", searchRaw);
            request.setAttribute("pages", pages);
            request.setAttribute("total", total);
            request.setAttribute("customers", customers);

            request.getRequestDispatcher(ViewPath.CUSTOMER_LIST).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading customers");
        }
    }

    private void viewCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(ViewPath.CUSTOMER_CREATE).forward(request, response);
    }

    private void viewDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/customer");
                return;
            }
            Long id = Long.valueOf(idRaw);
            Customer customer = customerDao.getDetail(id);
            if (customer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found");
                return;
            }
            request.setAttribute("customer", customer);
            request.getRequestDispatcher(ViewPath.CUSTOMER_DETAIL).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading customer detail");
        }
    }

    private void viewUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/customer");
                return;
            }
            Long id = Long.valueOf(idRaw);
            Customer customer = customerDao.getDetail(id);
            if (customer == null) {
                response.sendRedirect(request.getContextPath() + "/admin/customer");
                return;
            }
            request.setAttribute("customer", customer);
            request.getRequestDispatcher(ViewPath.CUSTOMER_UPDATE).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        }
    }

    // ======================== POST handlers ========================

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name") != null ? request.getParameter("name").trim() : "";
        String email = request.getParameter("email") != null ? request.getParameter("email").trim() : "";
        String phone = request.getParameter("phone") != null ? request.getParameter("phone").trim() : "";
        String address = request.getParameter("address") != null ? request.getParameter("address").trim() : "";

        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        c.setStatus("ACTIVE");

        try {
            // Validate required fields
            if (name.isEmpty()) {
                request.setAttribute("error", "Name is required");
                request.setAttribute("customer", c);
                request.getRequestDispatcher(ViewPath.CUSTOMER_CREATE).forward(request, response);
                return;
            }

            // Auto-generate code
            String code = customerDao.generateNextCode();
            c.setCode(code);

            customerDao.create(c);
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher(ViewPath.CUSTOMER_CREATE).forward(request, response);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null) {
                response.sendRedirect(request.getContextPath() + "/admin/customer");
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
                Customer old = customerDao.getDetail(id);
                request.setAttribute("error", "Code and Name are required");
                request.setAttribute("customer", old);
                request.getRequestDispatcher(ViewPath.CUSTOMER_UPDATE).forward(request, response);
                return;
            }

            Customer c = new Customer();
            c.setCustomerId(id);
            c.setCode(code);
            c.setName(name);
            c.setEmail(email);
            c.setPhone(phone);
            c.setAddress(address);
            c.setStatus(status);

            // Check if code already exists for another customer
            if (customerDao.codeExists(code, id)) {
                request.setAttribute("error", "Customer Code already exists");
                request.setAttribute("customer", c);
                request.getRequestDispatcher(ViewPath.CUSTOMER_UPDATE).forward(request, response);
                return;
            }

            customerDao.update(c);
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                Long id = Long.valueOf(idRaw);
                customerDao.delete(id);
            }
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        }
    }
    
}
