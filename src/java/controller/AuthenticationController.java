package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import model.User;
import util.PasswordUtil;
import util.SendEmail;
import util.ViewPath;

@WebServlet(name = "AuthenticationController", urlPatterns = {"/authen"})
public class AuthenticationController extends HttpServlet {

    private static final String SESSION_USER_KEY = "USER";
    private static final boolean IS_OTP_ENABLED = false;

    /** Định dạng email cơ bản (local@domain.tld) */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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

        // show reset page (only if OTP was verified)
        if ("reset".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("VERIFIED_OTP") == null) {
                response.sendRedirect(request.getContextPath() + "/authen?action=forgot");
                return;
            }
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

        if ("resend-otp".equalsIgnoreCase(action)) {
            handleResendOtp(request, response);
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
            request.setAttribute("error", "Please enter both email and password");
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
            return;
        }

        // login
        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(identity, password);

        if (user == null) {
            request.setAttribute("error", "Invalid email or password");
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
            return;
        }

        // success
        try {
            // Nếu tắt OTP (để test nhanh)
            if (!IS_OTP_ENABLED) {
                user.setPasswordHash(null);
                request.getSession().setAttribute(SESSION_USER_KEY, user);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }

            // Tạo OTP lưu vào DB (thay vì session)
            String otp = userDAO.createOtpForUser(user.getUserId());

            // Gửi OTP qua email
            SendEmail.sendOTP(user.getEmail(), otp);

            // KHÔNG set USER vào session ở đây (fix BUG-1 - OTP bypass)
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(60 * 60 * 4);
            session.setAttribute("AUTH_TYPE", "LOGIN");
            session.setAttribute("PRE_LOGIN_USER_ID", user.getUserId());
            session.setAttribute("RESET_EMAIL", user.getEmail());

            // Chuyển sang trang verify otp
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to generate OTP " + e.getMessage());
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
            request.setAttribute("error", "Please enter your email");
            request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
            return;
        }

        if (!isValidEmailFormat(email)) {
            request.setAttribute("error", "Invalid email format. Example: name@example.com");
            request.setAttribute("email", email);
            request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();
        try {
            // Tạo OTP lưu DB + trả OTP raw để gửi email
            String otp = dao.createPasswordResetOtpByEmail(email);
            if (otp == null) {
                request.setAttribute("error", "Email does not exist in the system");
                request.setAttribute("email", email);
                request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
                return;
            }

            SendEmail.sendOTP(email, otp);

            // lưu email vào session
            HttpSession session = request.getSession(true);
            session.setAttribute("RESET_EMAIL", email);
            session.setAttribute("AUTH_TYPE", "RESET");

            // chuyển sang trang verify otp
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred. Please try again.");
            request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
        }
    }

    /**
     * Gửi lại OTP, giữ người dùng ở màn verify (không redirect về forgot).
     * Dựa trên session: RESET (quên mật khẩu) hoặc LOGIN (OTP sau đăng nhập).
     */
    private void handleResendOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/authen?action=forgot");
            return;
        }

        String authType = (String) session.getAttribute("AUTH_TYPE");
        String resetEmail = (String) session.getAttribute("RESET_EMAIL");
        Long preLoginUserId = toLong(session.getAttribute("PRE_LOGIN_USER_ID"));

        UserDAO dao = new UserDAO();
        try {
            if ("RESET".equals(authType) && resetEmail != null && !resetEmail.isBlank()) {
                if (!isValidEmailFormat(resetEmail)) {
                    request.setAttribute("error", "Invalid session email. Please start forgot password again.");
                    request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
                    return;
                }
                String otp = dao.createPasswordResetOtpByEmail(resetEmail);
                if (otp == null) {
                    request.setAttribute("error", "Could not resend OTP. Please try forgot password again.");
                    request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
                    return;
                }
                SendEmail.sendOTP(resetEmail, otp);
                request.setAttribute("message", "A new OTP has been sent to your email.");
                request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
                return;
            }

            if ("LOGIN".equals(authType) && preLoginUserId != null && resetEmail != null && !resetEmail.isBlank()) {
                String otp = dao.createOtpForUser(preLoginUserId);
                SendEmail.sendOTP(resetEmail, otp);
                request.setAttribute("message", "A new OTP has been sent to your email.");
                request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/authen?action=forgot");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Could not resend OTP. Please try again.");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
        }
    }

    private static Long toLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isValidEmailFormat(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private void handleVerifyOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String otp = trimOrEmpty(request.getParameter("otp"));

        if (otp.isEmpty()) {
            request.setAttribute("error", "Please enter OTP");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
            return;
        }

        // Validate Session
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + ViewPath.VIEW_LOGIN);
            return;
        }

        UserDAO dao = new UserDAO();
        try {
            Long verifiedUserId = dao.verifyResetOtpAndGetUserId(otp);

            if (verifiedUserId == null) {
                request.setAttribute("error", "OTP is invalid or expired");
                request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
                return;
            }

            // OTP hợp lệ -> đánh dấu đã sử dụng
            dao.markResetOtpUsed(otp);

            String authType = (String) session.getAttribute("AUTH_TYPE");

            // CASE 1: LOGIN
            if ("LOGIN".equals(authType)) {
                User user = dao.getById(verifiedUserId);
                if (user != null) {
                    user.setPasswordHash(null);
                    session.setAttribute(SESSION_USER_KEY, user);

                    // Cleanup session
                    session.removeAttribute("AUTH_TYPE");
                    session.removeAttribute("PRE_LOGIN_USER_ID");
                    session.removeAttribute("RESET_EMAIL");

                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                    return;
                }
            }

            // CASE 2: RESET PASSWORD
            session.setAttribute("VERIFIED_OTP", "TRUE");
            session.setAttribute("VERIFIED_USER_ID", verifiedUserId);

            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred. Please try again.");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
        }
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
            request.setAttribute("error", "Please enter a new password");
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Password confirmation does not match");
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
            return;
        }

        UserDAO dao = new UserDAO();
        try {
            // Get Email from session
            String email = (String) session.getAttribute("RESET_EMAIL");
            if (email == null) {
                request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);
                return;
            }

            User user = dao.findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "User not found");
                request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
                return;
            }

            Long userId = user.getUserId();

            // hash password mới (BCrypt)
            String newHash = PasswordUtil.hashPassword(newPassword);

            boolean updated = dao.updatePasswordHash(userId, newHash);
            if (!updated) {
                request.setAttribute("error", "Failed to update password");
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
                    "Password changed successfully. Please log in again");
            request.getRequestDispatcher(ViewPath.VIEW_LOGIN).forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred. Please try again");
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);
        }
    }
}
