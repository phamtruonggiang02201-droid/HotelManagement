package com.example.HM.controller;

import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.AdminAccountRequest;
import com.example.HM.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AccountController {

    private final AccountService accountService;

    // --- VIEW ROUTES ---

    @GetMapping("/accounts")
    public String accountIndex() {
        return "management/account/index";
    }

    @GetMapping("/accounts/new")
    public String newAccountForm(Model model) {
        model.addAttribute("mode", "create");
        return "management/account/detail";
    }

    @GetMapping("/accounts/{id}")
    public String accountDetail(@PathVariable String id, Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("accountId", id);
        return "management/account/detail";
    }

    // --- API ENDPOINTS ---

    @GetMapping("/api/accounts")
    @ResponseBody
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/api/accounts/{id}")
    @ResponseBody
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping("/api/accounts")
    @ResponseBody
    public ResponseEntity<?> createAccount(@RequestBody AdminAccountRequest request) {
        try {
            AccountDTO newAccount = accountService.createAccountByAdmin(request);
            return ResponseEntity.ok(Map.of("message", "Tạo tài khoản thành công!", "account", newAccount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/accounts/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAccount(@PathVariable String id, @RequestBody AdminAccountRequest request) {
        try {
            AccountDTO updated = accountService.updateAccountByAdmin(id, request);
            return ResponseEntity.ok(Map.of("message", "Cập nhật tài khoản thành công!", "account", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/accounts/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAccount(@PathVariable String id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok(Map.of("message", "Xóa tài khoản thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

