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
    
    // Nhiệm vụ (TASK) nghiệp vụ
    List<AssignmentResponseDTO> getTasksInPool(String roleName); // Lấy danh sách việc chưa ai nhận của bộ phận
    List<AssignmentResponseDTO> getMyActiveTasks(); // Lấy việc mình đang làm
    void claimTask(String taskId); // Nhận việc từ pool
    AssignmentResponseDTO assignTask(String taskId, String employeeId, String shift); // Manager gán việc
    void completeTask(String taskId); // Báo hoàn thành việc

    void createTaskFromService(String bookedDetailId, String serviceName, String roomName, String guestName, String requiredRole);
    
    // Gán lịch hàng loạt
    int applyWeek(LocalDate sourceDate);
    int copyToNextDay(LocalDate sourceDate);
}
