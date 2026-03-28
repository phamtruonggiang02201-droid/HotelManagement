package com.example.HM.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private String id;
    private String roomName;
    private String status;
    @PositiveOrZero(message = "Giá phòng phải >= 0")
    private BigDecimal price;
    private String roomImage;
    private RoomTypeDTO roomType;
    private String areaId;
    private String areaName;
}
