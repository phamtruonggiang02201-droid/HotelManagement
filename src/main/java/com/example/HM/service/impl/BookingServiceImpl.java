package com.example.HM.service.impl;

import com.example.HM.constant.BookingStatus;
import com.example.HM.dto.BookServiceRequest;
import com.example.HM.dto.BookingRequest;
import com.example.HM.dto.CheckInRequest;
import com.example.HM.entity.*;
import com.example.HM.repository.*;
import com.example.HM.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final AccountRepository accountRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ExtraServiceRepository extraServiceRepository;
    private final BookedServiceRepository bookedServiceRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request, String accountId) {
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));

        // Check availability: at least one room of this type is AVAILABLE
        List<Room> availableRooms = roomRepository.findByRoomTypeAndStatus(roomType, "AVAILABLE", Pageable.unpaged()).getContent();
        if (availableRooms.isEmpty()) {
            throw new RuntimeException("Loại phòng này hiện không còn phòng trống!");
        }

        // Find or Create Guest
        Guest guest = guestRepository.findByEmail(request.getGuestEmail())
                .orElseGet(() -> {
                    Guest newGuest = new Guest();
                    newGuest.setFullName(request.getGuestName());
                    newGuest.setEmail(request.getGuestEmail());
                    newGuest.setPhone(request.getGuestPhone());
                    return guestRepository.save(newGuest);
                });

        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (days <= 0) days = 1;

        BigDecimal totalAmount = roomType.getPrice().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuest(guest);
        booking.setRoomType(roomType);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        if (accountId != null) {
            Account account = accountRepository.findById(accountId).orElse(null);
            booking.setAccount(account);
        }

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void updateBookingStatus(String bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        booking.setStatus(status);

        if (BookingStatus.PAID.equals(status)) {
            booking.setPaymentDate(LocalDateTime.now());
        }

        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void checkIn(CheckInRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        if (!BookingStatus.PAID.equals(booking.getStatus())) {
            throw new RuntimeException("Chỉ có thể check-in các đơn đã thanh toán!");
        }

        Set<Room> assignedRooms = new HashSet<>();
        for (String roomId : request.getRoomIds()) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Phòng " + roomId + " không tồn tại!"));

            if (!"AVAILABLE".equals(room.getStatus())) {
                throw new RuntimeException("Phòng " + room.getRoomName() + " hiện không khả dụng!");
            }

            // Verify room belongs to the booked room type
            if (!room.getRoomType().getId().equals(booking.getRoomType().getId())) {
                throw new RuntimeException("Phòng " + room.getRoomName() + " không thuộc loại phòng đã đặt!");
            }

            room.setStatus("OCCUPIED");
            roomRepository.save(room);
            assignedRooms.add(room);
        }

        booking.setRooms(assignedRooms);
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void checkOut(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        if (!BookingStatus.CHECKED_IN.equals(booking.getStatus())) {
            throw new RuntimeException("Chỉ có thể check-out các đơn đã check-in!");
        }

        // Release all assigned rooms
        if (booking.getRooms() != null) {
            booking.getRooms().forEach(room -> {
                room.setStatus("AVAILABLE");
                roomRepository.save(room);
            });
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
    }

    @Override
    public Page<Booking> getMyBookings(String accountId, Pageable pageable) {
        return bookingRepository.findByAccountId(accountId, pageable);
    }

    @Override
    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Override
    public Booking getBookingById(String id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Booking> getPaidBookings(Pageable pageable) {
        return bookingRepository.findByStatus(BookingStatus.PAID, pageable);
    }

    @Override
    public Page<Booking> getCheckedInBookings(Pageable pageable) {
        return bookingRepository.findByStatus(BookingStatus.CHECKED_IN, pageable);
    }

    @Override
    @Transactional
    public void bookService(BookServiceRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        ExtraService service = extraServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));

        BookedService bookedService = new BookedService();
        bookedService.setBooking(booking);
        bookedService.setService(service);
        bookedService.setQuantity(request.getQuantity());
        bookedService.setUnitPrice(service.getPrice());
        bookedService.setStatus("ORDERED");

        bookedServiceRepository.save(bookedService);

        BigDecimal serviceTotal = service.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        booking.setTotalAmount(booking.getTotalAmount().add(serviceTotal));
        bookingRepository.save(booking);
    }
}
