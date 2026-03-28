package com.example.HM.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumerationDTO {

    private String id;
    private String enumCode;
    private String enumName;
    private String enumTypeId;
    private Integer sequenceNum;
    private String description;
}
