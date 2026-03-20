package com.example.HM.controller;

import com.example.HM.dto.AssignmentResponseDTO;
import com.example.HM.security.SecurityUtils;
import com.example.HM.service.WorkAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/tasks")
@RequiredArgsConstructor
public class StaffTaskController {

    private final WorkAssignmentService assignmentService;

    @GetMapping("/pool")
    public ResponseEntity<List<AssignmentResponseDTO>> getTaskPool() {
        // Tự động lấy Role của user hiện tại từ Security Context
        var userDetails = SecurityUtils.getCurrentUserDetails();
        String roleName = userDetails != null ? userDetails.getRoleName() : "";
        return ResponseEntity.ok(assignmentService.getTasksInPool(roleName));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AssignmentResponseDTO>> getMyActiveTasks() {
        return ResponseEntity.ok(assignmentService.getMyActiveTasks());
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<Map<String, Object>> claimTask(@PathVariable String id) {
        try {
            assignmentService.claimTask(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã nhận nhiệm vụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> completeTask(@PathVariable String id, @RequestParam String status) {
        try {
            if ("COMPLETED".equals(status)) {
                assignmentService.completeTask(id);
            } else {
                assignmentService.updateStatus(id, status);
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
