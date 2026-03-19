package com.example.HM.repository;

import com.example.HM.entity.WorkAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkAssignmentRepository extends JpaRepository<WorkAssignment, String> {
    
    List<WorkAssignment> findAllByWorkDate(LocalDate date);
    
    List<WorkAssignment> findAllByEmployee_IdAndWorkDate(String employeeId, LocalDate date);
    
    Page<WorkAssignment> findAllByWorkDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    
    List<WorkAssignment> findAllByEmployee_Id(String employeeId);

    List<WorkAssignment> findAllByTypeAndStatus(String type, String status);

    List<WorkAssignment> findAllByEmployee_Role_RoleNameAndWorkDateAndType(String roleName, LocalDate date, String type);

    java.util.Optional<WorkAssignment> findByTargetId(String targetId);
}
