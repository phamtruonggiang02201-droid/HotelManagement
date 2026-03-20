package com.example.HM.controller;

import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.ChangePasswordRequest;
import com.example.HM.dto.UpdateProfileRequest;
import com.example.HM.entity.Account;
import com.example.HM.security.CustomUserDetails;
import com.example.HM.security.SecurityUtils;
import com.example.HM.service.AccountService;
import com.example.HM.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final AccountService accountService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public String profile(Model model) {
        AccountDTO profile = accountService.getCurrentProfile();
        if (profile != null) {
            model.addAttribute("account", profile);
        }
        return "profile";
    }

    @PostMapping("/api/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@ModelAttribute UpdateProfileRequest request) {
        try {
            accountService.updateProfile(request);
            return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@ModelAttribute ChangePasswordRequest request) {
        try {
            accountService.changePassword(request);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn ảnh!"));
        }

        try {
            // 1. Upload to Cloudinary
            Map uploadResult = cloudinaryService.upload(file);
            String avatarUrl = (String) uploadResult.get("url");

            // 2. Save to database
            accountService.updateAvatar(avatarUrl);

            // 3. Fetch fresh DTO from DB
            String username = SecurityUtils.getCurrentUsername();
            AccountDTO updatedAccount = accountService.findByUsername(username);

            // 4. Update Authentication in Security Context for header sync
            Account accountEntity = accountService.findAccountByUsername(username);
            CustomUserDetails currentUser = SecurityUtils.getCurrentUserDetails();
            
            CustomUserDetails updatedPrincipal = new CustomUserDetails(
                accountEntity.getId(),
                accountEntity.getUsername(),
                accountEntity.getPassword(),
                accountEntity.getEmailVerified() != null && accountEntity.getEmailVerified(),
                true,
                true,
                accountEntity.getStatus(),
                currentUser != null ? currentUser.getAuthorities() : Collections.emptyList(),
                updatedAccount.getFullName(),
                updatedAccount.getRoleName(),
                avatarUrl
            );
            
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedPrincipal, 
                null, 
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
