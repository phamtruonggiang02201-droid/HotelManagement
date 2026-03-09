package com.example.HM.controller;

import com.example.HM.constant.BookingStatus;
import com.example.HM.service.BookingService;
import com.example.HM.service.VNPayService;
import com.example.HM.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final BookingService bookingService;

    /**
     * Test payment page (for demo purposes)
     */
    @GetMapping("/test")
    public String testPaymentPage() {
        return "payment/test";
    }

    /**
     * Create payment URL and redirect user to VNPay
     */
    @PostMapping("/create-vnpay")
    @ResponseBody
    public ResponseEntity<?> createVNPayPayment(
            @RequestParam long amount,
            @RequestParam String orderInfo,
            HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, VNPayUtil.getRandomNumber(8), request);
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * VNPay Return URL - User is redirected here after payment
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                vnpParams.put(paramName, paramValue);
            }
        }

        boolean isValid = vnPayService.verifySignature(vnpParams);
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String amount = vnpParams.get("vnp_Amount");
        String orderInfo = vnpParams.get("vnp_OrderInfo");
        String txnRef = vnpParams.get("vnp_TxnRef");

        model.addAttribute("isValid", isValid);
        model.addAttribute("responseCode", responseCode);
        model.addAttribute("transactionNo", transactionNo);
        model.addAttribute("amount", amount != null ? Long.parseLong(amount) / 100 : 0);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("txnRef", txnRef);

        if (isValid && "00".equals(responseCode)) {
            // Update Booking Status to PAID
            if (txnRef != null && txnRef.length() > 10) { // Primitive check for real booking IDs
                try {
                    bookingService.updateBookingStatus(txnRef, BookingStatus.PAID);
                } catch (Exception e) {
                    // Log error but show success if payment was valid
                }
            }
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
        } else if (isValid) {
            // Payment failed or cancelled at the gateway
            if (txnRef != null && txnRef.length() > 10) {
                try {
                    bookingService.updateBookingStatus(txnRef, BookingStatus.CANCELLED);
                } catch (Exception e) {
                    // Log error
                }
            }
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại hoặc đã bị khách hàng hủy.");
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "Chữ ký không hợp lệ.");
        }

        return "payment/result";
    }

    /**
     * VNPay IPN URL - Server-to-server notification
     */
    @GetMapping("/vnpay-ipn")
    @ResponseBody
    public ResponseEntity<?> vnpayIPN(HttpServletRequest request) {
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                vnpParams.put(paramName, paramValue);
            }
        }

        boolean isValid = vnPayService.verifySignature(vnpParams);
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String txnRef = vnpParams.get("vnp_TxnRef");

        if (!isValid) {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid signature"));
        }

        if ("00".equals(responseCode)) {
            // Payment successful - update order status
             if (txnRef != null && txnRef.length() > 10) {
                 bookingService.updateBookingStatus(txnRef, BookingStatus.PAID);
             }
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            // Payment failed or cancelled
             if (txnRef != null && txnRef.length() > 10) {
                 bookingService.updateBookingStatus(txnRef, BookingStatus.CANCELLED);
             }
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        }
    }
}
