package com.example.HM.controller;

import com.example.HM.dto.AssignmentRequest;
import com.example.HM.dto.AssignmentResponseDTO;
import com.example.HM.service.WorkAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/management/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final WorkAssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String index(Model model) {
        return "management/assignment/index";
    }

    @GetMapping("/my-schedule")
    public String mySchedule(Model model) {
        return "management/assignment/my-schedule";
    }

    // --- API ENDPOINTS ---

    @PostMapping("/api")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseBody
    public ResponseEntity<?> assignWork(@RequestBody AssignmentRequest request) {
        try {
            return ResponseEntity.ok(assignmentService.assignWork(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/apply-week")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseBody
    public ResponseEntity<?> applyWeek(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            int count = assignmentService.applyWeek(date);
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/copy-next-day")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseBody
    public ResponseEntity<?> copyToNextDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            int count = assignmentService.copyToNextDay(date);
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Page<AssignmentResponseDTO>> getAssignments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(assignmentService.getAssignments(start, end, pageable));
    }

    @GetMapping("/api/by-date")
    @ResponseBody
    public ResponseEntity<List<AssignmentResponseDTO>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByDate(date));
    }

    @GetMapping("/api/my")
    @ResponseBody
    public ResponseEntity<List<AssignmentResponseDTO>> getMyAssignments() {
        return ResponseEntity.ok(assignmentService.getMyAssignments());
    }

    @DeleteMapping("/api/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseBody
    public ResponseEntity<?> deleteAssignment(@PathVariable String id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok(Map.of("message", "Xóa phân công thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseBody
    public ResponseEntity<?> assignTask(@PathVariable String id, @RequestParam String employeeId, @RequestParam String shift) {
        try {
            return ResponseEntity.ok(assignmentService.assignTask(id, employeeId, shift));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
