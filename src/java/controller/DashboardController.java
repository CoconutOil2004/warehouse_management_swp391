package controller;

import java.io.IOException;
import java.util.List;

import dao.ProductDAO;
import dao.WarehouseDAO;
import dto.ProductListDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.ViewPath;

@WebServlet(name = "DashboardController", urlPatterns = {"/admin/dashboard"})
public class DashboardController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Load products data
            ProductDAO productDAO = new ProductDAO();
            List<ProductListDTO> products = productDAO.getAllProducts(10, 0); // Top 10 products
            request.setAttribute("products", products);

            // Load warehouses data
            WarehouseDAO warehouseDAO = new WarehouseDAO();
            request.setAttribute("warehouses", warehouseDAO.getAll());

        } catch (Exception e) {
            // Log error but don't break the page
            e.printStackTrace();
        }

        request.getRequestDispatcher(ViewPath.ADMIN_DASHBOARD).forward(request, response);
    }

}
