package util;

import jakarta.servlet.http.HttpServletRequest;
import model.User;

/**
 * Role checks from session {@code USER.roleNames} (comma-separated, same as DAO).
 * {@code PURCHASE_STAFF}: chỉ xem product (list + detail JSON); không warehouse layout / không sửa product / không variants UI.
 * {@code SALE_STAFF} / {@code SALES_STAFF}: outbound + catalog (product read-only, có variants xem); không inbound, layout, master data.
 * {@code ADMIN} hoặc {@code WAREHOUSE_MANAGER} bỏ qua các hạn chế này.
 * {@code WAREHOUSE_STAFF}: xem danh sách + chi tiết PO/SO; không tạo/sửa/xóa/import — trừ khi có
 * {@code PURCHASE_STAFF} (PO) / {@code SALE_STAFF}/{@code SALES_STAFF} (SO), {@code WAREHOUSE_MANAGER} hoặc {@code ADMIN}.
 */
public final class RoleUtil {

    private RoleUtil() {
    }

    public static String roleNames(HttpServletRequest request) {
        if (request == null || request.getSession(false) == null) {
            return "";
        }
        Object u = request.getSession(false).getAttribute("USER");
        if (u instanceof User) {
            User user = (User) u;
            String rn = user.getRoleNames();
            return rn != null ? rn : "";
        }
        return "";
    }

    public static boolean isPurchaseStaffRestricted(String roleNames) {
        if (roleNames == null || roleNames.isBlank()) {
            return false;
        }
        if (roleNames.contains("ADMIN") || roleNames.contains("WAREHOUSE_MANAGER")) {
            return false;
        }
        return roleNames.contains("PURCHASE_STAFF");
    }

    public static boolean isSaleStaffRestricted(String roleNames) {
        if (roleNames == null || roleNames.isBlank()) {
            return false;
        }
        if (roleNames.contains("ADMIN") || roleNames.contains("WAREHOUSE_MANAGER")) {
            return false;
        }
        return roleNames.contains("SALE_STAFF") || roleNames.contains("SALES_STAFF");
    }

    /**
     * Chỉ xem PO (list + POST detail); không được mutation. PURCHASE_STAFF vẫn quản lý PO đầy đủ.
     */
    public static boolean isPurchaseOrderReadOnlyForWarehouseStaff(String roleNames) {
        if (roleNames == null || roleNames.isBlank()) {
            return false;
        }
        if (roleNames.contains("ADMIN")
                || roleNames.contains("WAREHOUSE_MANAGER")
                || roleNames.contains("PURCHASE_STAFF")) {
            return false;
        }
        return roleNames.contains("WAREHOUSE_STAFF");
    }

    /**
     * Chỉ xem SO (list + GET detail); không được mutation. SALE_STAFF / SALES_STAFF vẫn quản lý SO đầy đủ.
     */
    public static boolean isSalesOrderReadOnlyForWarehouseStaff(String roleNames) {
        if (roleNames == null || roleNames.isBlank()) {
            return false;
        }
        if (roleNames.contains("ADMIN")
                || roleNames.contains("WAREHOUSE_MANAGER")
                || roleNames.contains("SALE_STAFF")
                || roleNames.contains("SALES_STAFF")) {
            return false;
        }
        return roleNames.contains("WAREHOUSE_STAFF");
    }

    /** Ẩn block Warehouse Layout trên dashboard / chặn trang layout. */
    public static boolean shouldHideWarehouseLayout(String roleNames) {
        return isPurchaseStaffRestricted(roleNames) || isSaleStaffRestricted(roleNames);
    }

    /**
     * ADMIN không dùng Inbound/Outbound (sidebar + URL). Còn dashboard, master data, products, warehouse layout…
     */
    public static boolean shouldBlockInboundOutboundForAdmin(HttpServletRequest req) {
        String rn = roleNames(req);
        if (rn == null || !rn.contains("ADMIN")) {
            return false;
        }
        String uri = normalizedPath(req);
        return pathMatches(uri, "/purchase-orders")
                || pathMatches(uri, "/goods-receipt")
                || pathMatches(uri, "/sales-orders")
                || pathMatches(uri, "/goods-delivery-note")
                || pathMatches(uri, "/pick-wave")
                || pathMatches(uri, "/pick-task")
                || pathMatches(uri, "/packing")
                || pathMatches(uri, "/shipment");
    }

