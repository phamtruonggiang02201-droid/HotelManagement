package com.example.HM.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignmentRequest {
    private String employeeId;
    private String workDate; // yyyy-MM-dd
    private String area;
    private String shift;
    private String notes;
}
