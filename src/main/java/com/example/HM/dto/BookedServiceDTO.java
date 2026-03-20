package com.example.HM.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedServiceDTO {
    private String id;
    private String bookingId;
    private String guestName;
    private String roomId;
    private String roomName;
    private String serviceName;
    private String status; // Trạng thái của detail (PENDING, PROCESSING, COMPLETED)
    private String staffName; // Tên nhân viên đang xử lý
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<BookedDetailDTO> details;
}