    /**
     * PURCHASE_STAFF (hạn chế): chặn outbound, master data, layout, sửa product… — dùng trong {@code AuthFilter}.
     * Cho phép: dashboard, inbound (PO/GRN), profile, products (GET list hoặc {@code action=detail}).
     */
    public static boolean shouldBlockRequestForPurchaseStaff(HttpServletRequest req) {
        if (!isPurchaseStaffRestricted(roleNames(req))) {
            return false;
        }
        String uri = normalizedPath(req);

        if (pathMatches(uri, "/warehouse-layout")) {
            return true;
        }
        if (pathMatches(uri, "/sales-orders")) {
            return true;
        }
        if (pathMatches(uri, "/goods-delivery-note")) {
            return true;
        }
        if (pathMatches(uri, "/pick-wave")) {
            return true;
        }
        if (pathMatches(uri, "/pick-task")) {
            return true;
        }
        if (pathMatches(uri, "/packing")) {
            return true;
        }
        if (pathMatches(uri, "/shipment")) {
            return true;
        }
        if (pathMatches(uri, "/admin/permission")) {
            return true;
        }
        if (pathMatches(uri, "/admin/role")) {
            return true;
        }
        if (pathMatches(uri, "/admin/user")) {
            return true;
        }
        if (pathMatches(uri, "/admin/supplier")) {
            return true;
        }
        if (pathMatches(uri, "/admin/customer")) {
            return true;
        }
        if (pathMatches(uri, "/admin/warehouse")) {
            return true;
        }

        if (pathMatches(uri, "/products")) {
            if ("POST".equalsIgnoreCase(req.getMethod())) {
                return true;
            }
            String action = req.getParameter("action");
            if (action != null && !action.isBlank()) {
                String a = action.trim();
                if (a.equalsIgnoreCase("create") || a.equalsIgnoreCase("edit") || a.equalsIgnoreCase("variants")) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * {@code SALE_STAFF}: không inbound / layout / master data; được outbound đầy đủ; {@code /products} chỉ GET
     * (list, detail, variants — không create/edit/POST).
     */
    public static boolean shouldBlockRequestForSaleStaff(HttpServletRequest req) {
        if (!isSaleStaffRestricted(roleNames(req))) {
            return false;
        }
        String uri = normalizedPath(req);

        if (pathMatches(uri, "/purchase-orders")) {
            return true;
        }
        if (pathMatches(uri, "/goods-receipt")) {
            return true;
        }
        if (pathMatches(uri, "/warehouse-layout")) {
            return true;
        }
        if (pathMatches(uri, "/admin/permission")) {
            return true;
        }
        if (pathMatches(uri, "/admin/role")) {
            return true;
        }
        if (pathMatches(uri, "/admin/user")) {
            return true;
        }
        if (pathMatches(uri, "/admin/supplier")) {
            return true;
        }
        if (pathMatches(uri, "/admin/customer")) {
            return true;
        }
        if (pathMatches(uri, "/admin/warehouse")) {
            return true;
        }

        if (pathMatches(uri, "/products")) {
            if ("POST".equalsIgnoreCase(req.getMethod())) {
                return true;
            }
            String action = req.getParameter("action");
            if (action != null && !action.isBlank()) {
                String a = action.trim();
                if (a.equalsIgnoreCase("create") || a.equalsIgnoreCase("edit")) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private static String normalizedPath(HttpServletRequest req) {
        String ctx = req.getContextPath();
        if (ctx == null) {
            ctx = "";
        }
        String uri = req.getRequestURI();
        if (!ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        if (uri.isEmpty()) {
            uri = "/";
        }
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        return uri;
    }

    private static boolean pathMatches(String path, String base) {
        return path.equals(base) || path.startsWith(base + "/");
    }
}
