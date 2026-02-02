package com.example.HM.dto;

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
    private BigDecimal price;
    private String roomImage;
    private RoomTypeDTO roomType;
}
