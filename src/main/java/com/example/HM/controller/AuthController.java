package com.example.HM.controller;

import com.example.HM.dto.RegisterRequest;
import com.example.HM.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/register")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> processRegister(@ModelAttribute RegisterRequest request) {
        Map<String, String> response = new java.util.HashMap<>();
        try {
            accountService.register(request);
            response.put("status", "success");
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean verified = accountService.verifyEmail(token);
        if (verified) {
            redirectAttributes.addFlashAttribute("message", "Tài khoản của bạn đã được kích hoạt thành công. Bây giờ bạn có thể đăng nhập.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Mã xác thực không hợp lệ hoặc đã hết hạn.");
        }
        return "redirect:/login";
    }
}
