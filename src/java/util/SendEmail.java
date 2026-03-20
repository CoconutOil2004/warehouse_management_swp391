/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 *
 * @author Chanh
 */
public class SendEmail {

    // Lấy thông tin từ biến môi trường
    private static final String EMAIL_SENDER = "xuanquels@gmail.com";
    private static final String EMAIL_PASSWORD = "Opnep1204";

    // Tạo phiên gửi email
    private static Session getSession() {
        if (EMAIL_SENDER == null || EMAIL_PASSWORD == null) {
            System.err.println(
                    "ERROR: Config missing. Please set MAIL_USERNAME and MAIL_PASSWORD in environment variables.");
            return null;
        }

        @SuppressWarnings("unused")
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
            }
        });
    }

    // Gửi email với nội dung HTML
    private static void sendEmail(String toEmail, String subject, String content) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Error sending email to " + toEmail + ": " + e.getMessage());
        }
    }

    // Gửi OTP
    public static void sendOTP(String toEmail, String otp) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your OTP Code");

            String content = "<h3>Your OTP code is: <strong>" + otp + "</strong></h3>";
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("OTP sent to: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Gửi email xác minh
    public static void sendVerificationEmail(String toEmail, String verificationCode) {
        System.out.println("Preparing to send verification email to: " + toEmail);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Verification Code");

            // Nội dung email
            String emailContent = "<h3>Hello,</h3>"
                    + "<p>Your verification code is: <strong>" + verificationCode + "</strong></p>"
                    + "<p>Please enter this code on the verification page to complete your registration.</p>"
                    + "<p>If you did not register, please ignore this email.</p>";

            message.setContent(emailContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Verification email has been sent to: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Error sending email: " + e.getMessage());
        }
    }

    // Gửi email thông tin mật khẩu
    public static void sendPasswordEmail(String toEmail, String password) {
        System.out.println("Preparing to send password email to: " + toEmail);
        String emailContent = "<h3>Hello,</h3>"
                + "<p>Your account has been successfully created.</p>"
                + "<p>Your username: <strong>" + toEmail + "</strong></p>"
                + "<p>Your temporary password is: <strong>" + password + "</strong></p>"
                + "<p>Please log in and change your password as soon as possible.</p>"
                + "<br><p>Best regards,</p>"
                + "<p><strong>Admin Team</strong></p>";
        sendEmail(toEmail, "Your New Account Password", emailContent);
    }
}
