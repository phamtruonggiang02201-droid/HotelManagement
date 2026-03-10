package com.example.HM.repository;

import com.example.HM.entity.Booking;
import com.example.HM.entity.BookedService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookedServiceRepository extends JpaRepository<BookedService, String> {
    List<BookedService> findByBooking(Booking booking);

    @Query("SELECT bs FROM BookedService bs JOIN bs.booking b JOIN b.guest g " +
           "WHERE LOWER(g.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(bs.service.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BookedService> searchBookedServices(@Param("keyword") String keyword, Pageable pageable);

    Page<BookedService> findByStatus(String status, Pageable pageable);
}
