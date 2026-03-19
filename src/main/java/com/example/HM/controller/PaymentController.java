package com.example.HM.controller;

import com.example.HM.constant.BookingStatus;
import com.example.HM.service.BookingService;
import com.example.HM.service.VNPayService;
import com.example.HM.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.HM.dto.PaymentDTO;
import com.example.HM.security.SecurityUtils;
import com.example.HM.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final VNPayService vnPayService;
    private final BookingService bookingService;
    private final com.example.HM.service.PaymentService paymentService;

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
                    bookingService.updateBookingStatus(txnRef, BookingStatus.PAID, transactionNo);
                } catch (Exception e) {
                    log.error("[VNPay Return] Thanh toán thành công nhưng KHÔNG update được booking {}: {}", txnRef, e.getMessage(), e);
                }
            }
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
        } else if (isValid) {
            // Payment failed or cancelled at the gateway
            if (txnRef != null && txnRef.length() > 10) {
                try {
                    bookingService.updateBookingStatus(txnRef, BookingStatus.CANCELLED, null);
                } catch (Exception e) {
                    log.error("[VNPay Return] Thanh toán thất bại, KHÔNG hủy được booking {}: {}", txnRef, e.getMessage(), e);
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
                String transactionNo = vnpParams.get("vnp_TransactionNo");
                bookingService.updateBookingStatus(txnRef, BookingStatus.PAID, transactionNo);
            }
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            // Payment failed or cancelled
            if (txnRef != null && txnRef.length() > 10) {
                bookingService.updateBookingStatus(txnRef, BookingStatus.CANCELLED, null);
            }
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        }
    }
    /**
     * View current user's payment history
     */
    @GetMapping("/history")
    public String myPaymentHistory(Model model, @PageableDefault(size = 10) Pageable pageable) {
        String accountId = SecurityUtils.getCurrentUserId();
        if (accountId == null) return "redirect:/login";
        
        Page<PaymentDTO> payments = paymentService.getMyPayments(accountId, pageable);
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("currentPage", payments.getNumber());
        model.addAttribute("totalPages", payments.getTotalPages());
        return "payment/my-history";
    }

    /**
     * View all payment history (Admin/Manager/Receptionist)
     */
    @GetMapping("/admin/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public String adminPaymentHistory(Model model, @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getAllPayments(pageable);
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("currentPage", payments.getNumber());
        model.addAttribute("totalPages", payments.getTotalPages());
        return "admin/payment-history";
    }
}
