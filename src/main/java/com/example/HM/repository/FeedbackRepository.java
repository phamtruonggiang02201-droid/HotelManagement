package com.example.HM.repository;

import com.example.HM.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Feedback> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
}
