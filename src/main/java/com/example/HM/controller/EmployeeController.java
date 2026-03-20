package com.example.HM.controller;

import com.example.HM.dto.AdminAccountRequest;
import com.example.HM.dto.EmployeeResponseDTO;
import com.example.HM.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/management/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class EmployeeController {

    private final AccountService accountService;

    @GetMapping
    public String index(Model model) {
        return "management/employee/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        return "management/employee/detail";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("employeeId", id);
        return "management/employee/detail";
    }

    // --- API ENDPOINTS ---

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployees(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(accountService.getEmployees(search, pageable));
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createEmployee(@RequestBody AdminAccountRequest request) {
        try {
            accountService.createAccountByAdmin(request);
            return ResponseEntity.ok(Map.of("message", "Thêm nhân viên thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateEmployee(@PathVariable String id, @RequestBody AdminAccountRequest request) {
        try {
            accountService.updateAccountByAdmin(id, request);
            return ResponseEntity.ok(Map.of("message", "Cập nhật nhân viên thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteEmployee(@PathVariable String id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok(Map.of("message", "Xóa nhân viên thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
