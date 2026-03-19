package com.example.HM.repository;

import com.example.HM.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    @Query("SELECT p FROM Payment p WHERE p.booking.account.id = :accountId ORDER BY p.paymentDate DESC")
    Page<Payment> findByAccountId(String accountId, Pageable pageable);
    
    Page<Payment> findAllByOrderByPaymentDateDesc(Pageable pageable);

    java.util.Optional<Payment> findTopByBookingIdAndPaymentStatusOrderByPaymentDateDesc(String bookingId, String paymentStatus);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.paymentStatus = 'SUCCESS'")
    Object sumRevenueBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT CAST(p.paymentDate AS date) as date, SUM(p.amount) as amount FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.paymentStatus = 'SUCCESS' " +
           "GROUP BY CAST(p.paymentDate AS date) ORDER BY CAST(p.paymentDate AS date)")
    List<Object[]> getDailyRevenueBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
