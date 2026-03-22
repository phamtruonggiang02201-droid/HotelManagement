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

    @Query("SELECT DISTINCT bs FROM BookedService bs JOIN bs.booking b JOIN b.guest g LEFT JOIN bs.details d LEFT JOIN d.service s " +
           "WHERE LOWER(g.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BookedService> searchBookedServices(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT bs FROM BookedService bs JOIN bs.booking b JOIN b.guest g LEFT JOIN bs.details d LEFT JOIN d.service s " +
           "WHERE LOWER(g.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BookedService> searchBookedServicesList(@Param("keyword") String keyword);

    Page<BookedService> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT bs FROM BookedService bs WHERE bs.status = :status")
    List<BookedService> findByStatusAll(@Param("status") String status);

}
