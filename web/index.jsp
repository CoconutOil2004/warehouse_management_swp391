<%-- Document : index.jsp (Login) Created on : Jan 18, 2026 Author : chaua --%>
    <%@ page contentType="text/html; charset=UTF-8" %>
        <%@ taglib prefix="c" uri="jakarta.tags.core" %>

            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đăng nhập</title>

                <!-- Fonts & Icons -->
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

                    /* Animated Background Layer */
                    .background-container {
                        position: fixed;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        z-index: -1;
                        overflow: hidden;
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
                            transform: scale(1.1) translate(0, 0);
                        }

                        100% {
                            transform: scale(1.2) translate(-2%, -2%);
                        }
                    }

                    .bg-overlay {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        background: radial-gradient(circle at center, transparent 0%, rgba(15, 23, 42, 0.8) 100%);
                    }

                    /* Abstract Glows */
                    .glow {
                        position: absolute;
                        border-radius: 50%;
                        filter: blur(80px);
                        opacity: 0.3;
                        z-index: -1;
                        animation: pulse 10s infinite alternate;
                    }

                    .glow-1 {
                        width: 400px;
                        height: 400px;
                        background: var(--primary);
                        top: -100px;
                        right: -100px;
                    }

                    .glow-2 {
                        width: 300px;
                        height: 300px;
                        background: var(--secondary);
                        bottom: -50px;
                        left: -50px;
                        animation-delay: -5s;
                    }

                    @keyframes pulse {
                        0% {
                            transform: scale(1) translate(0, 0);
                            opacity: 0.2;
                        }

                        100% {
                            transform: scale(1.2) translate(30px, 30px);
                            opacity: 0.4;
                        }
                    }

                    /* Login Container */
                    .login-container {
                        width: 100%;
                        max-width: 600px;
                        padding: 20px;
                        z-index: 10;
                    }

                    .login-card {
                        background: var(--glass-bg);
                        backdrop-filter: blur(25px);
                        -webkit-backdrop-filter: blur(25px);
                        border: 1px solid var(--glass-border);
                        border-radius: 32px;
                        padding: 50px 45px;
                        box-shadow: 0 40px 100px rgba(0, 0, 0, 0.5);
                        animation: slideUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
                    }

                    @keyframes slideUp {
                        from {
                            opacity: 0;
                            transform: translateY(40px);
                        }

                        to {
                            opacity: 1;
                            transform: translateY(0);
                        }
                    }

                    .header {
                        text-align: center;
                        margin-bottom: 45px;
                    }

                    .header .logo-area {
                        width: 64px;
                        height: 64px;
                        background: linear-gradient(135deg, var(--primary), var(--secondary));
                        border-radius: 18px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 20px;
                        box-shadow: 0 10px 25px rgba(78, 115, 223, 0.4);
                    }

                    .header .logo-area i {
                        font-size: 30px;
                        color: #fff;
                    }

                    .header h1 {
                        font-size: 30px;
                        font-weight: 700;
                        background: linear-gradient(to right, #fff, var(--primary-light));
                        -webkit-background-clip: text;
                        background-clip: text;
                        -webkit-text-fill-color: transparent;
                        letter-spacing: -0.5px;
                    }

                    .header p {
                        color: var(--text-dim);
                        font-size: 14px;
                        margin-top: 8px;
                    }

                    /* Form Styling */
                    .form-group {
                        margin-bottom: 24px;
                        position: relative;
                    }

                    .input-field {
                        position: relative;
                        display: flex;
                        align-items: center;
                        background: rgba(255, 255, 255, 0.04);
                        border: 1.5px solid rgba(255, 255, 255, 0.1);
                        border-radius: 16px;
                        padding: 0 20px;
                        height: 60px;
                        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    }

                    .input-field:focus-within {
                        border-color: var(--primary-light);
                        background: rgba(255, 255, 255, 0.08);
                        box-shadow: 0 0 0 4px rgba(162, 155, 254, 0.15);
                    }

                    .input-field i.icon {
                        color: var(--text-dim);
                        font-size: 20px;
                        margin-right: 15px;
                        transition: 0.3s;
                    }

                    .input-field:focus-within i.icon {
                        color: var(--primary-light);
                    }

                    .input-field input {
                        flex: 1;
                        background: transparent;
                        border: none;
                        outline: none;
                        color: #fff;
                        font-size: 16px;
                        height: 100%;
                    }

                    .input-field input::placeholder {
                        color: rgba(255, 255, 255, 0.25);
                    }

                    .eye-toggle {
                        cursor: pointer;
                        color: var(--text-dim);
                        padding: 10px;
                        margin-right: -10px;
                        transition: 0.3s;
                    }

                    /* Error States */
                    .form-group.has-error .input-field {
                        border-color: var(--error);
                        background: rgba(255, 118, 117, 0.05);
                    }

                    .error-hint {
                        position: absolute;
                        bottom: -20px;
                        left: 5px;
                        color: var(--error);
                        font-size: 12px;
                        display: none;
                        animation: fadeIn 0.3s ease;
                    }

                    .form-group.has-error .error-hint {
                        display: block;
                    }

                    @keyframes fadeIn {
                        from {
                            opacity: 0;
                            transform: translateY(-5px);
                        }

                        to {
                            opacity: 1;
                            transform: translateY(0);
                        }
                    }

                    /* Login Button */
                    .btn-login {
                        width: 100%;
                        height: 60px;
                        background: linear-gradient(135deg, var(--primary), var(--secondary));
                        border: none;
                        border-radius: 16px;
                        color: white;
                        font-size: 17px;
                        font-weight: 600;
                        cursor: pointer;
                        transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        margin-top: 10px;
                        box-shadow: 0 15px 30px rgba(78, 115, 223, 0.3);
                        position: relative;
                        overflow: hidden;
                    }

                    .btn-login:hover {
                        transform: translateY(-5px);
                        box-shadow: 0 20px 40px rgba(78, 115, 223, 0.4);
                        filter: brightness(1.1);
                    }

                    .btn-login:active {
                        transform: translateY(-2px);
                    }

                    /* Loader */
                    .btn-login .loader {
                        display: none;
                        width: 24px;
                        height: 24px;
                        border: 3px solid rgba(255, 255, 255, 0.3);
                        border-top-color: #fff;
                        border-radius: 50%;
                        animation: spin 0.8s linear infinite;
                        margin: 0 auto;
                    }

                    @keyframes spin {
                        to {
                            transform: rotate(360deg);
                        }
                    }

                    .btn-login.loading .loader {
                        display: block;
                    }

                    .btn-login.loading .btn-text {
                        display: none;
                    }

                    /* Footer Links */
                    .footer-links {
                        text-align: center;
                        margin-top: 30px;
                    }

                    .footer-links a {
                        color: var(--text-dim);
                        text-decoration: none;
                        font-size: 14px;
                        transition: 0.3s;
                        position: relative;
                    }

                    .footer-links a:hover {
                        color: var(--primary-light);
                    }

                    .footer-links a::after {
                        content: '';
                        position: absolute;
                        bottom: -4px;
                        left: 0;
                        width: 0;
                        height: 1px;
                        background: var(--primary-light);
                        transition: 0.3s;
                    }

                    .footer-links a:hover::after {
                        width: 100%;
                    }

                    /* Alert Messages */
                    .alert {
                        padding: 16px;
                        border-radius: 16px;
                        font-size: 14px;
                        margin-bottom: 24px;
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        animation: shake 0.5s ease-in-out;
                    }

                    @keyframes shake {

                        0%,
                        100% {
                            transform: translateX(0);
                        }

                        25% {
                            transform: translateX(-5px);
                        }

                        75% {
                            transform: translateX(5px);
                        }
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

                    /* Particles */
                    .particles-canvas {
                        position: absolute;
                        top: 0;
                        left: 0;
                        pointer-events: none;
                    }

                    /* Password Checklist */
                    .password-check-list {
                        margin-top: 15px;
                        padding: 15px;
                        background: rgba(255, 255, 255, 0.03);
                        border-radius: 12px;
                        border: 1px solid rgba(255, 255, 255, 0.05);
                        display: none;
                        flex-direction: column;
                        gap: 10px;
                        animation: fadeIn 0.3s ease;
                    }

                    .form-group.has-focus .password-check-list,
                    .form-group.has-error .password-check-list {
                        display: flex;
                    }

                    .check-item {
                        font-size: 13px;
                        color: var(--text-dim);
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        transition: 0.3s;
                    }

                    .check-item i {
                        font-size: 14px;
                        width: 16px;
                        text-align: center;
                    }

                    .check-item.valid {
                        color: var(--success);
                    }

                    .check-item.invalid {
                        color: var(--error);
                    }

                    .check-item.valid i {
                        color: var(--success);
                    }

                    .check-item.invalid i {
                        color: var(--error);
                    }
                </style>
            </head>

            <body>

                <div class="background-container">
                    <canvas id="particlesCanvas" class="particles-canvas"></canvas>
                    <img src="<%=request.getContextPath()%>/assets/img/clothing-bg.png" alt="Fashion Warehouse"
                        class="bg-image">
                    <div class="bg-overlay"></div>
                    <div class="glow glow-1"></div>
                    <div class="glow glow-2"></div>
                </div>

                <div class="login-container">
                    <div class="login-card">
                        <div class="header">
                            <div class="logo-area">
                                <i class="fas fa-tshirt"></i>
                            </div>
                            <h1>Warehouse Management System</h1>
                        </div>

                        <!-- Notifications -->
                        <c:if test="${not empty error}">
                            <div class="alert alert-error">
                                <i class="fas fa-exclamation-circle"></i>
                                ${error}
                            </div>
                        </c:if>
                        <c:if test="${not empty message}">
                            <div class="alert alert-success">
                                <i class="fas fa-check-circle"></i>
                                ${message}
                            </div>
                        </c:if>

                        <form id="loginForm" action="<%=request.getContextPath()%>/authen" method="post" novalidate>
                            <div class="form-group" id="emailGroup">
                                <div class="input-field">
                                    <i class="fas fa-envelope icon"></i>
                                    <input type="email" name="mail" id="email" placeholder="Enter your Email address"
                                        required autocomplete="email">
                                </div>
                                <span class="error-hint">Please enter a valid email address</span>
                            </div>

                            <div class="form-group" id="passGroup">
                                <div class="input-field">
                                    <i class="fas fa-fingerprint icon"></i>
                                    <input type="password" name="password" id="password" placeholder="Password"
                                        required autocomplete="current-password">
                                    <i class="fas fa-eye-slash eye-toggle" id="togglePassword"></i>
                                </div>
                                <div class="password-check-list" id="passwordCheckList">
                                    <div class="check-item" id="reqLength">
                                        <i class="fas fa-times-circle"></i> Password must be at least 8 characters long
                                    </div>
                                    <div class="check-item" id="reqUpper">
                                        <i class="fas fa-times-circle"></i> Password must contain at least one uppercase letter
                                    </div>
                                    <div class="check-item" id="reqNumber">
                                        <i class="fas fa-times-circle"></i> Password must contain at least one digit (0–9)
                                    </div>
                                </div>
                                <span class="error-hint" id="passwordError">Please enter a valid password</span>
                            </div>

                            <button type="submit" class="btn-login" id="loginBtn">
                                <span class="btn-text">Log In</span>
                                <div class="loader"></div>
                            </button>
                        </form>

                        <div class="footer-links">
                            <a href="<%=request.getContextPath()%>/authen?action=forgot">Forgot password?</a>
                        </div>
                    </div>
                </div>

                <script>
                    // Canvas Particles Animation
                    const canvas = document.getElementById('particlesCanvas');
                    const ctx = canvas.getContext('2d');
                    let particles = [];

                    function resize() {
                        canvas.width = window.innerWidth;
                        canvas.height = window.innerHeight;
                    }

                    window.addEventListener('resize', resize);
                    resize();

                    class Particle {
                        constructor() {
                            this.x = Math.random() * canvas.width;
                            this.y = Math.random() * canvas.height;
                            this.size = Math.random() * 2 + 1;
                            this.speedX = Math.random() * 0.5 - 0.25;
                            this.speedY = Math.random() * 0.5 - 0.25;
                            this.opacity = Math.random() * 0.5;
                        }

                        update() {
                            this.x += this.speedX;
                            this.y += this.speedY;
                            if (this.x > canvas.width)
                                this.x = 0;
                            if (this.x < 0)
                                this.x = canvas.width;
                            if (this.y > canvas.height)
                                this.y = 0;
                            if (this.y < 0)
                                this.y = canvas.height;
                        }

                        draw() {
                            ctx.fillStyle = 'rgba(255, 255, 255, ' + this.opacity + ')';
                            ctx.beginPath();
                            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
                            ctx.fill();
                        }
                    }

                    function init() {
                        particles = [];
                        for (let i = 0; i < 50; i++) {
                            particles.push(new Particle());
                        }
                    }

                    function animate() {
                        ctx.clearRect(0, 0, canvas.width, canvas.height);
                        particles.forEach(p => {
                            p.update();
                            p.draw();
                        });
                        requestAnimationFrame(animate);
                    }

                    init();
                    animate();

                    // Form Interactions
                    const togglePassword = document.querySelector('#togglePassword');
                    const password = document.querySelector('#password');
                    const loginForm = document.getElementById('loginForm');
                    const loginBtn = document.getElementById('loginBtn');

                    togglePassword.addEventListener('click', function () {
                        const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
                        password.setAttribute('type', type);
                        this.classList.toggle('fa-eye');
                        this.classList.toggle('fa-eye-slash');
                    });

                    // Real-time password validation logic
                    function updateRequirement(id, isValid) {
                        const el = document.getElementById(id);
                        const icon = el.querySelector('i');
                        if (isValid) {
                            el.classList.add('valid');
                            el.classList.remove('invalid');
                            icon.className = 'fas fa-check-circle';
                        } else {
                            el.classList.remove('valid');
                            el.classList.add('invalid');
                            icon.className = 'fas fa-times-circle';
                        }
                    }

                    function validatePassword() {
                        const val = password.value;
                        const group = document.getElementById('passGroup');

                        const isLengthValid = val.length >= 8;
                        const hasUpperCase = /[A-Z]/.test(val);
                        const hasNumber = /\d/.test(val);

                        updateRequirement('reqLength', isLengthValid);
                        updateRequirement('reqUpper', hasUpperCase);
                        updateRequirement('reqNumber', hasNumber);

                        const isAllValid = isLengthValid && hasUpperCase && hasNumber;

                        if (val.length > 0) {
                            document.getElementById('passwordCheckList').style.display = 'flex';
                            if (!isAllValid) {
                                group.classList.add('has-error');
                                loginBtn.style.opacity = "0.5";
                                loginBtn.style.pointerEvents = "none";
                            } else {
                                group.classList.remove('has-error');
                                loginBtn.style.opacity = "1";
                                loginBtn.style.pointerEvents = "auto";
                            }
                        } else {
                            group.classList.remove('has-error');
                            loginBtn.style.opacity = "1";
                            loginBtn.style.pointerEvents = "auto";
                        }
                        return isAllValid;
                    }

                    password.addEventListener('input', validatePassword);
                    password.addEventListener('focus', () => {
                        document.getElementById('passGroup').classList.add('has-focus');
                    });

                    loginForm.addEventListener('submit', function (e) {
                        let isValid = true;
                        const emailInput = document.getElementById('email');
                        const emailValue = emailInput.value.trim();
                        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

                        if (!emailValue || !emailRegex.test(emailValue)) {
                            document.getElementById('emailGroup').classList.add('has-error');
                            isValid = false;
                        } else {
                            document.getElementById('emailGroup').classList.remove('has-error');
                        }

                        const passValue = password.value;
                        const isAllValid = validatePassword();

                        if (!passValue) {
                            document.getElementById('passGroup').classList.add('has-error');
                            document.getElementById('passwordError').textContent = "Vui lòng nhập mật khẩu";
                            isValid = false;
                        } else if (!isAllValid) {
                            document.getElementById('passGroup').classList.add('has-error');
                            isValid = false;
                        } else {
                            document.getElementById('passGroup').classList.remove('has-error');
                        }

                        if (!isValid) {
                            e.preventDefault();
                        } else {
                            loginBtn.classList.add('loading');
                            loginBtn.style.pointerEvents = 'none';
                        }
                    });

                    // Clear error on input
                    document.querySelectorAll('input').forEach(input => {
                        input.addEventListener('input', function () {
                            this.closest('.form-group').classList.remove('has-error');
                        });
                    });
                </script>
            </body>

            </html>