package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * Đọc user đăng nhập từ session sau khi {@code AuthenticationController} set attribute.
 * Dùng chung toàn app thay vì lặp {@code getSession().getAttribute("USER")} / {@code userId}.
 */
public final class CurrentUserUtil {

    /** Phải trùng với giá trị lưu sau login trong AuthenticationController. */
    public static final String SESSION_USER_KEY = "USER";

    private CurrentUserUtil() {
    }

    public static User getSessionUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute(SESSION_USER_KEY);
        return u instanceof User ? (User) u : null;
    }

    /**
     * {@link User#getUserId()} từ session; hỗ trợ thêm legacy attribute {@code userId} (Long/Integer) nếu có.
     */
    public static Long getUserId(HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user != null && user.getUserId() != null) {
            return user.getUserId();
        }
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session == null) {
            return null;
        }
        Object rawId = session.getAttribute("userId");
        if (rawId instanceof Long) {
            return (Long) rawId;
        }
        if (rawId instanceof Integer) {
            return ((Integer) rawId).longValue();
        }
        return null;
    }
}
