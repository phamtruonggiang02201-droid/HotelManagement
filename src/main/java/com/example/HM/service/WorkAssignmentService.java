package com.example.HM.service;

import com.example.HM.dto.AssignmentRequest;
import com.example.HM.dto.AssignmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WorkAssignmentService {
    AssignmentResponseDTO assignWork(AssignmentRequest request);
    void updateAssignment(String id, AssignmentRequest request);
    void deleteAssignment(String id);
    void updateStatus(String id, String status);
    
    Page<AssignmentResponseDTO> getAssignments(LocalDate start, LocalDate end, Pageable pageable);
    List<AssignmentResponseDTO> getMyAssignments();
    List<AssignmentResponseDTO> getAssignmentsByDate(LocalDate date);
}
