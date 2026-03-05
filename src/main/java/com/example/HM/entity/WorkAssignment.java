package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "WorkAssignments")
@Getter
@Setter
public class WorkAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Account employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "area", length = 100, nullable = false)
    private String area; // Ví dụ: Sảnh, Tầng 1, Nhà hàng...

    @Column(name = "shift", length = 20)
    private String shift; // Sáng, Chiều, Tối

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, COMPLETED, CANCELLED

    @Column(name = "notes", length = 500)
    private String notes;
}
