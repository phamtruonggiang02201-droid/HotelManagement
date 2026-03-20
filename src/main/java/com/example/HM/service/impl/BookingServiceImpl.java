package com.example.HM.service.impl;

import com.example.HM.dto.*;
import com.example.HM.entity.*;
import com.example.HM.repository.*;
import com.example.HM.service.BookingService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final RoomRepository roomRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final AccountRepository accountRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final OccupantRepository occupantRepository;
    private final BookedServiceRepository bookedServiceRepository;

    @Override
    public ByteArrayInputStream exportBookingsToExcel(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<Booking> bookings;
        if (startDate != null && endDate != null) {
            bookings = bookingRepository.findByCheckInBetween(startDate, endDate);
        } else {
            bookings = bookingRepository.findAll();
        }

        String[] headers = { "Mã Booking", "Khách hàng", "Username", "Email", "Check-in", "Check-out", "Tổng tiền", "Số tiền đã trả", "Trạng thái", "Ngày thanh toán" };
        
        return ExcelHelper.dataToExcel(bookings, "Bookings", headers, (row, booking) -> {
            row.createCell(0).setCellValue(booking.getId());
            row.createCell(1).setCellValue(booking.getGuest() != null ? booking.getGuest().getFullName() : "N/A");
            row.createCell(2).setCellValue(booking.getAccount() != null ? booking.getAccount().getUsername() : "N/A");
            row.createCell(3).setCellValue(booking.getAccount() != null ? booking.getAccount().getEmail() : "N/A");
            row.createCell(4).setCellValue(booking.getCheckIn() != null ? booking.getCheckIn().toString() : "N/A");
            row.createCell(5).setCellValue(booking.getCheckOut() != null ? booking.getCheckOut().toString() : "N/A");
            row.createCell(6).setCellValue(booking.getTotalAmount() != null ? booking.getTotalAmount().doubleValue() : 0.0);
            row.createCell(7).setCellValue(booking.getPaidAmount() != null ? booking.getPaidAmount().doubleValue() : 0.0);
            row.createCell(8).setCellValue(booking.getStatus());
            row.createCell(9).setCellValue(booking.getPaymentDate() != null ? booking.getPaymentDate().toString() : "N/A");
        });
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
    public Page<BookedServiceDTO> getAllBookedServices(String keyword, String status, Pageable pageable) {
        // Luôn sắp xếp đơn mới nhất lên đầu nếu không có sort khác
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                org.springframework.data.domain.Sort.by("createdAt").descending()
            );
        }
        
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
    public ByteArrayInputStream exportBookedServicesToExcel(String keyword, String status) {
        List<BookedService> bookedServices;
        if (keyword != null && !keyword.isEmpty()) {
            bookedServices = bookedServiceRepository.searchBookedServicesList(keyword);
            if (status != null && !status.isEmpty()) {
                bookedServices = bookedServices.stream()
                        .filter(bs -> bs.getStatus().equals(status))
                        .collect(java.util.stream.Collectors.toList());
            }
        } else if (status != null && !status.isEmpty()) {
            bookedServices = bookedServiceRepository.findByStatusAll(status);
        } else {
            bookedServices = bookedServiceRepository.findAll();
        }
        
        String[] headers = { "Mã đơn", "Khách hàng", "Phòng", "Dịch vụ", "Số lượng", "Tổng tiền", "Trạng thái", "Ngày tạo" };
        
        return ExcelHelper.dataToExcel(bookedServices, "ServiceOrders", headers, (row, bs) -> {
            row.createCell(0).setCellValue(bs.getId().substring(0, 8));
            row.createCell(1).setCellValue(bs.getBooking().getGuest() != null ? bs.getBooking().getGuest().getFullName() : "Khách vãng lai");
            row.createCell(2).setCellValue(bs.getRoom() != null ? bs.getRoom().getRoomName() : "N/A");
            
            String summary = bs.getDetails().stream()
                    .map(d -> d.getService().getServiceName() + " (x" + d.getQuantity() + ")")
                    .collect(Collectors.joining(", "));
            row.createCell(3).setCellValue(summary);
            
            int totalQty = bs.getDetails().stream().mapToInt(BookedDetail::getQuantity).sum();
            row.createCell(4).setCellValue(totalQty);
            row.createCell(5).setCellValue(bs.getTotalAmount() != null ? bs.getTotalAmount().doubleValue() : 0.0);
            row.createCell(6).setCellValue(bs.getStatus());
            row.createCell(7).setCellValue(bs.getCreatedAt().toString());
        });
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
