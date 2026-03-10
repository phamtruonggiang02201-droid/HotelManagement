package com.example.HM.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookServiceRequest {
    private String bookingId;
    private String serviceId;
    private Integer quantity;
}
