package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import model.User;
import util.PasswordUtil;
import util.SendEmail;
import util.ViewPath;

@WebServlet(name = "AuthenticationController", urlPatterns = {"/authen"})
public class AuthenticationController extends HttpServlet {

    /**
     * Bật = true: khi dev, đăng nhập xong không cần OTP, vào thẳng dashboard.
     * Tắt = false: bật lại OTP khi deploy / làm xong.
     */
    private static final boolean SKIP_OTP_DEV_MODE = true;

    private String generateOTP() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    private static final String SESSION_USER_KEY = "USER";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // đọc action từ query param
        String action = trimOrEmpty(request.getParameter("action"));

        // logout
        if ("logout".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + ViewPath.VIEW_LOGIN);
            return;

        }

        // show forgot page
        if ("forgot".equalsIgnoreCase(action)) {
            request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
            return;
        }

        // show reset page
        if ("reset".equalsIgnoreCase(action)) {
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
            return;
        }

        // default show login
        request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = trimOrEmpty(request.getParameter("action"));

        if ("forgot".equalsIgnoreCase(action)) {
            handleForgot(request, response);
            return;
        }

        if ("verify-otp".equalsIgnoreCase(action)) {
            handleVerifyOtp(request, response);
            return;
        }

        if ("reset".equalsIgnoreCase(action)) {
            handleReset(request, response);
            return;
        }

        handleLogin(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // lấy input
        String identity = trimOrEmpty(request.getParameter("mail"));
        String password = trimOrEmpty(request.getParameter("password"));

        // validate khong de trong
        if (identity.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ email và password");
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
            return;
        }

        // login
        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(identity, password);

        if (user == null) {
            request.setAttribute("error", "Sai email hoặc password");
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(60 * 60 * 4);

        // Chế độ dev: bỏ qua OTP, đăng nhập thẳng
        if (SKIP_OTP_DEV_MODE) {
            user.setPasswordHash(null);
            session.setAttribute(SESSION_USER_KEY, user);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // success - bắt buộc OTP
        try {
            String otp = generateOTP();
            SendEmail.sendOTP(user.getEmail(), otp);
            request.setAttribute("devOtp", otp);

            session.setAttribute("USER", user);
            session.setAttribute("AUTH_TYPE", "LOGIN");
            session.setAttribute("PRE_LOGIN_USER_ID", user.getUserId());
            session.setAttribute("RESET_EMAIL", user.getEmail());
            session.setAttribute("CURRENT_OTP", otp);
            session.setAttribute("OTP_CREATION_TIME", System.currentTimeMillis());

            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);

        } catch (ServletException | IOException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tạo OTP: " + e.getMessage());
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
        }
    }

    private String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private void handleForgot(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = trimOrEmpty(request.getParameter("email"));

        // validate
        if (email.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email");
            request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();
        try {
            // Check email exists
            User user = dao.findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "Email không tồn tại trong hệ thống");
                request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
                return;
            }

            // Tạo OTP (Session based)
            String otp = generateOTP();

            // Gửi OTP qua email
            SendEmail.sendOTP(email, otp);

            // DEV MODE
            request.setAttribute("devOtp", otp);

            // lưu email vào session
            HttpSession session = request.getSession(true);
            session.setAttribute("RESET_EMAIL", email);
            session.setAttribute("AUTH_TYPE", "RESET"); // Đánh dấu là flow Reset Password
            session.setAttribute("CURRENT_OTP", otp);
            session.setAttribute("OTP_CREATION_TIME", System.currentTimeMillis());

            // chuyển sang trang verify otp
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
        } catch (ServletException | IOException | SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
            request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
        }
    }

    private void handleVerifyOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String otp = trimOrEmpty(request.getParameter("otp"));

        if (otp.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập OTP");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
            return;
        }

        // Validate Session OTP
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + ViewPath.VIEW_LOGIN);
            return;
        }

        String sessionOtp = (String) session.getAttribute("CURRENT_OTP");
        Long creationTime = (Long) session.getAttribute("OTP_CREATION_TIME");

        if (sessionOtp == null || creationTime == null) {
            request.setAttribute("error", "Yêu cầu hết hạn. Vui lòng thử lại.");
            if ("RESET".equals(session.getAttribute("AUTH_TYPE"))) {
                request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
            } else {
                request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
            }
            return;
        }

        // Check Expiry (5 minutes)
        if (System.currentTimeMillis() - creationTime > 5 * 60 * 1000) {
            session.removeAttribute("CURRENT_OTP");
            session.removeAttribute("OTP_CREATION_TIME");
            request.setAttribute("error", "OTP đã hết hạn. Vui lòng lấy mã mới.");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
            return;
        }

        // Check Match
        if (!sessionOtp.equals(otp)) {
            request.setAttribute("error", "OTP không chính xác");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
            return;
        }

        // OTP Valid
        session.removeAttribute("CURRENT_OTP"); // Clear OTP to prevent replay

        String authType = (String) session.getAttribute("AUTH_TYPE");

        // CASE 1: LOGIN
        if ("LOGIN".equals(authType)) {
            Long preUserId = (Long) session.getAttribute("PRE_LOGIN_USER_ID");

            UserDAO dao = new UserDAO();
            try {
                User user = dao.getById(preUserId);
                if (user != null) {
                    user.setPasswordHash(null);
                    session.setAttribute(SESSION_USER_KEY, user);

                    // Cleanup session
                    session.removeAttribute("AUTH_TYPE");
                    session.removeAttribute("PRE_LOGIN_USER_ID");
                    session.removeAttribute("RESET_EMAIL");
                    session.removeAttribute("OTP_CREATION_TIME");

                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // CASE 2: RESET PASSWORD (default)
        // lưu vào session để sang bước reset
        session.setAttribute("VERIFIED_OTP", "TRUE"); // Mark as verified for next step

        // Chuyển sang trang reset password
        request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
    }

    private void handleReset(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Không lấy OTP từ param nữa, mà lấy từ session
        HttpSession session = request.getSession(false);
        String otp = (session != null) ? (String) session.getAttribute("VERIFIED_OTP") : null;

        if (otp == null) {
            // Chưa verify OTP mà nhảy vào đây -> đá về forgot
            response.sendRedirect(request.getContextPath() + "/authen?action=forgot");
            return;
        }

        String newPassword = trimOrEmpty(request.getParameter("newPassword"));
        String confirmPassword = trimOrEmpty(request.getParameter("confirmPassword"));

        // validate input
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập mật khẩu mới");
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp");
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();
        try {
            // Get Email from session
            String email = (String) session.getAttribute("RESET_EMAIL");
            if (email == null) {
                request.getRequestDispatcher(request.getContextPath() + ViewPath.VIEW_LOGIN).forward(request, response);
                return;
            }

            User user = dao.findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "User không tìm thấy");
                request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
                return;
            }

            Long userId = user.getUserId();

            // hash password mới (BCrypt)
            String newHash = PasswordUtil.hashPassword(newPassword);

            boolean updated = dao.updatePasswordHash(userId, newHash);
            if (!updated) {
                request.setAttribute("error", "Không thể cập nhật mật khẩu");
                request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
                return;
            }

            // xóa session reset
            if (session != null) {
                session.removeAttribute("RESET_EMAIL");
                session.removeAttribute("VERIFIED_OTP"); // clear otp
                session.removeAttribute("OTP_CREATION_TIME");
            }

            // thành công → quay về login
            request.setAttribute("message",
                    "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
        }
    }
}
