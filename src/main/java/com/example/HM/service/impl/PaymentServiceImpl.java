package com.example.HM.service.impl;

import com.example.HM.dto.PaymentDTO;
import com.example.HM.entity.Payment;
import com.example.HM.repository.PaymentRepository;
import com.example.HM.repository.RefundRepository;
import com.example.HM.service.PaymentService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    public Page<PaymentDTO> getMyPayments(String accountId, Pageable pageable) {
        return paymentRepository.findByAccountId(accountId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        return paymentRepository.findAllByOrderByPaymentDateDesc(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public ByteArrayInputStream exportPaymentsToExcel() {
        List<Payment> payments = paymentRepository.findAll();
        String[] headers = { "ID", "Khách hàng", "Booking ID", "Số tiền", "Ngày thanh toán", "Phương thức", "Trạng thái" };
        
        return ExcelHelper.dataToExcel(payments, "Payments", headers, (row, p) -> {
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getBooking() != null ? 
                (p.getBooking().getGuest() != null ? p.getBooking().getGuest().getFullName() : 
                 (p.getBooking().getAccount() != null ? p.getBooking().getAccount().getFirstName() + " " + p.getBooking().getAccount().getLastName() : "Vãng lai")) : "N/A");
            row.createCell(2).setCellValue(p.getBooking() != null ? p.getBooking().getId() : "N/A");
            row.createCell(3).setCellValue(p.getAmount().doubleValue());
            row.createCell(4).setCellValue(p.getPaymentDate().toString());
            row.createCell(5).setCellValue(p.getPaymentMethod() != null ? p.getPaymentMethod().getMethodName() : "VNPay");
            row.createCell(6).setCellValue(p.getPaymentStatus());
        });
    }

    private PaymentDTO convertToDTO(Payment payment) {
        String bookingId = payment.getBooking() != null ? payment.getBooking().getId() : null;
        String refundStatus = null;
        String refundId = null;
        if (bookingId != null && !bookingId.equals("N/A")) {
            var refundOpt = refundRepository.findTopByBookingIdOrderByRequestedAtDesc(bookingId);
            if (refundOpt.isPresent()) {
                refundStatus = refundOpt.get().getStatus();
                refundId = refundOpt.get().getId();
            }
        }
        return PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(bookingId != null ? bookingId : "N/A")
                .guestName(payment.getBooking() != null ? 
                        (payment.getBooking().getGuest() != null ? payment.getBooking().getGuest().getFullName() : 
                         (payment.getBooking().getAccount() != null ? payment.getBooking().getAccount().getFirstName() + " " + payment.getBooking().getAccount().getLastName() : "Vãng lai")) : "N/A")
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().getMethodName() : "VNPay")
                .refundStatus(refundStatus)
                .refundId(refundId)
                .build();
    }
}
