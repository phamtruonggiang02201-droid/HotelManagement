package com.example.HM.service;

import com.example.HM.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Page<PaymentDTO> getMyPayments(String accountId, Pageable pageable);
    Page<PaymentDTO> getAllPayments(Pageable pageable);
    java.io.ByteArrayInputStream exportPaymentsToExcel();
}
