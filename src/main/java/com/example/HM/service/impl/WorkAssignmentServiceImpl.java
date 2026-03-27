package com.example.HM.service.impl;

import com.example.HM.dto.AssignmentRequest;
import com.example.HM.dto.AssignmentResponseDTO;
import com.example.HM.entity.Account;
import com.example.HM.entity.WorkAssignment;
import com.example.HM.repository.AccountRepository;
import com.example.HM.repository.WorkAssignmentRepository;
import com.example.HM.security.CustomUserDetails;
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
        System.out.println("BE: assignWork started for employeeId: " + request.getEmployeeId() + " on date: " + request.getWorkDate());
        Account employee = accountRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại!"));

        if (!employee.getStatus()) {
            throw new RuntimeException("Không thể gán lịch cho tài khoản đã bị xóa/vô hiệu hóa!");
        }

        LocalDate workDate = LocalDate.parse(request.getWorkDate());
        System.out.println("BE: Parsed workDate: " + workDate);
        
        // CĐC: Nếu đã có lịch trực trong ngày này, thì cập nhật thay vì báo lỗi
        List<WorkAssignment> existing = assignmentRepository.findAllByEmployee_IdAndWorkDate(employee.getId(), workDate);
        System.out.println("BE: Found existing assignments: " + existing.size());
        
        WorkAssignment assignment;
        if (!existing.isEmpty()) {
            assignment = existing.get(0);
            System.out.println("BE: Updating existing assignment ID: " + assignment.getId());
        } else {
            assignment = new WorkAssignment();
            assignment.setEmployee(employee);
            assignment.setWorkDate(workDate);
            assignment.setType("SCHEDULE");
            assignment.setStatus("PENDING");
            System.out.println("BE: Creating new assignment");
        }
        
        assignment.setArea(request.getArea());
        assignment.setShift(request.getShift());
        assignment.setNotes(request.getNotes());

        try {
            WorkAssignment saved = assignmentRepository.save(assignment);
            System.out.println("BE: Assignment saved successfully. ID: " + saved.getId());
            return convertToDTO(saved);
        } catch (Exception e) {
            System.err.println("BE ERROR: Failed to save assignment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
        CustomUserDetails details = SecurityUtils.getCurrentUserDetails();
        if (details == null) return Page.empty();

        // Kiểm tra role: Nếu là ADMIN hoặc MANAGER thì lấy tất cả, ngược lại chỉ lấy của mình
        boolean isPrivileged = details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        if (isPrivileged) {
            return assignmentRepository.findAllByWorkDateBetween(start, end, pageable)
                    .map(this::convertToDTO);
        } else {
            return assignmentRepository.findAllByEmployee_IdAndWorkDateBetween(details.getId(), start, end, pageable)
                    .map(this::convertToDTO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getMyAssignments() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return List.of();
        
        return assignmentRepository.findAllByEmployee_Id(userId).stream()
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
            // Tạm thời gán Ca hiện tại dựa trên giờ hệ thống để dễ hiển thị trên bảng
            int hour = java.time.LocalTime.now().getHour();
            String currentShift = "Sáng";
            if (hour >= 12 && hour < 18) currentShift = "Chiều";
            else if (hour >= 18 || hour < 6) currentShift = "Tối";
            task.setShift(currentShift);
        }

        assignmentRepository.save(task);
    }

    @Override
    @Transactional
    public AssignmentResponseDTO assignTask(String taskId, String employeeId, String shift) {
        WorkAssignment task = assignmentRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhiệm vụ!"));
        Account employee = accountRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        
        if (!employee.getStatus()) {
            throw new RuntimeException("Không thể gán nhiệm vụ cho tài khoản đã bị xóa/vô hiệu hóa!");
        }

        task.setEmployee(employee);
        task.setShift(shift);
        task.setStatus("PROCESSING");
        return convertToDTO(assignmentRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getTasksInPool(String roleName) {
        // Lấy danh sách nhiệm vụ của bộ phận chưa có người nhận trong ngày
        return assignmentRepository.findAllByTypeAndStatus("TASK", "PENDING").stream()
                .filter(t -> t.getEmployee() == null && t.getWorkDate().equals(LocalDate.now()))
                .map(this::convertToDTO)
                .toList();
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

    @Override
    @Transactional
    public int applyWeek(LocalDate sourceDate) {
        System.out.println("BE: applyWeek started for sourceDate: " + sourceDate);
        List<WorkAssignment> sourceAssignments = assignmentRepository.findAllByWorkDate(sourceDate);
        if (sourceAssignments.isEmpty()) {
            System.out.println("BE: No source assignments found for " + sourceDate);
            return 0;
        }

        int dayOfWeek = sourceDate.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)
        int daysToCopy = 7 - dayOfWeek;
        System.out.println("BE: Days to copy: " + daysToCopy);

        if (daysToCopy <= 0) {
            return 0;
        }

        int totalApplied = 0;
        for (int i = 1; i <= daysToCopy; i++) {
            LocalDate targetDate = sourceDate.plusDays(i);
            System.out.println("BE: Copying to " + targetDate);
            
            for (WorkAssignment sourceAs : sourceAssignments) {
                if (sourceAs.getEmployee() == null || !sourceAs.getEmployee().getStatus()) continue;

                List<WorkAssignment> existing = assignmentRepository.findAllByEmployee_IdAndWorkDate(
                        sourceAs.getEmployee().getId(), targetDate);
                
                WorkAssignment targetAs;
                if (!existing.isEmpty()) {
                    targetAs = existing.get(0);
                } else {
                    targetAs = new WorkAssignment();
                    targetAs.setEmployee(sourceAs.getEmployee());
                    targetAs.setWorkDate(targetDate);
                    targetAs.setType("SCHEDULE");
                    targetAs.setStatus("PENDING");
                }
                
                targetAs.setArea(sourceAs.getArea());
                targetAs.setShift(sourceAs.getShift());
                targetAs.setNotes(sourceAs.getNotes());
                
                assignmentRepository.save(targetAs);
                totalApplied++;
            }
        }
        
        System.out.println("BE: Total assignments applied to week: " + totalApplied);
        return totalApplied;
    }

    @Override
    @Transactional
    public int copyToNextDay(LocalDate sourceDate) {
        System.out.println("BE: copyToNextDay started for sourceDate: " + sourceDate);
        List<WorkAssignment> sourceAssignments = assignmentRepository.findAllByWorkDate(sourceDate);
        if (sourceAssignments.isEmpty()) {
            return 0;
        }

        LocalDate targetDate = sourceDate.plusDays(1);
        int totalApplied = 0;
        
        for (WorkAssignment sourceAs : sourceAssignments) {
            if (sourceAs.getEmployee() == null || !sourceAs.getEmployee().getStatus()) continue;

            List<WorkAssignment> existing = assignmentRepository.findAllByEmployee_IdAndWorkDate(
                    sourceAs.getEmployee().getId(), targetDate);
            
            WorkAssignment targetAs;
            if (!existing.isEmpty()) {
                targetAs = existing.get(0);
            } else {
                targetAs = new WorkAssignment();
                targetAs.setEmployee(sourceAs.getEmployee());
                targetAs.setWorkDate(targetDate);
                targetAs.setType("SCHEDULE");
                targetAs.setStatus("PENDING");
            }
            
            targetAs.setArea(sourceAs.getArea());
            targetAs.setShift(sourceAs.getShift());
            targetAs.setNotes(sourceAs.getNotes());
            
            assignmentRepository.save(targetAs);
            totalApplied++;
        }
        return totalApplied;
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
