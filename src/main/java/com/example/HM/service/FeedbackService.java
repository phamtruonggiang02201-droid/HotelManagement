package com.example.HM.service;

import com.example.HM.dto.FeedbackRequest;
import com.example.HM.dto.RefundRequest;
import com.example.HM.dto.RoomIssueRequest;
import com.example.HM.entity.Feedback;
import com.example.HM.entity.Refund;
import com.example.HM.entity.RoomIssueReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {

    // Feedback
    Feedback submitFeedback(FeedbackRequest request);
    Page<Feedback> getAllFeedbacks(Integer rating, Pageable pageable);
    Feedback replyToFeedback(String feedbackId, String reply);
    Page<Feedback> getFeedbacksByRoomType(String roomTypeId, Pageable pageable);

    // Room Issues
    RoomIssueReport reportIssue(RoomIssueRequest request);
    Page<RoomIssueReport> getAllIssues(Pageable pageable);
    RoomIssueReport resolveIssue(String issueId);

    // Refund
    Refund requestRefund(RefundRequest request);
    Page<Refund> getAllRefunds(Pageable pageable);
    Refund updateRefundStatus(String refundId, String status);
}
