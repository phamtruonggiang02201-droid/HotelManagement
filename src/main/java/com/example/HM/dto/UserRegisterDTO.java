package com.example.HM.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    private String phone;
}
