package com.example.HM.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String idNumber;
    private String idType;
    private String nationality;
    private String phone;
}
