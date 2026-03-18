package controller;

import dao.PackingDAO;
import dao.GoodsDeliveryNoteDAO;
import dto.GDNDetailDTO;
import dto.GDNLineDTO;
import dto.PackingDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import java.io.IOException;
import java.math.BigDecimal;
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
                case "station" -> handleStation(request, response);
                case "packLine" -> handlePackLine(request, response);
                case "ready" -> handleReadyList(request, response);
                case "start" -> handleStart(request, response);
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
        request.setAttribute("isReadyView", false);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-list.jsp").forward(request, response);
    }

    private void handleStart(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=ready");
            return;
        }

        PackingDAO packingDao = new PackingDAO();
        PackingDTO existing = packingDao.getByGdnId(gdnId);
        
        if (existing != null) {
            response.sendRedirect(request.getContextPath() + "/packing?action=form&gdnId=" + gdnId);
            return;
        }

        Long packId = packingDao.createPackingForGDN(gdnId);
        
        request.getSession().setAttribute("message", "Packing record created successfully!");
        response.sendRedirect(request.getContextPath() + "/packing?action=form&gdnId=" + gdnId);
    }

    private void handleReadyList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PackingDAO packingDao = new PackingDAO();
        List<PackingDTO> list = packingDao.listGDNsReadyForPacking();
        request.setAttribute("packings", list);
        request.setAttribute("isReadyView", true);
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

        if ("PICKING".equals(gdn.getStatus())) {
            gdnDao.updateGDNStatus(gdnId, "PACKING");
            gdn = gdnDao.getGDNDetailById(gdnId);
        }

        List<GDNLineDTO> packingLines = packingDao.getPackingLines(gdnId);

        request.setAttribute("gdn", gdn);
        request.setAttribute("packing", packing);
        request.setAttribute("packingLines", packingLines);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-form.jsp").forward(request, response);
    }

    private void handleStation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        if (gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=ready");
            return;
        }

        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();
        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
        if (gdn == null) {
            response.sendRedirect(request.getContextPath() + "/packing?action=ready");
            return;
        }

        PackingDAO packingDao = new PackingDAO();
        PackingDTO packing = packingDao.getByGdnId(gdnId);

        if ("PICKING".equals(gdn.getStatus())) {
            gdnDao.updateGDNStatus(gdnId, "PACKING");
            gdn = gdnDao.getGDNDetailById(gdnId);
        }

        List<GDNLineDTO> packingLines = packingDao.getPackingLines(gdnId);

        request.setAttribute("gdn", gdn);
        request.setAttribute("packing", packing);
        request.setAttribute("packingLines", packingLines);
        request.getRequestDispatcher("/WEB-INF/views/outbound/packing-station.jsp").forward(request, response);
    }

    private void handlePackLine(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long gdnLineId = parseLong(request.getParameter("gdnLineId"), -1);
        BigDecimal qtyPacked = parseBigDecimal(request.getParameter("qtyPacked"), null);

        if (gdnLineId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=ready");
            return;
        }

        long gdnId = parseLong(request.getParameter("gdnId"), -1);

        PackingDAO packingDao = new PackingDAO();
        if (qtyPacked != null && qtyPacked.signum() >= 0) {
            packingDao.updateLinePackedQty(gdnLineId, qtyPacked);
        }

        response.sendRedirect(request.getContextPath() + "/packing?action=station&gdnId=" + gdnId);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        switch (action != null ? action : "") {
            case "save" -> {
                try {
                    handleSave(request, response);
                } catch (Exception e) {
                    Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
                    throw new ServletException(e);
                }
            }
            case "saveStation" -> {
                try {
                    handleSaveStation(request, response);
                } catch (Exception e) {
                    Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
                    throw new ServletException(e);
                }
            }
            case "complete" -> {
                try {
                    handleComplete(request, response);
                } catch (Exception e) {
                    Logger.getLogger(PackingController.class.getName()).log(Level.SEVERE, null, e);
                    throw new ServletException(e);
                }
            }
            default -> response.sendRedirect(request.getContextPath() + "/packing?action=list");
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long packId = parseLong(request.getParameter("packId"), -1);
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        String packageLabel = request.getParameter("packageLabel");
        String packageType = request.getParameter("packageType");
        String weightStr = request.getParameter("weight");
        String weightUnit = request.getParameter("weightUnit");
        String notes = request.getParameter("notes");

        if (packageLabel != null) packageLabel = packageLabel.trim();
        if (packageType != null) packageType = packageType.trim();
        if (notes != null) notes = notes.trim();

        BigDecimal weight = parseBigDecimal(weightStr, null);

        if (packId <= 0 || gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long packedBy = user != null ? user.getUserId() : null;

        PackingDAO packingDao = new PackingDAO();
        packingDao.updatePacking(packId, "PENDING", packedBy, packageLabel, packageType, weight, weightUnit, notes, null, null);

        response.sendRedirect(request.getContextPath() + "/packing?action=form&gdnId=" + gdnId);
    }

    private void handleSaveStation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long packId = parseLong(request.getParameter("packId"), -1);
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        String packageLabel = request.getParameter("packageLabel");
        String packageType = request.getParameter("packageType");
        String weightStr = request.getParameter("weight");
        String weightUnit = request.getParameter("weightUnit");
        String notes = request.getParameter("notes");
        String totalPackagesStr = request.getParameter("totalPackages");
        String currentPackageStr = request.getParameter("currentPackage");

        if (packageLabel != null) packageLabel = packageLabel.trim();
        if (packageType != null) packageType = packageType.trim();
        if (notes != null) notes = notes.trim();

        BigDecimal weight = parseBigDecimal(weightStr, null);
        Integer totalPackages = parseInt(totalPackagesStr, 1);
        Integer currentPackage = parseInt(currentPackageStr, 1);

        if (packId <= 0 || gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=ready");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long packedBy = user != null ? user.getUserId() : null;

        PackingDAO packingDao = new PackingDAO();
        packingDao.updatePacking(packId, "IN_PROGRESS", packedBy, packageLabel, packageType, weight, weightUnit, notes, totalPackages, currentPackage);

        response.sendRedirect(request.getContextPath() + "/packing?action=station&gdnId=" + gdnId);
    }

    private void handleComplete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long packId = parseLong(request.getParameter("packId"), -1);
        long gdnId = parseLong(request.getParameter("gdnId"), -1);
        String packageLabel = request.getParameter("packageLabel");
        String packageType = request.getParameter("packageType");
        String weightStr = request.getParameter("weight");
        String weightUnit = request.getParameter("weightUnit");
        String notes = request.getParameter("notes");

        if (packageLabel != null) packageLabel = packageLabel.trim();
        if (packageType != null) packageType = packageType.trim();
        if (notes != null) notes = notes.trim();

        BigDecimal weight = parseBigDecimal(weightStr, null);

        if (packId <= 0 || gdnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/packing?action=list");
            return;
        }

        User user = (User) request.getSession().getAttribute("USER");
        Long packedBy = user != null ? user.getUserId() : null;

        PackingDAO packingDao = new PackingDAO();
        GoodsDeliveryNoteDAO gdnDao = new GoodsDeliveryNoteDAO();

        packingDao.updatePacking(packId, "DONE", packedBy, packageLabel, packageType, weight, weightUnit, notes, null, null);

        GDNDetailDTO gdn = gdnDao.getGDNDetailById(gdnId);
        if (gdn != null && gdn.getLines() != null) {
            Map<Long, BigDecimal> lineQtyPacked = new HashMap<>();
            for (GDNLineDTO line : gdn.getLines()) {
                lineQtyPacked.put(line.getGdnLineId(), line.getQtyPicked() != null ? line.getQtyPicked() : BigDecimal.ZERO);
            }
            packingDao.updateGDNLinesPacked(gdnId, lineQtyPacked);
        }

        gdnDao.updateGDNStatus(gdnId, "CONFIRMED");
        gdnDao.deductInventoryOnConfirm(gdnId);

        response.sendRedirect(request.getContextPath() + "/packing?action=list&message=Packing+completed+successfully");
    }

    private long parseLong(String raw, long def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Long.parseLong(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal parseBigDecimal(String raw, BigDecimal def) {
        if (raw == null || raw.isBlank()) return def;
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private int parseInt(String raw, int def) {
        try {
            return (raw == null || raw.isBlank()) ? def : Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
