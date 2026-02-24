package controller;

import dao.ShipmentDAO;
import model.Shipment;
import util.ViewPath;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "ShipmentController", urlPatterns = { "/shipment" })
public class ShipmentController extends HttpServlet {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "list";

        try {
            switch (action) {
                case "list" -> handleList(request, response);
                case "create" -> handleCreate(request, response);
                case "detail" -> handleDetail(request, response);
                case "edit" -> handleEdit(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/shipment?action=list");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/shipment?action=list");
            return;
        }
        try {
            switch (action) {
                case "store" -> handleStore(request, response);
                case "update" -> handleUpdate(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/shipment?action=list");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String shipmentNumber = request.getParameter("shipmentNumber");
        String carrierIdStr = request.getParameter("carrierId");
        Long carrierId = (carrierIdStr != null && !carrierIdStr.isEmpty()) ? Long.valueOf(carrierIdStr) : null;
        String status = request.getParameter("status");
        String shipmentType = request.getParameter("shipmentType");
        String sortBy = request.getParameter("sortBy");
        String order = request.getParameter("order");

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty())
            page = Integer.parseInt(pageStr);
        int limit = 10;
        int offset = (page - 1) * limit;

        List<dto.ShipmentListDTO> shipments = shipmentDAO.getFilteredShipments(shipmentNumber, carrierId, status,
                shipmentType,
                sortBy, order, limit, offset);
        int totalRecords = shipmentDAO.countFilteredShipments(shipmentNumber, carrierId, status, shipmentType);
        int totalPages = (int) Math.ceil((double) totalRecords / limit);

        request.setAttribute("shipments", shipments);
        request.setAttribute("shipmentType", shipmentType);
        request.setAttribute("carriers", shipmentDAO.getAllCarriers());
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.getRequestDispatcher(ViewPath.SHIPMENT_LIST).forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("carriers", shipmentDAO.getAllCarriers());
        request.setAttribute("gdns", shipmentDAO.getAvailableGDNs());
        request.setAttribute("nextShipmentNumber", shipmentDAO.getNextShipmentNumber());
        request.getRequestDispatcher(ViewPath.SHIPMENT_CREATE).forward(request, response);
    }

    private void handleStore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        Shipment s = new Shipment();
        s.setShipmentNumber(request.getParameter("shipmentNumber"));
        s.setGdnId(Long.valueOf(request.getParameter("gdnId")));
        String carrierIdStr = request.getParameter("carrierId");
        s.setCarrierId((carrierIdStr != null && !carrierIdStr.isBlank()) ? Long.valueOf(carrierIdStr) : null);
        s.setShipmentType(request.getParameter("shipmentType"));
        s.setTrackingCode(request.getParameter("trackingCode"));
        s.setNote(request.getParameter("note"));

        shipmentDAO.createShipment(s);
        response.sendRedirect(request.getContextPath() + "/shipment?action=list");
    }

    private void handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        Long id = Long.valueOf(request.getParameter("id"));
        Shipment s = shipmentDAO.getById(id);
        if (s == null) {
            response.sendRedirect(request.getContextPath() + "/shipment?action=list");
            return;
        }
        request.setAttribute("shipment", s);
        request.getRequestDispatcher(ViewPath.SHIPMENT_DETAIL).forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        Long id = Long.valueOf(request.getParameter("id"));
        Shipment s = shipmentDAO.getById(id);
        if (s == null || "CANCELLED".equals(s.getStatus()) || "DELIVERED".equals(s.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/shipment?action=detail&id=" + id);
            return;
        }
        request.setAttribute("shipment", s);
        request.setAttribute("carriers", shipmentDAO.getAllCarriers());
        request.getRequestDispatcher(ViewPath.SHIPMENT_EDIT).forward(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        Long id = Long.valueOf(request.getParameter("id"));
        Shipment s = shipmentDAO.getById(id);
        if (s != null) {
            if ("CANCELLED".equals(s.getStatus()) || "DELIVERED".equals(s.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/shipment?action=detail&id=" + id);
                return;
            }
            String newStatus = request.getParameter("status");
            String carrierIdStr = request.getParameter("carrierId");

            // Only allow carrier change if currently CREATED
            if ("CREATED".equals(s.getStatus())) {
                s.setCarrierId((carrierIdStr != null && !carrierIdStr.isBlank()) ? Long.valueOf(carrierIdStr) : null);
            }
            s.setTrackingCode(request.getParameter("trackingCode"));
            s.setNote(request.getParameter("note"));

            // Log logic for status changes
            if (newStatus != null && s.getStatus() != null && !s.getStatus().equals(newStatus)) {
                if ("PICKED_UP".equals(newStatus) || "IN_TRANSIT".equals(newStatus)) {
                    if (s.getPickedUpAt() == null) {
                        s.setPickedUpAt(java.time.LocalDateTime.now());
                    }
                }
                if ("DELIVERED".equals(newStatus)) {
                    if (s.getDeliveredAt() == null) {
                        s.setDeliveredAt(java.time.LocalDateTime.now());
                    }
                    if (s.getPickedUpAt() == null) {
                        s.setPickedUpAt(java.time.LocalDateTime.now());
                    }
                }
            }
            if (newStatus != null && !newStatus.isBlank()) {
                s.setStatus(newStatus);
            }
            shipmentDAO.updateShipment(s);
        }
        response.sendRedirect(request.getContextPath() + "/shipment?action=detail&id=" + id);
    }
}
