package com.example.HM.config;

import com.example.HM.entity.*;
import com.example.HM.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                     AccountRepository accountRepository,
                                     RoomTypeRepository roomTypeRepository,
                                     RoomRepository roomRepository,
                                     ServiceCategoryRepository categoryRepository,
                                     ExtraServiceRepository extraServiceRepository,
                                     BookingRepository bookingRepository,
                                     GuestRepository guestRepository,
                                     BookedServiceRepository bookedServiceRepository,
                                     FeedbackRepository feedbackRepository,
                                     RoomIssueReportRepository roomIssueReportRepository,
                                     RefundRepository refundRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Init Roles
            if (roleRepository.findByRoleName("CUSTOMER").isEmpty()) {
                Role customer = new Role();
                customer.setRoleName("CUSTOMER");
                customer.setDescription("Khách hàng");
                roleRepository.save(customer);
            }
            if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
                Role admin = new Role();
                admin.setRoleName("ADMIN");
                admin.setDescription("Quản trị viên");
                roleRepository.save(admin);
            }
            if (roleRepository.findByRoleName("MANAGER").isEmpty()) {
                Role manager = new Role();
                manager.setRoleName("MANAGER");
                manager.setDescription("Quản lý");
                roleRepository.save(manager);
            }
            if (roleRepository.findByRoleName("RECEPTION").isEmpty()) {
                Role reception = new Role();
                reception.setRoleName("RECEPTION");
                reception.setDescription("Lễ tân");
                roleRepository.save(reception);
            }

            Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
            Role managerRole = roleRepository.findByRoleName("MANAGER").orElse(null);
            Role receptionRole = roleRepository.findByRoleName("RECEPTION").orElse(null);
            Role customerRole = roleRepository.findByRoleName("CUSTOMER").orElse(null);

            // 2. Init Default Accounts
            if (accountRepository.findByUsername("admin").isEmpty() && adminRole != null) {
                Account admin = new Account();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setEmail("admin@luxestay.com");
                admin.setFirstName("Hệ thống");
                admin.setLastName("Admin");
                admin.setRole(adminRole);
                admin.setStatus(true);
                admin.setEmailVerified(true);
                accountRepository.save(admin);
            }

            if (accountRepository.findByUsername("manager").isEmpty() && managerRole != null) {
                Account manager = new Account();
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("Manager@123"));
                manager.setEmail("manager@luxestay.com");
                manager.setFirstName("Nguyễn");
                manager.setLastName("Quản Lý");
                manager.setRole(managerRole);
                manager.setStatus(true);
                manager.setEmailVerified(true);
                accountRepository.save(manager);
            }

            if (accountRepository.findByUsername("reception").isEmpty() && receptionRole != null) {
                Account reception = new Account();
                reception.setUsername("reception");
                reception.setPassword(passwordEncoder.encode("Reception@123"));
                reception.setEmail("reception@luxestay.com");
                reception.setFirstName("Lê");
                reception.setLastName("Lễ Tân");
                reception.setRole(receptionRole);
                reception.setStatus(true);
                reception.setEmailVerified(true);
                accountRepository.save(reception);
            }

            // 3. Init Sample Customers for Testing
            if (accountRepository.findByUsername("customer1").isEmpty() && customerRole != null) {
                Account c1 = new Account();
                c1.setUsername("customer1");
                c1.setPassword(passwordEncoder.encode("password123"));
                c1.setEmail("customer1@example.com");
                c1.setFirstName("Trường");
                c1.setLastName("Giang");
                c1.setRole(customerRole);
                c1.setStatus(true);
                c1.setEmailVerified(true);
                accountRepository.save(c1);
            }

            if (accountRepository.findByUsername("customer2").isEmpty() && customerRole != null) {
                Account c2 = new Account();
                c2.setUsername("customer2");
                c2.setPassword(passwordEncoder.encode("password123"));
                c2.setEmail("customer2@example.com");
                c2.setFirstName("Văn");
                c2.setLastName("A");
                c2.setRole(customerRole);
                c2.setStatus(true);
                c2.setEmailVerified(true);
                accountRepository.save(c2);
            }

            // 4. Init Room Types
            if (roomTypeRepository.count() == 0) {
                RoomType std = new RoomType();
                std.setTypeName("Standard");
                std.setCapacity(2);
                std.setPrice(new BigDecimal("500000"));
                std.setDescription("Phòng tiêu chuẩn");
                roomTypeRepository.save(std);

                RoomType dlx = new RoomType();
                dlx.setTypeName("Deluxe");
                dlx.setCapacity(2);
                dlx.setPrice(new BigDecimal("850000"));
                dlx.setDescription("Phòng cao cấp");
                roomTypeRepository.save(dlx);
            }

            // 5. Init Rooms
            if (roomRepository.count() == 0) {
                roomTypeRepository.findByTypeName("Standard").ifPresent(rt -> {
                    Room r101 = new Room();
                    r101.setRoomName("P.101");
                    r101.setRoomType(rt);
                    r101.setStatus("AVAILABLE");
                    roomRepository.save(r101);
                });
            }

            // 6. Init Service Categories
            if (categoryRepository.count() == 0) {
                ServiceCategory food = new ServiceCategory();
                food.setCategoryName("Ẩm thực");
                food.setDescription("Dịch vụ ăn uống tại phòng và nhà hàng");
                categoryRepository.save(food);

                ServiceCategory spa = new ServiceCategory();
                spa.setCategoryName("Spa & Wellness");
                spa.setDescription("Chăm sóc sức khỏe và làm đẹp");
                categoryRepository.save(spa);

                ServiceCategory transport = new ServiceCategory();
                transport.setCategoryName("Di chuyển & Tiện ích");
                transport.setDescription("Đưa đón và các dịch vụ khác");
                categoryRepository.save(transport);
            }

            // 7. Init Extra Services
            if (extraServiceRepository.count() == 0) {
                categoryRepository.findAll().forEach(cat -> {
                    if (cat.getCategoryName().equals("Ẩm thực")) {
                        ExtraService s1 = new ExtraService();
                        s1.setServiceName("Buffet Sáng");
                        s1.setPrice(new BigDecimal("150000"));
                        s1.setCategory(cat);
                        extraServiceRepository.save(s1);

                        ExtraService s2 = new ExtraService();
                        s2.setServiceName("Cơm Lam Gà Nướng");
                        s2.setPrice(new BigDecimal("250000"));
                        s2.setCategory(cat);
                        extraServiceRepository.save(s2);

                        ExtraService s3 = new ExtraService();
                        s3.setServiceName("Coca Cola");
                        s3.setPrice(new BigDecimal("20000"));
                        s3.setCategory(cat);
                        extraServiceRepository.save(s3);
                    } else if (cat.getCategoryName().equals("Spa & Wellness")) {
                        ExtraService s1 = new ExtraService();
                        s1.setServiceName("Massage Toàn Thân (60p)");
                        s1.setPrice(new BigDecimal("450000"));
                        s1.setCategory(cat);
                        extraServiceRepository.save(s1);

                        ExtraService s2 = new ExtraService();
                        s2.setServiceName("Xông Hơi Tinh Dầu");
                        s2.setPrice(new BigDecimal("200000"));
                        s2.setCategory(cat);
                        extraServiceRepository.save(s2);
                    } else if (cat.getCategoryName().equals("Di chuyển & Tiện ích")) {
                        ExtraService s1 = new ExtraService();
                        s1.setServiceName("Đưa đón Sân Bay");
                        s1.setPrice(new BigDecimal("300000"));
                        s1.setCategory(cat);
                        extraServiceRepository.save(s1);

                        ExtraService s2 = new ExtraService();
                        s2.setServiceName("Thuê Xe Máy (Ngày)");
                        s2.setPrice(new BigDecimal("120000"));
                        s2.setCategory(cat);
                        extraServiceRepository.save(s2);

                        ExtraService s3 = new ExtraService();
                        s3.setServiceName("Giặt ủi (kg)");
                        s3.setPrice(new BigDecimal("30000"));
                        s3.setCategory(cat);
                        extraServiceRepository.save(s3);
                    }
                });
            }

            // 8. Init Sample Bookings for Test
            if (bookingRepository.count() <= 2) {
                accountRepository.findByUsername("customer1").ifPresent(acc -> {
                    roomTypeRepository.findByTypeName("Deluxe").ifPresent(rt -> {
                        // 1. Booking đã trả phòng (Để test Feedback)
                        Booking b1 = new Booking();
                        b1.setAccount(acc);
                        b1.setRoomType(rt);
                        b1.setCheckIn(LocalDate.now().minusDays(10));
                        b1.setCheckOut(LocalDate.now().minusDays(7));
                        b1.setStatus("CHECKED_OUT");
                        b1.setTotalAmount(new BigDecimal("2550000"));
                        b1.setPaymentDate(LocalDateTime.now().minusDays(7));
                        bookingRepository.save(b1);

                        // 2. Booking đang ở (Để test Check-out & Service Order)
                        Booking b2 = new Booking();
                        b2.setAccount(acc);
                        b2.setRoomType(rt);
                        b2.setCheckIn(LocalDate.now().minusDays(2));
                        b2.setCheckOut(LocalDate.now().plusDays(1));
                        b2.setStatus("CHECKED_IN");
                        b2.setTotalAmount(new BigDecimal("2550000"));
                        bookingRepository.save(b2);
                        
                        // Gán phòng cho khách đang ở
                        roomRepository.findAll().stream()
                            .filter(r -> r.getRoomType().getId().equals(rt.getId()) && "AVAILABLE".equals(r.getStatus()))
                            .findFirst().ifPresent(room -> {
                                b2.setRooms(java.util.Set.of(room));
                                room.setStatus("OCCUPIED");
                                roomRepository.save(room);
                                bookingRepository.save(b2);
                            });
                    });
                });

                accountRepository.findByUsername("customer2").ifPresent(acc -> {
                    roomTypeRepository.findByTypeName("Standard").ifPresent(rt -> {
                        // 3. Booking chuẩn bị Check-in hôm nay
                        Booking b3 = new Booking();
                        b3.setAccount(acc);
                        b3.setRoomType(rt);
                        b3.setCheckIn(LocalDate.now());
                        b3.setCheckOut(LocalDate.now().plusDays(2));
                        b3.setStatus("PAID");
                        b3.setTotalAmount(new BigDecimal("1000000"));
                        b3.setPaymentDate(LocalDateTime.now().minusHours(5));
                        bookingRepository.save(b3);

                        // 4. Booking chờ thanh toán (Pending)
                        Booking b4 = new Booking();
                        b4.setAccount(acc);
                        b4.setRoomType(rt);
                        b4.setCheckIn(LocalDate.now().plusDays(5));
                        b4.setCheckOut(LocalDate.now().plusDays(7));
                        b4.setStatus("PENDING_PAYMENT");
                        b4.setTotalAmount(new BigDecimal("1000000"));
                        bookingRepository.save(b4);
                    });
                });

                // 5. Booking vãng lai (Guest only - Không có Account)
                roomTypeRepository.findByTypeName("Deluxe").ifPresent(rt -> {
                    Guest g1 = new Guest();
                    g1.setFullName("Nguyễn Văn Khách");
                    g1.setPhone("0912345678");
                    g1.setIdNumber("123456789012");
                    g1.setEmail("khachvanglai@gmail.com");
                    guestRepository.save(g1);

                    Booking b5 = new Booking();
                    b5.setGuest(g1);
                    b5.setRoomType(rt);
                    b5.setCheckIn(LocalDate.now());
                    b5.setCheckOut(LocalDate.now().plusDays(3));
                    b5.setStatus("PAID");
                    b5.setTotalAmount(new BigDecimal("2550000"));
                    bookingRepository.save(b5);
                });

                System.out.println(">>> 5 Diverse Sample Bookings Initialized <<<");

                // 6. Thêm một vài dịch vụ cho khách đang ở (b2) để test Check-out
                bookingRepository.findAll().stream()
                    .filter(b -> "CHECKED_IN".equals(b.getStatus()))
                    .findFirst().ifPresent(b -> {
                        extraServiceRepository.findAll().stream().limit(3).forEach(srv -> {
                            BookedService bs = new BookedService();
                            bs.setBooking(b);
                            bs.setService(srv);
                            bs.setQuantity(1);
                            bs.setUnitPrice(srv.getPrice());
                            bs.setStatus("DELIVERED");
                            bookedServiceRepository.save(bs);
                        });
                    });

                // 7. Thêm Feedback mẫu cho các đơn đã CHECKED_OUT
                bookingRepository.findAll().stream()
                    .filter(b -> "CHECKED_OUT".equals(b.getStatus()))
                    .forEach(b -> {
                        Feedback f = new Feedback();
                        f.setBooking(b);
                        f.setRating(5);
                        f.setComment("Dịch vụ tuyệt vời, phòng ốc sạch sẽ và nhân viên rất nhiệt tình. Chắc chắn sẽ quay lại!");
                        feedbackRepository.save(f);
                    });

                // 8. Thêm Sự cố mẫu (Issue) cho khách đang ở
                bookingRepository.findAll().stream()
                    .filter(b -> "CHECKED_IN".equals(b.getStatus()))
                    .findFirst().ifPresent(b -> {
                        RoomIssueReport issue = new RoomIssueReport();
                        issue.setBooking(b);
                        issue.setDescription("Vòi hoa sen trong phòng tắm bị rò rỉ nước.");
                        issue.setStatus("PENDING");
                        
                        // Lấy đại diện một phòng đang có khách để gán vào sự cố
                        roomRepository.findAll().stream()
                            .filter(r -> "OCCUPIED".equals(r.getStatus()))
                            .findFirst()
                            .ifPresent(issue::setRoom);
                            
                        roomIssueReportRepository.save(issue);
                    });

                // 9. Thêm yêu cầu hoàn tiền mẫu
                bookingRepository.findAll().stream()
                    .filter(b -> "PENDING_PAYMENT".equals(b.getStatus()))
                    .findFirst().ifPresent(b -> {
                        Refund r = new Refund();
                        r.setBooking(b);
                        r.setReason("Khách hàng muốn đổi sang loại phòng Suite và yêu cầu hoàn cọc đơn cũ.");
                        r.setRefundAmount(new BigDecimal("500000"));
                        r.setStatus("PENDING");
                        r.setRequestedAt(LocalDateTime.now().minusHours(2));
                        refundRepository.save(r);
                    });
            }

            // 8. Init 20 Sample Staff Members
            if (accountRepository.count() <= 10) { // Nếu chỉ có vài account mặc định thì thêm 20 staff
                Role mgrRole = roleRepository.findByRoleName("MANAGER").orElse(null);
                Role recRole = roleRepository.findByRoleName("RECEPTION").orElse(null);
                
                if (mgrRole != null && recRole != null) {
                    for (int i = 1; i <= 20; i++) {
                        String username = "staff" + i;
                        if (accountRepository.findByUsername(username).isEmpty()) {
                            Account staff = new Account();
                            staff.setUsername(username);
                            staff.setPassword(passwordEncoder.encode("Staff@123"));
                            staff.setEmail(username + "@luxestay.com");
                            staff.setFirstName("Nhân Viên");
                            staff.setLastName(String.valueOf(i));
                            staff.setRole(i <= 5 ? mgrRole : recRole); // 5 Manager, 15 Reception
                            staff.setStatus(true);
                            staff.setEmailVerified(true);
                            //staff.setPhoneNumber("0908123" + String.format("%03d", i));
                            accountRepository.save(staff);
                        }
                    }
                    System.out.println(">>> 20 Sample Staff Members Initialized <<<");
                }
            }

            // 9. Patch: Fix null isActive for existing services
            extraServiceRepository.findByIsActiveIsNull().forEach(srv -> {
                srv.setIsActive(true);
                extraServiceRepository.save(srv);
            });

            System.out.println(">>> Data Initialized Successfully <<<");
        };
    }
}