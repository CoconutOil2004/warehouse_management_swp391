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
                case "delete" -> handleDelete(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/shipment?action=list");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // Ham hien thi danh sach lo hang, ho tro loc va phan trang
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
        String limitStr = request.getParameter("size");
        if (limitStr != null && !limitStr.isEmpty())
            limit = Integer.parseInt(limitStr);

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
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("pageSize", limit);
        request.getRequestDispatcher(ViewPath.SHIPMENT_LIST).forward(request, response);
    }

    // Ham chuan bi du lieu (carrier, gdn) de hien thi tren form tao moi
    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("carriers", shipmentDAO.getAllCarriers());
        request.setAttribute("gdns", shipmentDAO.getAvailableGDNs());
        request.setAttribute("nextShipmentNumber", shipmentDAO.getNextShipmentNumber());
        String gdnIdParam = request.getParameter("gdnId");
        if (gdnIdParam != null && !gdnIdParam.isBlank()) {
            try {
                Long gdnId = Long.valueOf(gdnIdParam.trim());
                // Validate that the GDN is actually SHIPPING
                dao.GoodsDeliveryNoteDAO gdnDao = new dao.GoodsDeliveryNoteDAO();
                try {
                    dto.GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
                    if (gdn == null || !"SHIPPING".equals(gdn.getStatus())) {
                        request.setAttribute("error", "Lỗi: Chỉ có thể tạo lô hàng cho Phiếu Xuất Kho đã hoàn thành packing (SHIPPING).");
                        request.getRequestDispatcher(ViewPath.SHIPMENT_CREATE).forward(request, response);
                        return;
                    }

                    // Check if shipment already exists for this GDN
                    List<model.Shipment> existingShipments = shipmentDAO.getByGdnId(gdnId);
                    if (existingShipments != null && !existingShipments.isEmpty()) {
                        request.setAttribute("error", "Error: This GDN already has a shipment.");
                        request.getRequestDispatcher(ViewPath.SHIPMENT_CREATE).forward(request, response);
                        return;
                    }
                } catch (Exception e) {
                    request.setAttribute("error", "Error validating GDN status: " + e.getMessage());
                }
                request.setAttribute("selectedGdnId", gdnId);
            } catch (NumberFormatException ignored) { }
        }
        request.getRequestDispatcher(ViewPath.SHIPMENT_CREATE).forward(request, response);
    }

    // Ham luu thong tin lo hang moi vao database
    private void handleStore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String gdnIdStr = request.getParameter("gdnId");
        if (gdnIdStr == null || gdnIdStr.isBlank()) {
            request.setAttribute("error", "Lỗi: Chưa chọn Phiếu Xuất Kho (GDN).");
            handleCreate(request, response);
            return;
        }

        Long gdnId = Long.valueOf(gdnIdStr);
        // Strict server-side validation: must be SHIPPING
        dao.GoodsDeliveryNoteDAO gdnDao = new dao.GoodsDeliveryNoteDAO();
        try {
            dto.GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
            if (gdn == null || !"SHIPPING".equals(gdn.getStatus())) {
                request.setAttribute("error", "Lỗi: Phiếu Xuất Kho phải ở trạng thái SHIPPING mới có thể tạo lô hàng.");
                handleCreate(request, response);
                return;
            }

            // Check if shipment already exists for this GDN
            List<model.Shipment> existingShipments = shipmentDAO.getByGdnId(gdnId);
            if (existingShipments != null && !existingShipments.isEmpty()) {
                request.setAttribute("error", "Error: This GDN already has a shipment.");
                handleCreate(request, response);
                return;
            }
        } catch (Exception e) {
            request.setAttribute("error", "System error while validating GDN: " + e.getMessage());
            handleCreate(request, response);
            return;
        }

        Shipment s = new Shipment();
        s.setShipmentNumber(request.getParameter("shipmentNumber"));
        s.setGdnId(gdnId);
        String carrierIdStr = request.getParameter("carrierId");
        s.setCarrierId((carrierIdStr != null && !carrierIdStr.isBlank()) ? Long.valueOf(carrierIdStr) : null);
        s.setShipmentType(request.getParameter("shipmentType"));
        s.setTrackingCode(request.getParameter("trackingCode"));
        s.setNote(request.getParameter("note"));

        shipmentDAO.createShipment(s);
        request.getSession().setAttribute("message", "Lô hàng đã được tạo thành công!");
        request.getSession().setAttribute("type", "success");
        response.sendRedirect(request.getContextPath() + "/shipment?action=list");
    }

    // Ham hien thi chi tiet mot lo hang cu the
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

    // Ham hien thi form chinh sua thong tin lo hang
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

    // Ham xu ly cap nhat thong tin (tracking, status, note...)
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
            request.getSession().setAttribute("message", "Cập nhật lô hàng thành công!");
            request.getSession().setAttribute("type", "success");

            // When shipment is delivered, mark related GDN as DONE
            if ("DELIVERED".equals(newStatus) && s.getGdnId() != null) {
                dao.GoodsDeliveryNoteDAO gdnDao = new dao.GoodsDeliveryNoteDAO();
                try {
                    gdnDao.updateGDNStatus(s.getGdnId(), "DONE");
                } catch (Exception ignored) {
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/shipment?action=detail&id=" + id);
    }

    // Ham xoa lo hang (chi cho phep khi trang thai la CREATED)
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        Long id = Long.valueOf(request.getParameter("id"));
        Shipment s = shipmentDAO.getById(id);
        
        if (s != null && "CREATED".equals(s.getStatus())) {
            shipmentDAO.deleteShipment(id);
            request.getSession().setAttribute("message", "Xóa lô hàng thành công!");
            request.getSession().setAttribute("type", "success");
            response.sendRedirect(request.getContextPath() + "/shipment?action=list");
        } else {
            // Neu khong phai CREATED hoac khong ton tai, khong cho xoa va quay lai trang chi tiet
            response.sendRedirect(request.getContextPath() + "/shipment?action=detail&id=" + id + "&error=Cannot+delete+shipment+after+pickup");
        }
    }
}
