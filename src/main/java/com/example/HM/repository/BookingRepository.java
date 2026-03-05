package com.example.HM.repository;

import com.example.HM.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Page<Booking> findByAccountId(String accountId, Pageable pageable);
    Page<Booking> findByStatus(String status, Pageable pageable);
}
