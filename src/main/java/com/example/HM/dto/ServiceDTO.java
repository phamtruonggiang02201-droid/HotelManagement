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
public class ServiceDTO {
    private String id;
    private String serviceName;
    private BigDecimal price;
    private String categoryId;
    private String categoryName;
    private Boolean isActive;
}
