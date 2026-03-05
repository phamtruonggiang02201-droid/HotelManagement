package com.example.HM.service.impl;

import com.example.HM.config.EmailConfig;
import com.example.HM.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private ITemplateEngine templateEngine;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailConfig.getFromEmail());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        try {
            jakarta.mail.internet.MimeMessage message = emailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailConfig.getFromEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            emailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Gửi mail thất bại: " + e.getMessage());
        }
    }

    /**
     * Gửi email đăng ký tài khoản thành công sử dụng template HTML
     */
    public void sendRegistrationEmail(String to, String username, String fullName, String verifyUrl) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("username", username);
        context.setVariable("fullName", fullName);
        context.setVariable("verifyUrl", verifyUrl);
        context.setVariable("loginUrl", "http://localhost:8080/login");

        String htmlContent = templateEngine.process("mail/registration", context);
        sendHtmlMessage(to, "Kích hoạt tài khoản LuxeStay của bạn", htmlContent);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetUrl, String fullName) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("fullName", fullName);

        String htmlContent = templateEngine.process("mail/forgot-password", context);
        sendHtmlMessage(to, "Khôi phục mật khẩu tài khoản LuxeStay", htmlContent);
    }

    @Override
    public void sendEmployeeCredentialsEmail(String to, String username, String plainPassword, String fullName) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("username", username);
        context.setVariable("password", plainPassword);
        context.setVariable("fullName", fullName);
        context.setVariable("loginUrl", "http://localhost:8080/login");

        String htmlContent = templateEngine.process("mail/employee-credentials", context);
        sendHtmlMessage(to, "Thông tin tài khoản nhân viên LuxeStay của bạn", htmlContent);
    }
}
