package com.example.HM.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatsDTO {
    private long totalRooms;
    private long availableRooms;
    private long occupiedRooms;
    private long maintenanceRooms;
}
