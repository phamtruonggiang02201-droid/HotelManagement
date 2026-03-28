package com.example.HM.service;

import com.example.HM.dto.RefundRequest;
import com.example.HM.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundService {
    Refund requestRefund(RefundRequest request);
    Page<Refund> getAllRefunds(Pageable pageable);
    void approveRefund(String id);
    void rejectRefund(String id, String reason);
}
