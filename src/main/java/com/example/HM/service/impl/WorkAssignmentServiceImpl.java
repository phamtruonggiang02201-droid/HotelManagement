package com.example.HM.service.impl;

import com.example.HM.dto.AssignmentRequest;
import com.example.HM.dto.AssignmentResponseDTO;
import com.example.HM.entity.Account;
import com.example.HM.entity.WorkAssignment;
import com.example.HM.repository.AccountRepository;
import com.example.HM.repository.WorkAssignmentRepository;
import com.example.HM.security.SecurityUtils;
import com.example.HM.service.WorkAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkAssignmentServiceImpl implements WorkAssignmentService {

    private final WorkAssignmentRepository assignmentRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AssignmentResponseDTO assignWork(AssignmentRequest request) {
        Account employee = accountRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại!"));

        LocalDate workDate = LocalDate.parse(request.getWorkDate());
        
        // Kiểm tra xem nhân viên đã có lịch trực nào trong ngày này chưa
        List<WorkAssignment> existing = assignmentRepository.findAllByEmployee_IdAndWorkDate(employee.getId(), workDate);
        if (!existing.isEmpty()) {
            WorkAssignment current = existing.get(0);
            throw new RuntimeException("Nhân viên " + employee.getFirstName() + " " + employee.getLastName() + 
                " đã có lịch trực (" + current.getShift() + " tại " + current.getArea() + ") vào ngày này!");
        }

        WorkAssignment assignment = new WorkAssignment();
        assignment.setEmployee(employee);
        assignment.setWorkDate(workDate);
        assignment.setArea(request.getArea());
        assignment.setShift(request.getShift());
        assignment.setNotes(request.getNotes());
        assignment.setStatus("PENDING");

        return convertToDTO(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void updateAssignment(String id, AssignmentRequest request) {
        WorkAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phân công không tồn tại!"));
        
        assignment.setArea(request.getArea());
        assignment.setShift(request.getShift());
        assignment.setNotes(request.getNotes());
        if (request.getWorkDate() != null) {
            assignment.setWorkDate(LocalDate.parse(request.getWorkDate()));
        }
        
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(String id) {
        assignmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(String id, String status) {
        WorkAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phân công không tồn tại!"));
        assignment.setStatus(status);
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponseDTO> getAssignments(LocalDate start, LocalDate end, Pageable pageable) {
        return assignmentRepository.findAllByWorkDateBetween(start, end, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getMyAssignments() {
        String username = SecurityUtils.getCurrentUsername();
        Account current = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        
        return assignmentRepository.findAllByEmployee_Id(current.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getAssignmentsByDate(LocalDate date) {
        return assignmentRepository.findAllByWorkDate(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AssignmentResponseDTO convertToDTO(WorkAssignment assignment) {
        return AssignmentResponseDTO.builder()
                .id(assignment.getId())
                .employeeId(assignment.getEmployee().getId())
                .employeeFullName(assignment.getEmployee().getFirstName() + " " + assignment.getEmployee().getLastName())
                .jobTitle(assignment.getEmployee().getJobTitle())
                .workDate(assignment.getWorkDate())
                .area(assignment.getArea())
                .shift(assignment.getShift())
                .status(assignment.getStatus())
                .notes(assignment.getNotes())
                .build();
    }
}
