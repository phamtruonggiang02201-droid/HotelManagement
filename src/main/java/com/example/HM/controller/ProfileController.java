package com.example.HM.controller;

import com.example.HM.dto.AccountDTO;
import com.example.HM.entity.Account;
import com.example.HM.security.CustomUserDetails;
import com.example.HM.service.AccountService;
import com.example.HM.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final AccountService accountService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication != null) {
            model.addAttribute("account", accountService.findByUsername(authentication.getName()));
        }
        return "profile";
    }

    @PostMapping("/api/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam String fullName, Authentication authentication) {
        try {
            accountService.updateProfile(authentication.getName(), fullName);
            return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword, 
                                          @RequestParam String newPassword, 
                                          Authentication authentication) {
        try {
            accountService.changePassword(authentication.getName(), oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn ảnh!"));
        }

        try {
            // 1. Upload to Cloudinary
            Map uploadResult = cloudinaryService.upload(file);
            String avatarUrl = (String) uploadResult.get("url");

            // 2. Save to database
            accountService.updateAvatar(userDetails.getUsername(), avatarUrl);

            // 3. Fetch fresh DTO from DB
            AccountDTO updatedAccount = accountService.findByUsername(userDetails.getUsername());

            // 4. Update Authentication in Security Context (Optional, but recommended for header sync)
            // We fetch the entity internally in the service, but here we need it for CustomUserDetails password
            // Since we can't expose password in DTO, we might need a separate call or handle it in service
            // For now, let's just return the DTO and fix the bug by NOT passing nulls.
            
            // NOTE: To update the session, we still need the password. 
            // I will fetch the entity via the service JUST for the session update.
            Account accountEntity = accountService.findAccountByUsername(userDetails.getUsername());
            
            CustomUserDetails updatedPrincipal = new CustomUserDetails(
                accountEntity.getUsername(),
                accountEntity.getPassword(),
                accountEntity.getEmailVerified() != null && accountEntity.getEmailVerified(),
                true,
                true,
                accountEntity.getStatus(),
                userDetails.getAuthorities(),
                updatedAccount.getFullName(),
                updatedAccount.getRoleName(),
                avatarUrl
            );
            
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedPrincipal, 
                null, // Password can be null in Token after authentication
                updatedPrincipal.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return ResponseEntity.ok(Map.of(
                "message", "Cập nhật ảnh đại diện thành công!",
                "account", updatedAccount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
