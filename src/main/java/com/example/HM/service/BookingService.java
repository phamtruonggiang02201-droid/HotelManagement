package com.example.HM.service;

import com.example.HM.dto.BookServiceRequest;
import com.example.HM.dto.BookingRequest;
import com.example.HM.dto.CheckInRequest;
import com.example.HM.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingRequest request, String accountId);
    void updateBookingStatus(String bookingId, String status);
    Page<Booking> getMyBookings(String accountId, Pageable pageable);
    Page<Booking> getAllBookings(Pageable pageable);
    Booking getBookingById(String id);
    void bookService(BookServiceRequest request);
    void checkIn(CheckInRequest request);
    void checkOut(String bookingId);
    Page<Booking> getPaidBookings(Pageable pageable);
    Page<Booking> getCheckedInBookings(Pageable pageable);
}
