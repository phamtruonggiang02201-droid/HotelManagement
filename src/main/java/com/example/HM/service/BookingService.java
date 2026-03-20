package com.example.HM.service;

import com.example.HM.dto.BookedServiceDTO;
import com.example.HM.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface BookingService {
    Page<Booking> getAllBookings(Pageable pageable);
    Page<BookedServiceDTO> getAllBookedServices(String keyword, String status, Pageable pageable);
    // Export
    ByteArrayInputStream exportBookingsToExcel(java.time.LocalDate startDate, java.time.LocalDate endDate);
    ByteArrayInputStream exportBookedServicesToExcel(String keyword, String status);

}
