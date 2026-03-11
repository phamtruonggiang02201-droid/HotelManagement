package com.example.HM.repository;

import com.example.HM.entity.RoomIssueReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomIssueReportRepository extends JpaRepository<RoomIssueReport, String> {
    Page<RoomIssueReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<RoomIssueReport> findByBookingId(String bookingId);
    Page<RoomIssueReport> findByStatus(String status, Pageable pageable);
}
