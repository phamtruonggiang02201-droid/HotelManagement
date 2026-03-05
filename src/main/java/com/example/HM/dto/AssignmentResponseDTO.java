package com.example.HM.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AssignmentResponseDTO {
    private String id;
    private String employeeId;
    private String employeeFullName;
    private String jobTitle;
    private LocalDate workDate;
    private String area;
    private String shift;
    private String status;
    private String notes;
}
