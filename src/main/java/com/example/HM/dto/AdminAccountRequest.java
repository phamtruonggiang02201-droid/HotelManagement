package com.example.HM.dto;

import lombok.Data;

@Data
public class AdminAccountRequest {
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String roleName;
    private String jobTitle;
    private String phone;
    private String dob; // yyyy-MM-dd
    private String address;
    private String idNumber;
    private String idType;
    private String nationality;
    private Boolean status;
}
