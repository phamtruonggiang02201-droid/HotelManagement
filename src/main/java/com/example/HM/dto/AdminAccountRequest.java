package com.example.HM.dto;

import lombok.Data;

@Data
public class AdminAccountRequest {
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String roleName;
}
