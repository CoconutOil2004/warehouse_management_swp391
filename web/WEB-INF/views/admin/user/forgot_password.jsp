<%-- Document : forgot_password Created on : Jan 24, 2026, 8:27:05 PM Author : chaua --%>
    <%@page contentType="text/html" pageEncoding="UTF-8" %>
        <%@ taglib prefix="c" uri="jakarta.tags.core" %>

            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quên mật khẩu | Fashion WMS</title>

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
                        animation: moveBg 40s infinite alternate ease-in-out;
                    }

                    @keyframes moveBg {
                        0% {
                            transform: scale(1.1);
                        }

                        100% {
                            transform: scale(1.15) translate(-1%, -1%);
                        }
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
                        margin-bottom: 40px;
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
                        margin-bottom: 24px;
                        position: relative;
                    }

                    .input-field {
                        display: flex;
                        align-items: center;
                        background: rgba(255, 255, 255, 0.04);
                        border: 1.5px solid rgba(255, 255, 255, 0.1);
                        border-radius: 16px;
                        padding: 0 20px;
                        height: 60px;
                        transition: 0.3s;
                    }

                    .input-field:focus-within {
                        border-color: var(--primary-light);
                        background: rgba(255, 255, 255, 0.08);
                    }

                    .input-field i {
                        color: var(--text-dim);
                        margin-right: 15px;
                        font-size: 18px;
                    }

                    .input-field input {
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
                        box-shadow: 0 10px 20px rgba(78, 115, 223, 0.3);
                    }

                    .btn-action:hover {
                        transform: translateY(-3px);
                        filter: brightness(1.1);
                    }

                    .footer-links {
                        text-align: center;
                        margin-top: 30px;
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

                    .alert {
                        padding: 16px;
                        border-radius: 16px;
                        margin-bottom: 24px;
                        font-size: 14px;
                        display: flex;
                        align-items: center;
                        gap: 10px;
                    }

                    .alert-error {
                        background: rgba(255, 118, 117, 0.15);
                        border: 1px solid rgba(255, 118, 117, 0.2);
                        color: #ffb8b8;
                    }

                    .alert-success {
                        background: rgba(85, 239, 196, 0.15);
                        border: 1px solid rgba(85, 239, 196, 0.2);
                        color: #b8fff4;
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
                            <div class="logo-area"><i class="fas fa-lock-open" style="color:white; font-size:24px;"></i>
                            </div>
                            <h1>Forgot Password?</h1>
                            
                        </div>

                        <c:if test="${not empty error}">
                            <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> ${error}</div>
                        </c:if>
                        <c:if test="${not empty message}">
                            <div class="alert alert-success"><i class="fas fa-check-circle"></i> ${message}</div>
                        </c:if>

                        <form action="<%=request.getContextPath()%>/authen" method="post" novalidate>
                            <input type="hidden" name="action" value="forgot" />
                            <div class="form-group">
                                <div class="input-field">
                                    <i class="fas fa-envelope"></i>
                                    <input type="email" name="email" id="email" placeholder="Enter your email address"
                                        value="${email}"
                                        pattern="[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}"
                                        title="Valid email format: name@domain.com"
                                        required>
                                </div>
                            </div>
                            <button type="submit" class="btn-action">Send OTP</button>
                        </form>

                        <div class="footer-links">
                            <a href="<%=request.getContextPath()%>/authen"><i class="fas fa-chevron-left"
                                    style="font-size:12px; margin-right:5px;"></i> Back to login </a>
                        </div>
                    </div>
                </div>
            </body>

            </html>