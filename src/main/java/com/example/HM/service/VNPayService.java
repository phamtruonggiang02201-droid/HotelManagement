package com.example.HM.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {

    /**
     * Generate payment URL for VNPay
     * @param amount Amount in VND
     * @param orderInfo Order description
     * @param txnRef Transaction reference (Booking ID)
     * @param request HttpServletRequest for IP extraction
     * @return Payment URL
     */
    String createPaymentUrl(long amount, String orderInfo, String txnRef, HttpServletRequest request);

    /**
     * Verify signature from VNPay response
     * @param params Response parameters
     * @return true if signature is valid
     */
    boolean verifySignature(java.util.Map<String, String> params);
}
