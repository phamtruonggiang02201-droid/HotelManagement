package com.example.HM.service;

import com.example.HM.dto.CheckInDataDTO;
import com.example.HM.dto.BookedServiceDTO;
import com.example.HM.dto.BookServiceRequest;
import com.example.HM.dto.CheckoutSummaryDTO;
import com.example.HM.dto.BookingRequest;
import com.example.HM.dto.CheckInRequest;
import com.example.HM.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    CheckInDataDTO getCheckInData(String bookingId);
    Booking createBooking(BookingRequest request, String accountId);
    Booking createWalkInBooking(BookingRequest request);
    void updateBookingStatus(String bookingId, String status);
    Page<Booking> getMyBookings(String accountId, Pageable pageable);
    Page<Booking> getAllBookings(Pageable pageable);
    Booking getBookingById(String id);
    void bookService(BookServiceRequest request);
    void checkIn(CheckInRequest request);
    void checkOut(String bookingId);
    Page<Booking> getPaidBookings(Pageable pageable);
    Page<Booking> getCheckedInBookings(Pageable pageable);
    
    // Manage Booked Services
    Page<BookedServiceDTO> getAllBookedServices(String keyword, String status, Pageable pageable);
    BookedServiceDTO updateBookedServiceStatus(String id, String status);
    BookedServiceDTO updateBookedServiceQuantity(String detailId, int newQuantity);
    void rateBookedDetail(String id, int rating, String comment);
    
    // Quick Order
    void bookServices(String bookingId, String roomId, java.util.Map<String, Integer> serviceQuantities);
    Booking getActiveBookingByRoom(String roomId);
    
    // Checkout Summary
    CheckoutSummaryDTO getCheckoutSummary(String bookingId);
}
