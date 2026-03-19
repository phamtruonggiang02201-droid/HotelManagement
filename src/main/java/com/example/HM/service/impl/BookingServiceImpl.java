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
import java.time.temporal.ChronoUnit;

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
    private final BookedRoomRepository bookedRoomRepository;
    private final OccupantRepository occupantRepository;
    private final BookedDetailRepository bookedDetailRepository;
    private final PaymentRepository paymentRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final com.example.HM.service.WorkAssignmentService workAssignmentService;

    @Override
    public CheckInDataDTO getCheckInData(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        List<String> occupiedRoomIds = roomRepository.findOccupiedRoomIds(
                booking.getCheckIn(), booking.getCheckOut(), booking.getId());

        List<CheckInDataDTO.RoomGroupDTO> roomGroups = new ArrayList<>();

        for (BookedRoom br : booking.getBookedRooms()) {
            RoomType type = br.getRoomType();
            
            // Get all rooms of this type
            List<Room> roomsOfType = roomRepository.findAll().stream()
                    .filter(r -> r.getRoomType().getId().equals(type.getId()))
                    .collect(Collectors.toList());

            List<CheckInDataDTO.RoomStatusDTO> roomStatuses = roomsOfType.stream()
                    .map(r -> CheckInDataDTO.RoomStatusDTO.builder()
                            .id(r.getId())
                            .roomName(r.getRoomName())
                            .occupied(occupiedRoomIds.contains(r.getId()))
                            .build())
                    .collect(Collectors.toList());

            roomGroups.add(CheckInDataDTO.RoomGroupDTO.builder()
                    .roomTypeName(type.getTypeName())
                    .requiredQuantity(br.getQuantity())
                    .rooms(roomStatuses)
                    .build());
        }

        return CheckInDataDTO.builder()
                .booking(booking)
                .roomGroups(roomGroups)
                .build();
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request, String accountId) {
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (nights <= 0) nights = 1;

        Booking booking = new Booking();
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        if (accountId != null) {
            Account account = accountRepository.findById(accountId).orElse(null);
            booking.setAccount(account);
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
        booking.setGuest(guest);

        BigDecimal totalRoomAmount = BigDecimal.ZERO;
        List<BookedRoom> bookedRooms = new ArrayList<>();

        for (RoomSelection selection : request.getRoomSelections()) {
            RoomType roomType = roomTypeRepository.findById(selection.getRoomTypeId())
                    .orElseThrow(() -> new RuntimeException("Loại phòng " + selection.getRoomTypeId() + " không tồn tại!"));

            // Check availability: count available rooms of this type
            long availableCount = roomRepository.findByRoomTypeAndStatus(roomType, "AVAILABLE", Pageable.unpaged()).getTotalElements();
            if (availableCount < selection.getQuantity()) {
                throw new RuntimeException("Loại phòng " + roomType.getTypeName() + " không đủ phòng trống (Còn lại: " + availableCount + ")!");
            }

            BookedRoom bookedRoom = new BookedRoom();
            bookedRoom.setBooking(booking);
            bookedRoom.setRoomType(roomType);
            bookedRoom.setQuantity(selection.getQuantity());
            bookedRoom.setPriceAtBooking(roomType.getPrice());
            bookedRooms.add(bookedRoom);

            BigDecimal selectionPrice = roomType.getPrice().multiply(BigDecimal.valueOf(selection.getQuantity())).multiply(BigDecimal.valueOf(nights));
            totalRoomAmount = totalRoomAmount.add(selectionPrice);
        }

        booking.setBookedRooms(bookedRooms);
        booking.setTotalAmount(totalRoomAmount);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking createWalkInBooking(BookingRequest request) {
        Booking booking = createBooking(request, null);
        booking.setStatus(BookingStatus.PAID);
        booking.setPaymentDate(LocalDateTime.now());
        booking.setPaidAmount(booking.getTotalAmount());

        // Tạo bản ghi Payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setNote("Thanh toán tại quầy (Walk-in)");
        paymentRepository.save(payment);

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
            // Ghi nhận số tiền đã thanh toán ban đầu (thường là qua VNPay)
            booking.setPaidAmount(booking.getTotalAmount());
            
            // Tự động tạo bản ghi Payment
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus("SUCCESS");
            paymentRepository.save(payment);
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

        // Verify total number of assigned rooms matches booking
        int expectedRooms = booking.getBookedRooms().stream().mapToInt(BookedRoom::getQuantity).sum();
        if (request.getRoomIds().size() != expectedRooms) {
            throw new RuntimeException("Số lượng phòng được gán (" + request.getRoomIds().size() + ") không khớp với số lượng đã đặt (" + expectedRooms + ")!");
        }

        Set<Room> assignedRooms = new HashSet<>();
        Map<String, Integer> typeCounter = new HashMap<>(); // Track count per room type

        for (String roomId : request.getRoomIds()) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Phòng " + roomId + " không tồn tại!"));

            if (!"AVAILABLE".equals(room.getStatus())) {
                throw new RuntimeException("Phòng " + room.getRoomName() + " hiện không khả dụng!");
            }

            String typeId = room.getRoomType().getId();
            typeCounter.put(typeId, typeCounter.getOrDefault(typeId, 0) + 1);

            room.setStatus("OCCUPIED");
            roomRepository.save(room);
            assignedRooms.add(room);
        }

        // Verify assignment matches each RoomType quantity
        for (BookedRoom br : booking.getBookedRooms()) {
            int assignedCount = typeCounter.getOrDefault(br.getRoomType().getId(), 0);
            if (assignedCount != br.getQuantity()) {
                throw new RuntimeException("Số lượng phòng cho loại '" + br.getRoomType().getTypeName() + "' không khớp! (Đã gán: " + assignedCount + ", Yêu cầu: " + br.getQuantity() + ")");
            }
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

        // Save Occupants (Room Recipients)
        if (request.getOccupants() != null && !request.getOccupants().isEmpty()) {
            for (OccupantDTO occDto : request.getOccupants()) {
                Room room = roomRepository.findById(occDto.getRoomId())
                        .orElseThrow(() -> new RuntimeException("Phòng không tồn tại: " + occDto.getRoomId()));
                
                Occupant occupant = new Occupant();
                occupant.setBooking(booking);
                occupant.setRoom(room);
                occupant.setFullName(occDto.getFullName());
                occupant.setIdNumber(occDto.getIdNumber());
                occupant.setPhone(occDto.getPhone());
                occupantRepository.save(occupant);
            }
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
        
        // Tính toán lại số đêm ở thực tế (tối thiểu 1 đêm nếu checkout cùng ngày checkin)
        long actualNightsCalc = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), today);
        long actualNights = actualNightsCalc <= 0 ? 1 : actualNightsCalc;

        // Tính toán lại tổng tiền phòng
        BigDecimal actualRoomPrice = booking.getBookedRooms().stream()
                .map(br -> br.getPriceAtBooking().multiply(BigDecimal.valueOf(br.getQuantity())).multiply(BigDecimal.valueOf(actualNights)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính tổng tiền dịch vụ hiện tại
        BigDecimal totalServiceAmount = booking.getBookedServices().stream()
                .flatMap(bs -> bs.getDetails().stream())
                .map(detail -> detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cập nhật lại thông tin booking
        booking.setCheckOut(today);
        booking.setTotalAmount(actualRoomPrice.add(totalServiceAmount));
        booking.setStatus(BookingStatus.CHECKED_OUT);

        // Release all assigned rooms
        if (booking.getRooms() != null) {
            booking.getRooms().forEach(room -> {
                room.setStatus("AVAILABLE");
                roomRepository.save(room);
            });
        }

        // --- NEW: Tự động tạo nhiệm vụ dọn phòng khi check-out ---
        if (booking.getRooms() != null) {
            String guestName = (booking.getGuest() != null) ? booking.getGuest().getFullName() : "Khách";
            booking.getRooms().forEach(room -> {
                workAssignmentService.createTaskFromService(
                    null, // Không có detailId cho việc dọn phòng
                    "Dọn phòng " + room.getRoomName(),
                    room.getRoomName(),
                    guestName,
                    "ROLE_HOUSEKEEPING"
                );
            });
        }

        bookingRepository.save(booking);
    }

    @Override
    public Page<Booking> getMyBookings(String accountId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByAccount_Id(accountId, pageable);
        
        // Fill transient fields for display in UI
        bookings.forEach(booking -> {
            booking.getBookedServices().forEach(bs -> {
                bs.getDetails().forEach(detail -> {
                    workAssignmentRepository.findByTargetId(detail.getId()).ifPresent(wa -> {
                        detail.setServiceStatus(wa.getStatus());
                        if (wa.getEmployee() != null) {
                            detail.setStaffName(wa.getEmployee().getFullName());
                        } else {
                            detail.setStaffName("Đang chờ nhân viên...");
                        }
                    });
                });
            });
        });
        
        return bookings;
    }

    @Override
    public Page<Booking> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAll(pageable);
        // Fill transient fields for display in UI
        bookings.forEach(booking -> {
            booking.getBookedServices().forEach(bs -> {
                bs.getDetails().forEach(detail -> {
                    workAssignmentRepository.findByTargetId(detail.getId()).ifPresent(wa -> {
                        detail.setServiceStatus(wa.getStatus());
                        if (wa.getEmployee() != null) {
                            detail.setStaffName(wa.getEmployee().getFullName());
                        } else {
                            detail.setStaffName("Đang chờ...");
                        }
                    });
                });
            });
        });
        return bookings;
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

        // Tìm đơn dịch vụ đang ở trạng thái ORDERED cho phòng này (hoặc chung cho booking nếu không có phòng)
        Optional<BookedService> activeOrderOpt = booking.getBookedServices().stream()
                .filter(bs -> "ORDERED".equals(bs.getStatus()))
                .filter(bs -> (request.getRoomId() == null && bs.getRoom() == null) || 
                             (request.getRoomId() != null && bs.getRoom() != null && bs.getRoom().getId().equals(request.getRoomId())))
                .findFirst();

        BookedService bookedService;
        if (activeOrderOpt.isPresent()) {
            bookedService = activeOrderOpt.get();
        } else {
            bookedService = new BookedService();
            bookedService.setBooking(booking);
            bookedService.setStatus("ORDERED");
            bookedService.setTotalAmount(BigDecimal.ZERO);
            if (request.getRoomId() != null) {
                Room room = roomRepository.findById(request.getRoomId())
                        .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));
                bookedService.setRoom(room);
            }
            bookedService = bookedServiceRepository.save(bookedService);
            booking.getBookedServices().add(bookedService);
        }

        // Kiểm tra xem món này đã có trong đơn chưa
        Optional<BookedDetail> existingDetail = bookedService.getDetails().stream()
                .filter(d -> d.getService().getId().equals(service.getId()))
                .findFirst();

        if (existingDetail.isPresent()) {
            BookedDetail detail = existingDetail.get();
            detail.setQuantity(detail.getQuantity() + request.getQuantity());
        } else {
            BookedDetail detail = new BookedDetail();
            detail.setBookedService(bookedService);
            detail.setService(service);
            detail.setQuantity(request.getQuantity());
            detail.setUnitPrice(service.getPrice());
            bookedService.getDetails().add(detail);
        }

        // Cập nhật tổng tiền của đơn
        BigDecimal orderTotal = bookedService.getDetails().stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bookedService.setTotalAmount(orderTotal);
        bookedServiceRepository.save(bookedService);

        // Cập nhật tổng tiền của Booking (phòng + tất cả dịch vụ)
        // Lưu ý: Booking totalAmount ở đây thường là tiền phòng + các dịch vụ đã BOOKED.
        // Cần tính toán lại hoặc cộng dồn. Ở đây em cộng dồn phần chênh lệch cho đơn giản.
        BigDecimal serviceTotal = service.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal currentTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        booking.setTotalAmount(currentTotal.add(serviceTotal));
        bookingRepository.save(booking);

        // --- NEW: Tự động tạo nhiệm vụ cho bộ phận chuyên trách ---
        if (service.getCategory() != null && service.getCategory().getRequiredRole() != null) {
            String guestName = (booking.getGuest() != null) ? booking.getGuest().getFullName() : "Khách";
            String roomName = (bookedService.getRoom() != null) ? bookedService.getRoom().getRoomName() : "N/A";
            
            // Tìm detail vừa tạo/cập nhật để lấy ID
            bookedService.getDetails().stream()
                .filter(d -> d.getService().getId().equals(service.getId()))
                .findFirst()
                .ifPresent(detail -> {
                    workAssignmentService.createTaskFromService(
                        detail.getId(), 
                        service.getServiceName(), 
                        roomName, 
                        guestName, 
                        service.getCategory().getRequiredRole()
                    );
                });
        }
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
            BigDecimal orderTotal = bookedService.getTotalAmount() != null ? bookedService.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal currentTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            booking.setTotalAmount(currentTotal.subtract(orderTotal));
            bookingRepository.save(booking);
        }
        
        bookedService.setStatus(status);
        return convertToBookedServiceDTO(bookedServiceRepository.save(bookedService));
    }

    @Override
    @Transactional
    public BookedServiceDTO updateBookedServiceQuantity(String detailId, int newQuantity) {
        BookedDetail detail = bookedDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết dịch vụ không tồn tại!"));

        BookedService bookedService = detail.getBookedService();

        if (!"ORDERED".equals(bookedService.getStatus())) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa đơn hàng ở trạng thái 'Mới đặt'!");
        }

        int oldQuantity = detail.getQuantity();
        BigDecimal unitPrice = detail.getUnitPrice();
        BigDecimal amountDiff = unitPrice.multiply(BigDecimal.valueOf(newQuantity - oldQuantity));

        detail.setQuantity(newQuantity);
        bookedDetailRepository.save(detail);

        // Cập nhật tổng tiền của đơn (BookedService)
        BigDecimal newOrderTotal = bookedService.getDetails().stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bookedService.setTotalAmount(newOrderTotal);
        bookedServiceRepository.save(bookedService);

        // Cập nhật tổng tiền của Booking
        Booking booking = bookedService.getBooking();
        BigDecimal currentTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        booking.setTotalAmount(currentTotal.add(amountDiff));
        bookingRepository.save(booking);

        return convertToBookedServiceDTO(bookedService);
    }

    @Override
    @Transactional
    public void bookServices(String bookingId, String roomId, Map<String, Integer> serviceQuantities) {
        if (serviceQuantities == null || serviceQuantities.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : serviceQuantities.entrySet()) {
            BookServiceRequest request = new BookServiceRequest();
            request.setBookingId(bookingId);
            request.setRoomId(roomId);
            request.setServiceId(entry.getKey());
            request.setQuantity(entry.getValue());
            bookService(request);
        }
    }

    @Override
    public Booking getActiveBookingByRoom(String roomId) {
        return bookingRepository.findActiveBookingByRoom(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng hiện không có khách hoặc chưa Check-in!"));
    }

    @Override
    @Transactional
    public void rateBookedDetail(String id, int rating, String comment) {
        BookedDetail detail = bookedDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món dịch vụ không tồn tại!"));
        
        if (!"DELIVERED".equals(detail.getBookedService().getStatus())) {
            throw new RuntimeException("Chỉ có thể đánh giá dịch vụ sau khi đã nhận!");
        }

        detail.setRating(rating);
        detail.setRatingComment(comment);
        detail.setRatedAt(LocalDateTime.now());
        
        bookedDetailRepository.save(detail);
    }

    @Override
    public CheckoutSummaryDTO getCheckoutSummary(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        LocalDate today = LocalDate.now();
        LocalDate originalCheckOut = booking.getCheckOut();
        
        // Tính số đêm dự kiến ban đầu
        long originalNightsCalc = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), originalCheckOut);
        final long originalNights = originalNightsCalc <= 0 ? 1 : originalNightsCalc;

        // Tính số đêm thực tế (đến ngày hôm nay)
        // Nếu trả phòng sớm hơn dự kiến, sử dụng ngày hôm nay
        LocalDate actualCheckOut = today.isBefore(originalCheckOut) ? today : originalCheckOut;
        if (actualCheckOut.isBefore(booking.getCheckIn())) actualCheckOut = booking.getCheckIn();
        
        long actualNightsCalc = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), actualCheckOut);
        final long actualNights = actualNightsCalc <= 0 ? 1 : actualNightsCalc;

        // Lấy thông tin phòng đầu tiên (giả định đơn này đặt các phòng cùng loại hoặc lấy giá trung bình)
        // Lưu ý: Đơn giản hóa bằng cách lấy giá phòng từ bookedRooms
        BigDecimal pricePerNight = booking.getBookedRooms().isEmpty() ? BigDecimal.ZERO : booking.getBookedRooms().get(0).getPriceAtBooking();
        
        // Tổng tiền phòng dự kiến ban đầu
        BigDecimal originalRoomPrice = booking.getBookedRooms().stream()
                .map(br -> br.getPriceAtBooking().multiply(BigDecimal.valueOf(br.getQuantity())).multiply(BigDecimal.valueOf(originalNights)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền phòng thực tế
        BigDecimal actualRoomPrice = booking.getBookedRooms().stream()
                .map(br -> br.getPriceAtBooking().multiply(BigDecimal.valueOf(br.getQuantity())).multiply(BigDecimal.valueOf(actualNights)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Chênh lệch (số tiền được giảm/thay đổi)
        BigDecimal roomPriceAdjustment = actualRoomPrice.subtract(originalRoomPrice);

        List<BookedServiceDTO> serviceDTOs = booking.getBookedServices().stream()
                .map(this::convertToBookedServiceDTO)
                .collect(Collectors.toList());

        BigDecimal totalServiceAmount = serviceDTOs.stream()
                .map(BookedServiceDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền thực tế = Tiền phòng thực tế + Tiền dịch vụ
        BigDecimal totalAmount = actualRoomPrice.add(totalServiceAmount);
        BigDecimal originalTotalAmount = originalRoomPrice.add(totalServiceAmount);
        
        BigDecimal paidAmount = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal balance = totalAmount.subtract(paidAmount);

        List<OccupantDTO> occupantDTOs = booking.getOccupants() != null ? 
                booking.getOccupants().stream().map(this::convertToOccupantDTO).collect(Collectors.toList()) : 
                new ArrayList<>();

        return CheckoutSummaryDTO.builder()
                .bookingId(booking.getId())
                .guestName(booking.getGuest() != null ? booking.getGuest().getFullName() : "Khách vãng lai")
                .roomTypeName(!booking.getBookedRooms().isEmpty() ? booking.getBookedRooms().get(0).getRoomType().getTypeName() : "N/A")
                .roomNames(booking.getRooms() != null ? booking.getRooms().stream().map(Room::getRoomName).collect(Collectors.joining(", ")) : "N/A")
                .checkIn(booking.getCheckIn())
                .checkOut(originalCheckOut)
                .actualCheckOut(actualCheckOut)
                .totalNights(originalNights)
                .actualNights(actualNights)
                .roomPricePerNight(pricePerNight)
                .totalRoomPrice(actualRoomPrice)
                .roomPriceAdjustment(roomPriceAdjustment)
                .services(serviceDTOs)
                .totalServiceAmount(totalServiceAmount)
                .occupants(occupantDTOs)
                .totalAmount(totalAmount)
                .originalTotalAmount(originalTotalAmount)
                .paidAmount(paidAmount)
                .balance(balance)
                .build();
    }

    private BookedServiceDTO convertToBookedServiceDTO(BookedService bs) {
        List<BookedDetailDTO> detailDTOs = bs.getDetails().stream()
                .map(d -> {
                    String staffName = "Chưa có";
                    String itemStatus = "PENDING";
                    
                    Optional<WorkAssignment> assignment = workAssignmentRepository.findByTargetId(d.getId());
                    if (assignment.isPresent()) {
                        WorkAssignment wa = assignment.get();
                        itemStatus = wa.getStatus();
                        if (wa.getEmployee() != null) {
                            staffName = wa.getEmployee().getFullName();
                        }
                    }

                    return BookedDetailDTO.builder()
                        .id(d.getId())
                        .serviceId(d.getService().getId())
                        .serviceName(d.getService().getServiceName())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .subTotal(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                        .status(itemStatus)
                        .staffName(staffName)
                        .rating(d.getRating())
                        .ratingComment(d.getRatingComment())
                        .ratedAt(d.getRatedAt())
                        .build();
                })
                .collect(Collectors.toList());

        String serviceSummary = bs.getDetails().stream()
                .map(d -> d.getService().getServiceName())
                .collect(Collectors.joining(", "));
        
        Integer totalQty = bs.getDetails().stream()
                .mapToInt(BookedDetail::getQuantity)
                .sum();

        return BookedServiceDTO.builder()
                .id(bs.getId())
                .bookingId(bs.getBooking().getId())
                .guestName(bs.getBooking().getGuest() != null ? bs.getBooking().getGuest().getFullName() : "Khách vãng lai")
                .roomId(bs.getRoom() != null ? bs.getRoom().getId() : null)
                .roomName(bs.getRoom() != null ? bs.getRoom().getRoomName() : "N/A")
                .serviceName(serviceSummary)
                .totalQuantity(totalQty)
                .status(bs.getStatus())
                .totalAmount(bs.getTotalAmount())
                .createdAt(bs.getCreatedAt())
                .details(detailDTOs)
                .build();
    }

    private OccupantDTO convertToOccupantDTO(Occupant occ) {
        return OccupantDTO.builder()
                .roomId(occ.getRoom() != null ? occ.getRoom().getId() : null)
                .roomName(occ.getRoom() != null ? occ.getRoom().getRoomName() : "N/A")
                .fullName(occ.getFullName())
                .idNumber(occ.getIdNumber())
                .phone(occ.getPhone())
                .build();
    }
}
