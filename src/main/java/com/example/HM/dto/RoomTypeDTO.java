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
public class RoomTypeDTO {
    private String id;
    private String typeName;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private String roomImage;
    private long availableCount; // number of rooms available for this type
}
