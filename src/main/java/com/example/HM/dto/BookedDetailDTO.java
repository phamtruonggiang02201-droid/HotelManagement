package com.example.HM.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedDetailDTO {
    private String id;
    private String serviceId;
    private String serviceName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private Integer rating;
    private String ratingComment;
    private String status; // Trạng thái xử lý: PENDING, PROCESSING, COMPLETED
    private String staffName; // Tên nhân viên phục vụ
    private String assignmentId; // ID của WorkAssignment liên quan
    private LocalDateTime ratedAt;
}
