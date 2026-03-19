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

    @Override
    @Transactional
    public void createTaskFromService(String bookedDetailId, String serviceName, String roomName, String guestName, String requiredRole) {
        LocalDate today = LocalDate.now();
        
        // 1. Tìm những nhân viên có lịch trực (SCHEDULE) hôm nay và có Role phù hợp
        List<WorkAssignment> onDutyStaff = assignmentRepository.findAllByEmployee_Role_RoleNameAndWorkDateAndType(
                requiredRole, today, "SCHEDULE");

        WorkAssignment task = new WorkAssignment();
        task.setType("TASK");
        task.setTargetId(bookedDetailId);
        task.setWorkDate(today);
        task.setArea(roomName); // Khu vực là phòng khách đang ở
        task.setRoomName(roomName);
        task.setGuestName(guestName);
        task.setNotes("Phục vụ dịch vụ: " + serviceName);
        task.setStatus("PENDING");

        // 2. Nếu có nhân viên đang trực, có thể gán cho người đầu tiên hoặc để null cho "Task Pool"
        // Ở đây em chọn để null nếu muốn dùng cơ chế "Nhận việc", hoặc gán nếu đại ca muốn chỉ định
        if (!onDutyStaff.isEmpty()) {
            // Tạm thời để null để nhân viên tự nhận trong pool của họ
            // Hoặc gán: task.setEmployee(onDutyStaff.get(0).getEmployee());
        }

        assignmentRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getTasksInPool(String roleName) {
        // Lấy danh sách nhiệm vụ của bộ phận chưa có người nhận
        return assignmentRepository.findAllByTypeAndStatus("TASK", "PENDING").stream()
                .filter(t -> {
                    // Cần kiểm tra xem targetId có thuộc về category yêu cầu role này không
                    // Ở đây em đơn giản hóa: Nếu task không có employee và đúng ngày thì hiện
                    return t.getEmployee() == null && t.getWorkDate().equals(LocalDate.now());
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getMyActiveTasks() {
        String username = SecurityUtils.getCurrentUsername();
        Account current = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        
        return assignmentRepository.findAllByEmployee_Id(current.getId()).stream()
                .filter(t -> "TASK".equals(t.getType()) && ("PENDING".equals(t.getStatus()) || "PROCESSING".equals(t.getStatus())))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void claimTask(String taskId) {
        String username = SecurityUtils.getCurrentUsername();
        Account current = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        WorkAssignment task = assignmentRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Nhiệm vụ không tồn tại!"));

        if (task.getEmployee() != null) {
            throw new RuntimeException("Nhiệm vụ này đã có người nhận!");
        }

        // Kiểm tra xem nhân viên có đang trong ca trực không
        List<WorkAssignment> schedules = assignmentRepository.findAllByEmployee_IdAndWorkDate(current.getId(), LocalDate.now());
        if (schedules.isEmpty()) {
            throw new RuntimeException("Đại ca chưa có lịch trực hôm nay, không nhận việc được đâu ạ!");
        }

        task.setEmployee(current);
        task.setStatus("PROCESSING");
        assignmentRepository.save(task);
    }

    @Override
    @Transactional
    public void completeTask(String taskId) {
        WorkAssignment task = assignmentRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Nhiệm vụ không tồn tại!"));
        
        task.setStatus("COMPLETED");
        assignmentRepository.save(task);

        // Nếu là nhiệm vụ dịch vụ, cập nhật trạng thái đơn dịch vụ (BookedDetail -> BookedService)
        // Lưu ý: Cần thêm logic báo cho Lễ tân hoặc cập nhật đơn gốc
    }

    private AssignmentResponseDTO convertToDTO(WorkAssignment assignment) {
        return AssignmentResponseDTO.builder()
                .id(assignment.getId())
                .employeeId(assignment.getEmployee() != null ? assignment.getEmployee().getId() : null)
                .employeeFullName(assignment.getEmployee() != null ? 
                        assignment.getEmployee().getFirstName() + " " + assignment.getEmployee().getLastName() : "Chưa phân công")
                .jobTitle(assignment.getEmployee() != null ? assignment.getEmployee().getJobTitle() : null)
                .workDate(assignment.getWorkDate())
                .area(assignment.getArea())
                .shift(assignment.getShift())
                .status(assignment.getStatus())
                .notes(assignment.getNotes())
                .build();
    }
}
