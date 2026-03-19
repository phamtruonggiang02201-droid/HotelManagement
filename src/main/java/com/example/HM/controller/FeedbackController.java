package com.example.HM.controller;

import com.example.HM.dto.FeedbackRequest;
import com.example.HM.dto.RefundRequest;
import com.example.HM.dto.RoomIssueRequest;
import com.example.HM.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ============ API: SUBMIT FEEDBACK ============

    @PostMapping("/api/submit")
    @ResponseBody
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest request) {
        try {
            feedbackService.submitFeedback(request);
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã gửi đánh giá!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============ API: REPORT ROOM ISSUE ============

    @PostMapping("/api/report-issue")
    @ResponseBody
    public ResponseEntity<?> reportIssue(@RequestBody RoomIssueRequest request) {
        try {
            feedbackService.reportIssue(request);
            return ResponseEntity.ok(Map.of("message", "Báo cáo sự cố thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============ API: REQUEST REFUND ============

    @PostMapping("/api/request-refund")
    @ResponseBody
    public ResponseEntity<?> requestRefund(@RequestBody RefundRequest request) {
        try {
            feedbackService.requestRefund(request);
            return ResponseEntity.ok(Map.of("message", "Yêu cầu hoàn tiền đã được gửi!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============ ADMIN: FEEDBACK SUMMARY PAGE ============

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public String feedbackSummary(Model model, 
                                 @RequestParam(required = false) Integer rating,
                                 @PageableDefault(size = 20) Pageable pageable) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedbacks(rating, pageable));
        model.addAttribute("issues", feedbackService.getAllIssues(pageable));
        model.addAttribute("refunds", feedbackService.getAllRefunds(pageable));
        model.addAttribute("currentRating", rating);
        return "management/feedback/index";
    }

    @PostMapping("/api/{id}/reply")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public ResponseEntity<?> replyToFeedback(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String reply = body.get("reply");
            if (reply == null || reply.isBlank()) {
                throw new RuntimeException("Nội dung phản hồi không được để trống!");
            }
            feedbackService.replyToFeedback(id, reply);
            return ResponseEntity.ok(Map.of("message", "Đã gửi phản hồi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============ ADMIN: RESOLVE ISSUE ============

    @PutMapping("/api/issues/{id}/resolve")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> resolveIssue(@PathVariable String id) {
        try {
            feedbackService.resolveIssue(id);
            return ResponseEntity.ok(Map.of("message", "Đã xử lý sự cố!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============ ADMIN: UPDATE REFUND STATUS ============

    @PutMapping("/api/refunds/{id}/status")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateRefundStatus(@PathVariable String id, @RequestParam String status) {
        try {
            feedbackService.updateRefundStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái hoàn tiền thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
