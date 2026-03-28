package com.example.HM.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountDTO {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatar;
    private String roleName;
    private String roleDescription;
    private Boolean status;
    private String idNumber;
    private String idType;
    private String nationality;
    private String phone;
    private Boolean emailVerified;
    private String jobTitle;
    private LocalDateTime createdAt;
}
