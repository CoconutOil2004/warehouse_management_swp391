package controller;

import dao.CustomerDAO;
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
import model.Customer;
import util.ViewPath;

@WebServlet(name = "CustomerController", urlPatterns = { "/admin/customer", "/admin/customer/*" })
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/activate".equals(path)) {
            try {
                String idRaw = request.getParameter("id");
                if (idRaw != null) {
                    Long id = Long.valueOf(idRaw);
                    customerDao.activate(id);
                }
                response.setHeader("HX-Location", request.getContextPath() + "/admin/customer");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Activate failed");
            }
            return;
        }

        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        Customer c = new Customer();
        c.setCode(code);
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        c.setStatus("ACTIVE");

        try {
            if (customerDao.codeExists(code, null)) {
                request.setAttribute("error", "Customer Code already exists");
                request.setAttribute("customer", c); // keep input
                request.getRequestDispatcher(ViewPath.CUSTOMER_CREATE).forward(request, response);
                return;
            }

            customerDao.create(c);
            response.sendRedirect(request.getContextPath() + "/admin/customer");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher(ViewPath.CUSTOMER_CREATE).forward(request, response);
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

            Customer c = new Customer();
            c.setCustomerId(id);
            c.setCode(code);
            c.setName(name);
            c.setEmail(email);
            c.setPhone(phone);
            c.setAddress(address);
            c.setStatus(status);

            if (customerDao.codeExists(code, id)) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Customer Code already exists");
                return;
            }

            customerDao.update(c);
            response.setHeader("HX-Location", request.getContextPath() + "/admin/customer");
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
                Long id = Long.valueOf(idRaw);
                customerDao.delete(id);
            }
            response.setHeader("HX-Location", request.getContextPath() + "/admin/customer");
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
