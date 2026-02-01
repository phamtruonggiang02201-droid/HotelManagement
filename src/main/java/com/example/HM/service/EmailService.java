package com.example.HM.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendHtmlMessage(String to, String subject, String htmlBody);
    void sendRegistrationEmail(String to, String username, String fullName, String verifyUrl);
    void sendForgotPasswordEmail(String to, String resetUrl, String fullName);
}
