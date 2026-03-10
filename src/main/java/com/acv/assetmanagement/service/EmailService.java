package com.acv.assetmanagement.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmployeeCredentials(String toEmail, String username, String initialPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ACV Asset Management <no-reply@acv.vn>");
            message.setTo(toEmail);
            message.setSubject("Chào mừng bạn gia nhập hệ thống ACV Asset Management");
            message.setText("Chào bạn,\n\n" +
                    "Tài khoản hệ thống Quản lý tài sản ACV của bạn đã được khởi tạo thành công.\n" +
                    "Dưới đây là thông tin đăng nhập của bạn:\n\n" +
                    "Trang web: http://localhost:8080\n" +
                    "Tên đăng nhập: " + username + "\n" +
                    "Mật khẩu tạm thời: " + initialPassword + "\n\n" +
                    "Vui lòng đăng nhập và đổi mật khẩu ngay sau khi truy cập.\n\n" +
                    "Trân trọng,\n" +
                    "Ban quản trị hệ thống ACV.");

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            // We don't throw exception to avoid breaking the registration flow if email
            // fails
        }
    }
}
