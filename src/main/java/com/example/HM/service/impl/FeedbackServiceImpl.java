package com.example.HM.service.impl;

import com.example.HM.dto.FeedbackRequest;
import com.example.HM.dto.RefundRequest;
import com.example.HM.dto.RoomIssueRequest;
import com.example.HM.entity.*;
import com.example.HM.repository.*;
import com.example.HM.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RoomIssueReportRepository roomIssueReportRepository;
    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ExtraServiceRepository extraServiceRepository;
    private final PaymentRepository paymentRepository;

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

        java.util.Optional<Feedback> existingFeedback = java.util.Optional.empty();
        RoomType roomType = null;
        ExtraService service = null;

        if (request.getRoomTypeId() != null && !request.getRoomTypeId().isBlank()) {
            roomType = roomTypeRepository.findById(request.getRoomTypeId()).orElse(null);
            if (roomType != null) {
                existingFeedback = feedbackRepository.findByBookingAndRoomType(booking, roomType);
            }
        } else if (request.getServiceId() != null && !request.getServiceId().isBlank()) {
            service = extraServiceRepository.findById(request.getServiceId()).orElse(null);
            if (service != null) {
                existingFeedback = feedbackRepository.findByBookingAndExtraService(booking, service);
            }
        }

        Feedback feedback = existingFeedback.orElse(new Feedback());
        feedback.setBooking(booking);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setRoomType(roomType);
        feedback.setExtraService(service);

        // Reset admin reply if user edits feedback? Maybe keep it. 
        // For now, let's just update the user content.

        return feedbackRepository.save(feedback);
    }

    @Override
    public Page<Feedback> getAllFeedbacks(Integer rating, Pageable pageable) {
        if (rating != null && rating >= 1 && rating <= 5) {
            return feedbackRepository.findByRatingOrderByCreatedAtDesc(rating, pageable);
        }
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Feedback> getFeedbacksByRoomType(String roomTypeId, Pageable pageable) {
        return feedbackRepository.findByRoomType_IdOrderByCreatedAtDesc(roomTypeId, pageable);
    }

    @Override
    @Transactional
    public Feedback replyToFeedback(String feedbackId, String reply) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại!"));
        
        feedback.setAdminReply(reply);
        feedback.setRepliedAt(LocalDateTime.now());
        
        return feedbackRepository.save(feedback);
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
    public Page<RoomIssueReport> getAllIssues(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return roomIssueReportRepository.findByStatus(status, pageable);
        }
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

        // Kiểm tra số dư khả dụng để hoàn tiền
        BigDecimal paidAmount = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        if (paidAmount.compareTo(request.getAmount()) < 0) {
            String msg = String.format("Số tiền hoàn trả (%s đ) không được vượt quá số tiền khách đã thanh toán (%s đ)!", 
                new java.text.DecimalFormat("#,###").format(request.getAmount()),
                new java.text.DecimalFormat("#,###").format(paidAmount));
            throw new RuntimeException(msg);
        }

        Refund refund = new Refund();
        refund.setBooking(booking);
        refund.setReason(request.getReason());
        refund.setRefundAmount(request.getAmount());
        refund.setStatus("PENDING");
        refund.setRequestedAt(LocalDateTime.now());

        // Tự động tìm giao dịch gần nhất để liên kết
        paymentRepository.findTopByBookingIdAndPaymentStatusOrderByPaymentDateDesc(booking.getId(), "SUCCESS")
                .ifPresent(refund::setPayment);

        return refundRepository.save(refund);
    }

    @Override
    public Page<Refund> getAllRefunds(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return refundRepository.findByStatus(status, pageable);
        }
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

        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("Trạng thái không hợp lệ! Chỉ chấp nhận APPROVED hoặc REJECTED.");
        }

        if ("APPROVED".equals(status)) {
            Booking booking = refund.getBooking();
            
            // 1. Cập nhật paidAmount trong Booking
            BigDecimal currentPaid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
            booking.setPaidAmount(currentPaid.subtract(refund.getRefundAmount()));
            bookingRepository.save(booking);

            // 2. Tạo bản ghi Payment âm (Phiếu chi)
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(refund.getRefundAmount().negate());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus("REFUNDED");
            if (refund.getPayment() != null) {
                payment.setPaymentMethod(refund.getPayment().getPaymentMethod());
            }
            paymentRepository.save(payment);
        }

        refund.setStatus(status);
        refund.setProcessedAt(LocalDateTime.now());
        return refundRepository.save(refund);
    }
}
