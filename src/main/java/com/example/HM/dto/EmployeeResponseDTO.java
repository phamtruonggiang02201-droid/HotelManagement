package com.example.HM.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponseDTO {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String jobTitle;
    private String roleName;
    private Boolean status;
    private LocalDateTime createdAt;
}
