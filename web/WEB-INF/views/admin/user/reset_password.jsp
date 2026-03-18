<%-- Document : reset_password Created on : Jan 24, 2026, 8:27:55 PM Author : chaua --%>
    <%@page contentType="text/html" pageEncoding="UTF-8" %>
        <%@ taglib prefix="c" uri="jakarta.tags.core" %>

            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đặt lại mật khẩu | Fashion WMS</title>

                <link href="<%=request.getContextPath()%>/assets/vendor/fontawesome-free/css/all.min.css"
                    rel="stylesheet" type="text/css">
                <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap"
                    rel="stylesheet">

                <style>
                    :root {
                        --primary: #4e73df;
                        --primary-light: #7091f1;
                        --secondary: #224abe;
                        --accent: #36b9cc;
                        --error: #e74a3b;
                        --success: #1cc88a;
                        --glass-bg: rgba(15, 23, 42, 0.7);
                        --glass-border: rgba(255, 255, 255, 0.1);
                        --text-main: #ffffff;
                        --text-dim: rgba(255, 255, 255, 0.6);
                    }

                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                        font-family: 'Outfit', sans-serif;
                    }

                    body {
                        height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        overflow: hidden;
                        background: #0f172a;
                        color: var(--text-main);
                    }

                    .background-container {
                        position: fixed;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        z-index: -1;
                    }

                    .bg-image {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                        filter: brightness(0.4) blur(4px);
                        transform: scale(1.1);
                    }

                    .bg-overlay {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        background: radial-gradient(circle, transparent, rgba(15, 23, 42, 0.8));
                    }

                    .login-container {
                        width: 100%;
                        max-width: 450px;
                        padding: 20px;
                        z-index: 10;
                        animation: fadeIn 0.8s ease;
                    }

                    .login-card {
                        background: var(--glass-bg);
                        backdrop-filter: blur(25px);
                        -webkit-backdrop-filter: blur(25px);
                        border: 1px solid var(--glass-border);
                        border-radius: 32px;
                        padding: 50px 45px;
                        box-shadow: 0 40px 100px rgba(0, 0, 0, 0.5);
                    }

                    .header {
                        text-align: center;
                        margin-bottom: 35px;
                    }

                    .header .logo-area {
                        width: 60px;
                        height: 60px;
                        background: linear-gradient(135deg, var(--primary), var(--secondary));
                        border-radius: 16px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 20px;
                    }

                    .header h1 {
                        font-size: 26px;
                        font-weight: 700;
                        color: #fff;
                        margin-bottom: 8px;
                    }

                    .header p {
                        color: var(--text-dim);
                        font-size: 14px;
                    }

                    .form-group {
                        margin-bottom: 20px;
                    }

                    .form-group label {
                        display: block;
                        color: var(--text-dim);
                        font-size: 13px;
                        margin-bottom: 8px;
                        margin-left: 5px;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }

                    .input-box {
                        background: rgba(255, 255, 255, 0.04);
                        border: 1.5px solid rgba(255, 255, 255, 0.1);
                        border-radius: 16px;
                        height: 60px;
                        display: flex;
                        align-items: center;
                        padding: 0 20px;
                        transition: 0.3s;
                    }

                    .input-box:focus-within {
                        border-color: var(--primary-light);
                        background: rgba(255, 255, 255, 0.08);
                    }

                    .input-box i {
                        color: var(--text-dim);
                        margin-right: 15px;
                        font-size: 18px;
                    }

                    .input-box input {
                        flex: 1;
                        background: transparent;
                        border: none;
                        outline: none;
                        color: #fff;
                        font-size: 16px;
                    }

                    .btn-action {
                        width: 100%;
                        height: 60px;
                        background: linear-gradient(135deg, var(--primary), var(--secondary));
                        border: none;
                        border-radius: 16px;
                        color: white;
                        font-size: 17px;
                        font-weight: 600;
                        cursor: pointer;
                        transition: 0.3s;
                        margin-top: 15px;
                        box-shadow: 0 10px 20px rgba(78, 115, 223, 0.3);
                    }

                    .btn-action:hover {
                        transform: translateY(-3px);
                        filter: brightness(1.1);
                    }

                    .alert-error {
                        background: rgba(255, 118, 117, 0.15);
                        border: 1px solid rgba(255, 118, 117, 0.2);
                        color: #ffb8b8;
                        padding: 15px;
                        border-radius: 16px;
                        margin-bottom: 20px;
                        font-size: 14px;
                    }

                    .alert-success {
                        background: rgba(85, 239, 196, 0.15);
                        border: 1px solid rgba(85, 239, 196, 0.2);
                        color: #b8fff4;
                        padding: 15px;
                        border-radius: 16px;
                        margin-bottom: 20px;
                        font-size: 14px;
                    }

                    .footer-links {
                        text-align: center;
                        margin-top: 25px;
                    }

                    .footer-links a {
                        color: var(--text-dim);
                        text-decoration: none;
                        font-size: 14px;
                        transition: 0.3s;
                    }

                    .footer-links a:hover {
                        color: var(--primary-light);
                    }

                    @keyframes fadeIn {
                        from {
                            opacity: 0;
                            transform: translateY(20px);
                        }

                        to {
                            opacity: 1;
                            transform: translateY(0);
                        }
                    }
                </style>
            </head>

            <body>
                <div class="background-container">
                    <img src="<%=request.getContextPath()%>/assets/img/clothing-bg.png" alt="Background"
                        class="bg-image">
                    <div class="bg-overlay"></div>
                </div>

                <div class="login-container">
                    <div class="login-card">
                        <div class="header">
                            <div class="logo-area"><i class="fas fa-key" style="color:white; font-size:24px;"></i></div>
                            <h1>Reset Password</h1>
                            <p>Create new password</p>
                        </div>

                        <c:if test="${not empty error}">
                            <div class="alert-error"><i class="fas fa-exclamation-circle"></i> ${error}</div>
                        </c:if>
                        <c:if test="${not empty message}">
                            <div class="alert-success"><i class="fas fa-check-circle"></i> ${message}</div>
                        </c:if>

                        <form action="<%=request.getContextPath()%>/authen" method="post">
                            <input type="hidden" name="action" value="reset" />

                            <div class="form-group">
                                <label>New Password</label>
                                <div class="input-box">
                                    <i class="fas fa-lock"></i>
                                    <input type="password" name="newPassword" required placeholder="••••••••">
                                </div>
                            </div>

                            <div class="form-group">
                                <label>Confirm Password</label>
                                <div class="input-box">
                                    <i class="fas fa-check-double"></i>
                                    <input type="password" name="confirmPassword" required placeholder="••••••••">
                                </div>
                            </div>

                            <button type="submit" class="btn-action">Update Password</button>
                        </form>

                        <div class="footer-links">
                            <a href="<%=request.getContextPath()%>/authen">Back to login</a>
                        </div>
                    </div>
                </div>
            </body>

            </html>