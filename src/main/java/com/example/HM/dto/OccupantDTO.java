package com.example.HM.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupantDTO {
    private String roomId;
    private String roomName;
    private String fullName;
    private String idNumber;
    private String phone;
}
