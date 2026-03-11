package controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final String SESSION_USER_KEY = "USER";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        // 1) Bypass authentication endpoints (login/forgot/reset... nếu đều đi qua /authen)
        boolean isAuthEndpoint = uri.startsWith(ctx + "/authen");

        // 2) Static assets bypass
        boolean isStatic = uri.startsWith(ctx + "/assets/")
                || uri.endsWith(".css") || uri.endsWith(".js")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".svg") || uri.endsWith(".woff") || uri.endsWith(".woff2")
                || uri.endsWith(".ico");

        if (isAuthEndpoint || isStatic) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ 3) Prevent browser back-cache for protected pages
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        res.setHeader("Pragma", "no-cache"); // HTTP 1.0
        res.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        Object user = (session == null) ? null : session.getAttribute(SESSION_USER_KEY);

        // Tạm thời comment để vào app không cần đăng nhập
        // if (user == null) {
        //     res.sendRedirect(ctx + "/authen");
        //     return;
        // }

        chain.doFilter(request, response);
    }
}
