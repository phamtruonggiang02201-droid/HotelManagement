package com.example.HM.controller;

import com.example.HM.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class PasswordResetController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/api/forgot-password")
    @ResponseBody
    public ResponseEntity<?> processForgotPassword(@RequestParam String email) {
        try {
            accountService.processForgotPassword(email);
            return ResponseEntity.ok(Map.of("message", "Yêu cầu đã được gửi! Vui lòng kiểm tra email của bạn."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestParam String token, 
                                          @RequestParam String password,
                                          @RequestParam String confirmPassword) {
        try {
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu xác nhận không khớp!"));
            }
            accountService.resetPassword(token, password);
            return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được đặt lại thành công! Đang chuyển hướng đến trang đăng nhập...", "targetUrl", "/login"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
