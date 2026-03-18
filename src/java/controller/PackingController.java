package controller;

import dao.PackingDAO;
import dao.GoodsDeliveryNoteDAO;
import dto.GDNDetailDTO;
import dto.PackingDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "PackingController", urlPatterns = { "/packing" })
public class PackingController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "list" -> handleList(request, response);
                case "form" -> handleForm(request, response);
                default -> response.sendRedirect(request.getContextPath() + "/packing?action=list");
            }
        } catch (Exception e) {
            Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
            throw new ServletException(e);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String status = request.getParameter("status");
        PackingDAO packingDao = new PackingDAO();
        List<PackingDTO> list = packingDao.listByStatus(status);
        request.setAttribute("packings", list);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-list.jsp").forward(request, response);
    }

    private void handleForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
        if (gdn == null) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        PackingDAO packingDao = new PackingDAO();
        PackingDTO packing = packingDao.getByGdnId(gdnId);
        if (packing == null) {
            Long packId = packingDao.createPackingForGDN(gdnId);
            packing = packingDao.getByPackId(packId);
        }

        request.setAttribute("gdn", gdn);
        request.setAttribute("packing", packing);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if ("save".equals(action)) {
            try {
                handleSave(request, response);
            } catch (Exception e) {
                Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
                throw new ServletException(e);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long packId = parseLong(request.getParameter("packId"), -1);
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        String packageLabel = request.getParameter("packageLabel");
        if (packageLabel != null) packageLabel = packageLabel.trim();

        if (packId <= 0 || gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long packedBy = user != null ? user.getUserId() : null;

        PackingDAO packingDao = new PackingDAO();
        packingDao.updatePacking(packId, "DONE", packedBy, packageLabel);

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
        if (gdn != null && gdn.getLines() != null) {
            Map<Long, java.math.BigDecimal> lineQtyPacked = new HashMap<>();
            for (dto.GDNLineDTO line : gdn.getLines()) {
                lineQtyPacked.put(line.getGdnLineId(), line.getQtyPicked() != null ? line.getQtyPicked() : java.math.BigDecimal.ZERO);
            }
            packingDao.updateGDNLinesPacked(gdnId, lineQtyPacked);
        }

        // When packing is done, move GDN to SHIPPING (shipment creation can start).
        try {
            gdnDao.updateGDNStatus(gdnId, "SHIPPING");
        } catch (Exception ignored) {
        }

        request.getSession().setAttribute("message", "Packing completed.");
        response.sendRedirect(request.getContextPath() + "/packing?action=form&gdnId=" + gdnId);
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
