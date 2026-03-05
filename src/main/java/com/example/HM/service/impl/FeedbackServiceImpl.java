package com.example.HM.service.impl;

import com.example.HM.dto.FeedbackRequest;
import com.example.HM.dto.RefundRequest;
import com.example.HM.dto.RoomIssueRequest;
import com.example.HM.entity.Booking;
import com.example.HM.entity.Feedback;
import com.example.HM.entity.Refund;
import com.example.HM.entity.Room;
import com.example.HM.entity.RoomIssueReport;
import com.example.HM.repository.BookingRepository;
import com.example.HM.repository.FeedbackRepository;
import com.example.HM.repository.RefundRepository;
import com.example.HM.repository.RoomIssueReportRepository;
import com.example.HM.repository.RoomRepository;
import com.example.HM.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RoomIssueReportRepository roomIssueReportRepository;
    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    // ======================== FEEDBACK ========================

    @Override
    @Transactional
    public Feedback submitFeedback(FeedbackRequest request) {
        if (request.getBookingId() == null || request.getBookingId().isBlank()) {
            throw new RuntimeException("Mã đặt phòng không được để trống!");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Số sao phải từ 1 đến 5!");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        if (feedbackRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("Bạn đã gửi đánh giá cho đơn này rồi!");
        }

        Feedback feedback = new Feedback();
        feedback.setBooking(booking);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        return feedbackRepository.save(feedback);
    }

    @Override
    public Page<Feedback> getAllFeedbacks(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ======================== ROOM ISSUES ========================

    @Override
    @Transactional
    public RoomIssueReport reportIssue(RoomIssueRequest request) {
        if (request.getBookingId() == null || request.getBookingId().isBlank()) {
            throw new RuntimeException("Mã đặt phòng không được để trống!");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new RuntimeException("Mô tả sự cố không được để trống!");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        RoomIssueReport issue = new RoomIssueReport();
        issue.setBooking(booking);
        issue.setDescription(request.getDescription());
        issue.setStatus("PENDING");

        if (request.getRoomId() != null && !request.getRoomId().isBlank()) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));
            issue.setRoom(room);
        }

        return roomIssueReportRepository.save(issue);
    }

    @Override
    public Page<RoomIssueReport> getAllIssues(Pageable pageable) {
        return roomIssueReportRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional
    public RoomIssueReport resolveIssue(String issueId) {
        RoomIssueReport issue = roomIssueReportRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Sự cố không tồn tại!"));
        issue.setStatus("RESOLVED");
        issue.setResolvedAt(LocalDateTime.now());
        return roomIssueReportRepository.save(issue);
    }

    // ======================== REFUND ========================

    @Override
    @Transactional
    public Refund requestRefund(RefundRequest request) {
        if (request.getBookingId() == null || request.getBookingId().isBlank()) {
            throw new RuntimeException("Mã đặt phòng không được để trống!");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new RuntimeException("Lý do hoàn tiền không được để trống!");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0!");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        if (refundRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("Bạn đã gửi yêu cầu hoàn tiền cho đơn này rồi!");
        }

        Refund refund = new Refund();
        refund.setBooking(booking);
        refund.setReason(request.getReason());
        refund.setRefundAmount(request.getAmount());
        refund.setStatus("PENDING");
        refund.setRequestedAt(LocalDateTime.now());

        return refundRepository.save(refund);
    }

    @Override
    public Page<Refund> getAllRefunds(Pageable pageable) {
        return refundRepository.findAllByOrderByRequestedAtDesc(pageable);
    }

    @Override
    @Transactional
    public Refund updateRefundStatus(String refundId, String status) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn tiền không tồn tại!"));

        if (!"PENDING".equals(refund.getStatus())) {
            throw new RuntimeException("Yêu cầu này đã được xử lý!");
        }

        refund.setStatus(status);
        refund.setProcessedAt(LocalDateTime.now());
        return refundRepository.save(refund);
    }
}
