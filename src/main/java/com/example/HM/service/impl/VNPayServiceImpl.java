package com.example.HM.service.impl;

import com.example.HM.config.VNPayConfig;
import com.example.HM.service.VNPayService;
import com.example.HM.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;

    @Override
    public String createPaymentUrl(long amount, String orderInfo, String txnRef, HttpServletRequest request) {
        String vnp_TxnRef = txnRef;
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);
        long vnp_Amount = amount * 100; // VNPay uses VND * 100

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build query string and hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    // Build hash data
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    // Build query
                    if (query.length() > 0) {
                        query.append('&');
                    }
                    query.append(fieldName);
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                    throw new RuntimeException("Error building VNPay URL", e);
                }
            }
        }

        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return vnPayConfig.getPayUrl() + "?" + query.toString();
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return false;
        }

        // Remove hash fields for verification
        Map<String, String> fieldsToHash = new HashMap<>(params);
        fieldsToHash.remove("vnp_SecureHash");
        fieldsToHash.remove("vnp_SecureHashType");

        String calculatedHash = VNPayUtil.hashAllFields(fieldsToHash, vnPayConfig.getHashSecret());
        return vnp_SecureHash.equalsIgnoreCase(calculatedHash);
    }
}
