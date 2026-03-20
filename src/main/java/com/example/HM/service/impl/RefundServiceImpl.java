package com.example.HM.service.impl;

import com.example.HM.dto.RefundRequest;
import com.example.HM.entity.Booking;
import com.example.HM.entity.Refund;
import com.example.HM.repository.BookingRepository;
import com.example.HM.repository.RefundRepository;
import com.example.HM.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Refund requestRefund(RefundRequest request) {
        if (refundRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("Yêu cầu hoàn tiền cho đơn hàng này đã tồn tại!");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        Refund refund = new Refund();
        refund.setBooking(booking);
        refund.setReason(request.getReason());
        refund.setRefundAmount(request.getAmount() != null ? request.getAmount() : booking.getPaidAmount());
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
    public void approveRefund(String id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn tiền không tồn tại!"));
        refund.setStatus("APPROVED");
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);
    }

    @Override
    @Transactional
    public void rejectRefund(String id, String reason) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn tiền không tồn tại!"));
        refund.setStatus("REJECTED");
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);
    }
}
