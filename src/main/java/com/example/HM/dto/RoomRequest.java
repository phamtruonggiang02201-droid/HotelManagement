package com.example.HM.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RoomRequest {
    private String roomName;
    private String roomTypeId;
    @PositiveOrZero(message = "Giá phòng phải >= 0")
    private BigDecimal price;
    private String status;
    private String roomImage;
}
