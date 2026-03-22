package com.example.HM.controller;

import com.example.HM.dto.UserRegisterDTO;
import com.example.HM.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> processRegister(@ModelAttribute UserRegisterDTO request) {
        Map<String, String> response = new HashMap<>();
        try {
            accountService.register(request);
            response.put("status", "success");
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
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
