package com.example.HM.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceRequest {
    private String serviceName;
    private BigDecimal price;
    private String categoryId;
}
