package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import util.PasswordUtil;
import util.SendEmail;
import util.ViewPath;

@WebServlet(name = "AuthenticationController", urlPatterns = {"/authen"})
public class AuthenticationController extends HttpServlet {

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

        UserDAO dao = new UserDAO();
        try {
            // Check email exists
            User user = dao.findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "Email does not exist in the system");
                request.getRequestDispatcher(ViewPath.VIEW_FORGOT).forward(request, response);
                return;
            }

            // Gửi OTP qua email
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

        String sessionOtp = (String) session.getAttribute("CURRENT_OTP");
        Long creationTime = (Long) session.getAttribute("OTP_CREATION_TIME");

        if (sessionOtp == null || creationTime == null) {
            request.setAttribute("error", "Request expired. Please try again");
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
            request.setAttribute("error", "OTP has expired. Please request a new one");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
            return;
        }

        // Check Match
        if (!sessionOtp.equals(otp)) {
            request.setAttribute("error", "Invalid OTP");
            request.getRequestDispatcher(ViewPath.VIEW_VERIFY_OTP).forward(request, response);
            return;
        }

        // OTP Valid
        session.removeAttribute("CURRENT_OTP"); // Clear OTP to prevent replay

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

            // Chuyển sang trang reset password
            request.getRequestDispatcher(ViewPath.VIEW_RESET).forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
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
