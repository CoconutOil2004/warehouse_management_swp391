package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class ToastUtil {

    private ToastUtil() {
    }

    public static void setToast(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("message", message);
        if (type != null && !type.isBlank()) {
            session.setAttribute("type", type);
        } else {
            session.removeAttribute("type");
        }
    }
}

