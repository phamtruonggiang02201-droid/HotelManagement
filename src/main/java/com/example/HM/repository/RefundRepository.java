package com.example.HM.repository;

import com.example.HM.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {
    Page<Refund> findAllByOrderByRequestedAtDesc(Pageable pageable);
    Optional<Refund> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
    Page<Refund> findByStatus(String status, Pageable pageable);
}
