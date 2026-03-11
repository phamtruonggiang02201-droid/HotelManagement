package com.example.HM.service.impl;

import com.example.HM.constant.BookingStatus;
import com.example.HM.dto.*;
import com.example.HM.entity.*;
import com.example.HM.repository.*;
import com.example.HM.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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

        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckIn())) {
            throw new RuntimeException("Chưa đến ngày nhận phòng! (Ngày đặt: " + booking.getCheckIn() + ")");
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
        
        // Update Guest info if provided during check-in
        Guest guest = booking.getGuest();
        if (guest != null) {
            if (request.getGuestIdCard() != null && !request.getGuestIdCard().isEmpty()) {
                guest.setIdNumber(request.getGuestIdCard());
            }
            if (request.getGuestFullName() != null && !request.getGuestFullName().isEmpty()) {
                guest.setFullName(request.getGuestFullName());
            }
            guestRepository.save(guest);
        }

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

        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckOut())) {
            throw new RuntimeException("Chưa đến ngày trả phòng theo lịch hẹn! (Ngày hẹn: " + booking.getCheckOut() + ")");
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
        return bookingRepository.findByAccount_Id(accountId, pageable);
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

        BigDecimal currentTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal serviceTotal = service.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        booking.setTotalAmount(currentTotal.add(serviceTotal));
        bookingRepository.save(booking);
    }

    @Override
    public Page<BookedServiceDTO> getAllBookedServices(String keyword, String status, Pageable pageable) {
        Page<BookedService> bookedPage;
        if (status != null && !status.isEmpty()) {
            bookedPage = bookedServiceRepository.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            bookedPage = bookedServiceRepository.searchBookedServices(keyword, pageable);
        } else {
            bookedPage = bookedServiceRepository.findAll(pageable);
        }
        return bookedPage.map(this::convertToBookedServiceDTO);
    }

    @Override
    @Transactional
    public BookedServiceDTO updateBookedServiceStatus(String id, String status) {
        BookedService bookedService = bookedServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn dịch vụ không tồn tại!"));
        
        String oldStatus = bookedService.getStatus();
        
        // Nếu chuyển sang CANCELLED thì trừ tiền trong Booking
        if ("CANCELLED".equals(status) && !"CANCELLED".equals(oldStatus)) {
            Booking booking = bookedService.getBooking();
            BigDecimal serviceTotal = bookedService.getUnitPrice().multiply(BigDecimal.valueOf(bookedService.getQuantity()));
            BigDecimal currentTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            booking.setTotalAmount(currentTotal.subtract(serviceTotal));
            bookingRepository.save(booking);
        }
        
        bookedService.setStatus(status);
        return convertToBookedServiceDTO(bookedServiceRepository.save(bookedService));
    }

    @Override
    @Transactional
    public BookedServiceDTO updateBookedServiceQuantity(String id, int newQuantity) {
        if (newQuantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }

        BookedService bookedService = bookedServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn dịch vụ không tồn tại!"));

        if (!"ORDERED".equals(bookedService.getStatus())) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa đơn hàng ở trạng thái 'Mới đặt'!");
        }

        int oldQuantity = bookedService.getQuantity();
        BigDecimal unitPrice = bookedService.getUnitPrice();
        
        // Calculate difference to update Booking totalAmount
        BigDecimal diffQuantity = BigDecimal.valueOf(newQuantity - oldQuantity);
        BigDecimal amountDiff = unitPrice.multiply(diffQuantity);

        // Update BookedService
        bookedService.setQuantity(newQuantity);
        bookedServiceRepository.save(bookedService);

        // Update Booking totalAmount
        Booking booking = bookedService.getBooking();
        BigDecimal currentTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        booking.setTotalAmount(currentTotal.add(amountDiff));
        bookingRepository.save(booking);

        return convertToBookedServiceDTO(bookedService);
    }

    @Override
    public CheckoutSummaryDTO getCheckoutSummary(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckOut())) {
            throw new RuntimeException("Chưa đến ngày trả phòng theo lịch hẹn! (Ngày hẹn: " + booking.getCheckOut() + ")");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        if (nights <= 0) nights = 1; // Tính tối thiểu 1 đêm

        BigDecimal roomPrice = booking.getRoomType().getPrice();
        BigDecimal totalRoomPrice = roomPrice.multiply(BigDecimal.valueOf(nights));

        List<BookedServiceDTO> serviceDTOs = booking.getBookedServices().stream()
                .map(this::convertToBookedServiceDTO)
                .collect(Collectors.toList());

        BigDecimal totalServiceAmount = serviceDTOs.stream()
                .map(BookedServiceDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = totalRoomPrice.add(totalServiceAmount);
        
        // Coi như tiền phòng đã được thanh toán nếu booking status là PAID hoặc CHECKED_IN (đã qua cổng VNPay)
        BigDecimal paidAmount = (booking.getStatus().equals(BookingStatus.PAID) || 
                                 booking.getStatus().equals(BookingStatus.CHECKED_IN)) 
                                 ? totalRoomPrice : BigDecimal.ZERO;

        String roomNames = booking.getRooms() != null ? 
                booking.getRooms().stream().map(Room::getRoomName).collect(Collectors.joining(", ")) : "";

        return CheckoutSummaryDTO.builder()
                .bookingId(booking.getId())
                .guestName(booking.getGuest() != null ? booking.getGuest().getFullName() : "Khách vãng lai")
                .roomTypeName(booking.getRoomType().getTypeName())
                .roomNames(roomNames)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .totalNights(nights)
                .roomPricePerNight(roomPrice)
                .totalRoomPrice(totalRoomPrice)
                .services(serviceDTOs)
                .totalServiceAmount(totalServiceAmount)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .balance(totalAmount.subtract(paidAmount))
                .build();
    }

    private BookedServiceDTO convertToBookedServiceDTO(BookedService bs) {
        String roomNames = "";
        if (bs.getBooking().getRooms() != null) {
            roomNames = bs.getBooking().getRooms().stream()
                    .map(Room::getRoomName)
                    .collect(Collectors.joining(", "));
        }
        
        BigDecimal quantityBD = BigDecimal.valueOf(bs.getQuantity());
        BigDecimal total = bs.getUnitPrice().multiply(quantityBD);

        return BookedServiceDTO.builder()
                .id(bs.getId())
                .bookingId(bs.getBooking().getId())
                .guestName(bs.getBooking().getGuest() != null ? bs.getBooking().getGuest().getFullName() : "Khách vãng lai")
                .roomNames(roomNames)
                .serviceName(bs.getService().getServiceName())
                .quantity(bs.getQuantity())
                .unitPrice(bs.getUnitPrice())
                .totalAmount(total)
                .status(bs.getStatus())
                .createdAt(bs.getCreatedAt())
                .build();
    }
}
