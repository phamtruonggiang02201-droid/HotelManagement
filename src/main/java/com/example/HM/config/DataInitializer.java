package com.example.HM.config;

import com.example.HM.entity.*;
import com.example.HM.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                     AccountRepository accountRepository,
                                     RoomTypeRepository roomTypeRepository,
                                     RoomRepository roomRepository,
                                     ServiceCategoryRepository categoryRepository,
                                     ExtraServiceRepository extraServiceRepository,
                                     BookingRepository bookingRepository,
                                     GuestRepository guestRepository,
                                     BookedServiceRepository bookedServiceRepository,
                                     BookedRoomRepository bookedRoomRepository,
                                     FeedbackRepository feedbackRepository,
                                     RoomIssueReportRepository roomIssueReportRepository,
                                     RefundRepository refundRepository,
                                     EnumerationTypeRepository enumTypeRepository,
                                     EnumerationRepository enumerationRepository,
                                     PaymentRepository paymentRepository,
                                     PaymentMethodRepository paymentMethodRepository,
                                     WorkAssignmentRepository assignmentRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            // 0. Init Enumerations
            initEnumerations(enumTypeRepository, enumerationRepository);
            // 0.1 Init Payment Methods
            initPaymentMethods(paymentMethodRepository);
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
            if (roleRepository.findByRoleName("ROLE_CHEF").isEmpty()) {
                Role chef = new Role();
                chef.setRoleName("ROLE_CHEF");
                chef.setDescription("Bộ phận Nhà Bếp");
                roleRepository.save(chef);
            }
            if (roleRepository.findByRoleName("ROLE_MASSAGE").isEmpty()) {
                Role massage = new Role();
                massage.setRoleName("ROLE_MASSAGE");
                massage.setDescription("Bộ phận Massage/Spa");
                roleRepository.save(massage);
            }
            if (roleRepository.findByRoleName("ROLE_HOUSEKEEPING").isEmpty()) {
                Role housekeeping = new Role();
                housekeeping.setRoleName("ROLE_HOUSEKEEPING");
                housekeeping.setDescription("Bộ phận Buồng phòng");
                roleRepository.save(housekeeping);
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

            // 2.1 Init Specialized Staff
            Role chefRole = roleRepository.findByRoleName("ROLE_CHEF").orElse(null);
            Role massageRole = roleRepository.findByRoleName("ROLE_MASSAGE").orElse(null);
            Role hkRole = roleRepository.findByRoleName("ROLE_HOUSEKEEPING").orElse(null);

            if (accountRepository.findByUsername("chef_nguyen").isEmpty() && chefRole != null) {
                Account acc = new Account();
                acc.setUsername("chef_nguyen");
                acc.setPassword(passwordEncoder.encode("Staff@123"));
                acc.setEmail("chef_nguyen@luxestay.com");
                acc.setFirstName("Nguyễn"); acc.setLastName("Bếp");
                acc.setRole(chefRole); acc.setStatus(true); acc.setEmailVerified(true);
                accountRepository.save(acc);
            }
            if (accountRepository.findByUsername("massage_linh").isEmpty() && massageRole != null) {
                Account acc = new Account();
                acc.setUsername("massage_linh");
                acc.setPassword(passwordEncoder.encode("Staff@123"));
                acc.setEmail("massage_linh@luxestay.com");
                acc.setFirstName("Trần"); acc.setLastName("Linh");
                acc.setRole(massageRole); acc.setStatus(true); acc.setEmailVerified(true);
                accountRepository.save(acc);
            }
            if (accountRepository.findByUsername("cleaner_hoa").isEmpty() && hkRole != null) {
                Account acc = new Account();
                acc.setUsername("cleaner_hoa");
                acc.setPassword(passwordEncoder.encode("Staff@123"));
                acc.setEmail("cleaner_hoa@luxestay.com");
                acc.setFirstName("Lê"); acc.setLastName("Hoa");
                acc.setRole(hkRole); acc.setStatus(true); acc.setEmailVerified(true);
                accountRepository.save(acc);
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

            if (accountRepository.findByUsername("customer3").isEmpty() && customerRole != null) {
                Account c3 = new Account();
                c3.setUsername("customer3");
                c3.setPassword(passwordEncoder.encode("password123"));
                c3.setEmail("customer3@example.com");
                c3.setFirstName("Thị");
                c3.setLastName("B");
                c3.setRole(customerRole);
                c3.setStatus(true);
                c3.setEmailVerified(true);
                accountRepository.save(c3);
            }

            // 4. Init Room Types
            if (roomTypeRepository.count() == 0) {
                RoomType std = new RoomType();
                std.setTypeName("Standard");
                std.setCapacity(2);
                std.setPrice(new BigDecimal("500000"));
                std.setDescription("Phòng tiêu chuẩn với đầy đủ tiện nghi, phù hợp cho khách du lịch ngắn ngày.");
                std.setRoomImage("https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&q=80&w=1200");
                roomTypeRepository.save(std);

                RoomType dlx = new RoomType();
                dlx.setTypeName("Deluxe");
                dlx.setCapacity(2);
                dlx.setPrice(new BigDecimal("850000"));
                dlx.setDescription("Phòng cao cấp với tầm nhìn đẹp, nội thất sang trọng mang lại trải nghiệm nghỉ dưỡng đích thực.");
                dlx.setRoomImage("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=1200");
                roomTypeRepository.save(dlx);

                RoomType ste = new RoomType();
                ste.setTypeName("Suite");
                ste.setCapacity(2);
                ste.setPrice(new BigDecimal("1500000"));
                ste.setDescription("Phòng sang trọng hạng nhất (Suite) với phòng khách riêng biệt, tiện nghi đẳng cấp 5 sao.");
                ste.setRoomImage("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=1200");
                roomTypeRepository.save(ste);

                RoomType fml = new RoomType();
                fml.setTypeName("Family");
                fml.setCapacity(4);
                fml.setPrice(new BigDecimal("1200000"));
                fml.setDescription("Không gian rộng rãi lý tưởng cho gia đình, đầy đủ các tiện ích cho cả trẻ nhỏ và người lớn.");
                fml.setRoomImage("https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&q=80&w=1200");
                roomTypeRepository.save(fml);
            } else {
                // Patch for existing room types without images
                roomTypeRepository.findAll().forEach(type -> {
                    if (type.getRoomImage() == null || type.getRoomImage().isBlank() || type.getRoomImage().startsWith("data:")) {
                        if (type.getTypeName().equals("Standard")) type.setRoomImage("https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&q=80&w=1200");
                        else if (type.getTypeName().equals("Deluxe")) type.setRoomImage("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=1200");
                        else if (type.getTypeName().equals("Suite")) type.setRoomImage("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&q=80&w=1200");
                        else if (type.getTypeName().equals("Family")) type.setRoomImage("https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&q=80&w=1200");
                        roomTypeRepository.save(type);
                    }
                });
            }

            // 5. Init Rooms (40 rooms total, 10 per floor)
            if (roomRepository.count() == 0) {
                String[] types = {"Standard", "Deluxe", "Suite", "Family"};
                for (int floor = 1; floor <= 4; floor++) {
                    String typeName = types[floor - 1];
                    final RoomType rt = roomTypeRepository.findByTypeName(typeName).orElse(null);
                    if (rt != null) {
                        for (int i = 1; i <= 10; i++) {
                            Room room = new Room();
                            room.setRoomName("P." + (floor * 100 + i)); // 101-110, 201-210, etc.
                            room.setRoomType(rt);
                            room.setStatus("AVAILABLE");
                            roomRepository.save(room);
                        }
                    }
                }
                System.out.println(">>> 40 Rooms across 4 Floors Initialized <<<");
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

            // 6.1 Update Categories with roles
            categoryRepository.findAll().forEach(cat -> {
                if (cat.getCategoryName().contains("Ẩm thực")) {
                    cat.setRequiredRole("ROLE_CHEF");
                    categoryRepository.save(cat);
                } else if (cat.getCategoryName().contains("Spa")) {
                    cat.setRequiredRole("ROLE_MASSAGE");
                    categoryRepository.save(cat);
                }
            });

            // 6.2 Init SAMPLE SCHEDULE for TODAY
            LocalDate today = LocalDate.now();
            accountRepository.findByUsername("chef_nguyen").ifPresent(acc -> {
                if (assignmentRepository.findAllByEmployee_IdAndWorkDate(acc.getId(), today).isEmpty()) {
                    WorkAssignment wa = new WorkAssignment();
                    wa.setEmployee(acc); wa.setWorkDate(today); wa.setArea("Nhà hàng"); wa.setShift("Sáng");
                    wa.setType("SCHEDULE"); wa.setStatus("COMPLETED");
                    assignmentRepository.save(wa);
                }
            });
            accountRepository.findByUsername("massage_linh").ifPresent(acc -> {
                if (assignmentRepository.findAllByEmployee_IdAndWorkDate(acc.getId(), today).isEmpty()) {
                    WorkAssignment wa = new WorkAssignment();
                    wa.setEmployee(acc); wa.setWorkDate(today); wa.setArea("Spa"); wa.setShift("Chiều");
                    wa.setType("SCHEDULE"); wa.setStatus("COMPLETED");
                    assignmentRepository.save(wa);
                }
            });
            accountRepository.findByUsername("cleaner_hoa").ifPresent(acc -> {
                if (assignmentRepository.findAllByEmployee_IdAndWorkDate(acc.getId(), today).isEmpty()) {
                    WorkAssignment wa = new WorkAssignment();
                    wa.setEmployee(acc); wa.setWorkDate(today); wa.setArea("Toàn khu"); wa.setShift("Sáng");
                    wa.setType("SCHEDULE"); wa.setStatus("COMPLETED");
                    assignmentRepository.save(wa);
                }
            });

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

            // 8. Init Diverse Sample Bookings for Test (Past, Present, Future)
            System.out.println(">>> Starting Data Seeding: Bookings, Payments, Feedbacks... <<<");
            if (true) { // Luôn chạy khi ở chế độ tạo dữ liệu mẫu
                PaymentMethod cash = paymentMethodRepository.findByMethodName("Tiền mặt").orElse(null);
                PaymentMethod vnpay = paymentMethodRepository.findByMethodName("VNPay").orElse(null);
                
                accountRepository.findByUsername("customer1").ifPresent(acc -> {
                    // Thêm 50 bookings rải rác trong 6 tháng qua
                    for (int i = 1; i <= 50; i++) {
                        LocalDate date = LocalDate.now().minusDays(i * 3);
                        Booking b = new Booking();
                        b.setAccount(acc);
                        b.setCheckIn(date);
                        b.setCheckOut(date.plusDays(2));
                        
                        // Random status
                        if (i < 15) b.setStatus("CHECKED_OUT");
                        else if (i < 25) b.setStatus("PAID");
                        else if (i < 35) b.setStatus("CANCELLED");
                        else if (i < 40) b.setStatus("CHECKED_IN");
                        else b.setStatus("PAID");

                        // Create Guest info for each booking so it shows up in UI
                        Guest g = new Guest();
                        g.setFullName(acc.getFirstName() + " " + acc.getLastName() + " " + i);
                        g.setEmail(acc.getEmail());
                        g.setPhone("090" + (1000000 + i));
                        g.setIdNumber("ID" + (123456789 + i));
                        guestRepository.save(g);
                        b.setGuest(g);

                        BigDecimal amount = new BigDecimal(500000 + (long)(Math.random() * 2000000));
                        b.setTotalAmount(amount);
                        b.setPaidAmount(b.getStatus().equals("CANCELLED") ? BigDecimal.ZERO : amount);
                        b.setPaymentDate(b.getStatus().equals("CANCELLED") ? null : b.getCheckIn().atTime(10, 0));
                        bookingRepository.save(b);

                        // 8.1 Add BookedRoom details
                        RoomType rt = roomTypeRepository.findAll().get((int)(Math.random() * 4));
                        BookedRoom br = new BookedRoom();
                        br.setBooking(b);
                        br.setRoomType(rt);
                        br.setQuantity(1);
                        br.setPriceAtBooking(rt.getPrice());
                        bookedRoomRepository.save(br);

                        // 8.2 Assign actual rooms for non-cancelled bookings
                        if (!b.getStatus().equals("CANCELLED") && !b.getStatus().equals("PENDING_PAYMENT")) {
                            roomRepository.findAll().stream()
                                .filter(r -> r.getRoomType().getId().equals(rt.getId()) && "AVAILABLE".equals(r.getStatus()))
                                .findFirst().ifPresent(room -> {
                                    b.setRooms(java.util.Set.of(room));
                                    if (b.getStatus().equals("CHECKED_IN")) {
                                        room.setStatus("OCCUPIED");
                                    }
                                    roomRepository.save(room);
                                    bookingRepository.save(b);
                                });
                        }

                        // Lưu Payment nếu đã thanh toán
                        if (!b.getStatus().equals("CANCELLED") && !b.getStatus().equals("PENDING_PAYMENT")) {
                            Payment p = new Payment();
                            p.setBooking(b);
                            p.setAmount(amount);
                            p.setPaymentDate(b.getPaymentDate());
                            p.setPaymentStatus("SUCCESS");
                            p.setPaymentMethod(i % 2 == 0 ? cash : vnpay);
                            paymentRepository.save(p);
                        }

                        // Thêm Dịch vụ cho mỗi booking (Random 1-3 dịch vụ)
                        if (!b.getStatus().equals("CANCELLED")) {
                            BookedService bs = new BookedService();
                            bs.setBooking(b);
                            bs.setStatus("DELIVERED");
                            bs.setTotalAmount(BigDecimal.ZERO);
                            
                            java.util.List<ExtraService> allServices = extraServiceRepository.findAll();
                            int numServices = (int)(Math.random() * 3) + 1;
                            for (int j = 0; j < numServices; j++) {
                                ExtraService srv = allServices.get((int)(Math.random() * allServices.size()));
                                BookedDetail detail = new BookedDetail();
                                detail.setBookedService(bs);
                                detail.setService(srv);
                                detail.setQuantity((int)(Math.random() * 2) + 1);
                                detail.setUnitPrice(srv.getPrice());
                                
                                // Random rating cho dịch vụ
                                detail.setRating((int)(Math.random() * 2) + 4);
                                detail.setRatingComment("Rất hài lòng " + j);
                                detail.setRatedAt(b.getCheckOut().atTime(12, 0));
                                
                                bs.getDetails().add(detail);
                                bs.setTotalAmount(bs.getTotalAmount().add(srv.getPrice().multiply(new BigDecimal(detail.getQuantity()))));
                            }
                            bookedServiceRepository.save(bs);
                            
                            // Cập nhật lại tổng tiền booking bao gồm dịch vụ
                            b.setTotalAmount(b.getTotalAmount().add(bs.getTotalAmount()));
                            bookingRepository.save(b);
                        }

                        // Thêm Feedback cho các đơn đã xong
                        if (b.getStatus().equals("CHECKED_OUT") || b.getStatus().equals("COMPLETED")) {
                            Feedback f = new Feedback();
                            f.setBooking(b);
                            f.setRating((int)(Math.random() * 2) + 4); // 4 or 5 stars
                            f.setComment("Dịch vụ tuyệt vời tại " + (rt != null ? rt.getTypeName() : "khách sạn") + "! " + i);
                            
                            // Link feedback to the room type of the booking
                            if (rt != null) {
                                f.setRoomType(rt);
                            }
                            feedbackRepository.save(f);
                        }
                    }
                });
                System.out.println(">>> 50 Diverse Historical Bookings & Payments Initialized <<<");
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
            // 10. Seeding Feedbacks for all RoomTypes if needed
            if (feedbackRepository.count() <= 50) {
                roomTypeRepository.findAll().forEach(type -> {
                    String[] sampleComments = {
                        "Phòng cực kỳ sạch sẽ, nhân viên hỗ trợ nhiệt tình. Sẽ quay lại!",
                        "Không gian yên tĩnh, giường nằm rất thoải mái. 10 điểm cho chất lượng!",
                        "View từ phòng " + type.getTypeName() + " thực sự ấn tượng. Đáng đồng tiền bát gạo.",
                        "Dịch vụ phòng nhanh chóng, đồ ăn sáng đa dạng và ngon miệng.",
                        "Trải nghiệm tuyệt vời, không có gì để phàn nàn. Cảm ơn LuxeStay!"
                    };
                    for (int i = 0; i < sampleComments.length; i++) {
                        final int index = i;
                        Feedback f = new Feedback();
                        f.setRating(index % 2 == 0 ? 5 : 4);
                        f.setComment(sampleComments[index]);
                        f.setRoomType(type);
                        
                        // Fake a booking for it to show customer info if needed
                        accountRepository.findByUsername("customer" + ((index % 3) + 1)).ifPresent(acc -> {
                            Booking b = new Booking();
                            b.setAccount(acc);
                            b.setStatus("CHECKED_OUT");
                            b.setCheckIn(LocalDate.now().minusDays(10 +     index));
                            b.setCheckOut(LocalDate.now().minusDays(8 + index));
                            bookingRepository.save(b);
                            f.setBooking(b);
                        });
                        
                        feedbackRepository.save(f);
                    }
                });
                System.out.println(">>> Professional Sample Feedbacks Initialized for all Room Types <<<");
            }

            System.out.println(">>> Data Initialized Successfully: 50 Bookings & Payments created! <<<");
        };
    }

    private void initPaymentMethods(PaymentMethodRepository repo) {
        if (repo.count() > 0) return;
        
        String[] methods = {"Tiền mặt", "Chuyển khoản", "VNPay", "Thẻ tín dụng"};
        for (String m : methods) {
            PaymentMethod pm = new PaymentMethod();
            pm.setMethodName(m);
            repo.save(pm);
        }
        System.out.println(">>> Payment Methods Initialized <<<");
    }

    private void initEnumerations(EnumerationTypeRepository enumTypeRepo, EnumerationRepository enumRepo) {
        if (enumTypeRepo.count() > 0) return; // Đã init rồi thì bỏ qua

        // --- BOOKING_STATUS ---
        EnumerationType bookingStatus = createEnumType(enumTypeRepo, "BOOKING_STATUS", "Trạng thái đặt phòng");
        createEnum(enumRepo, bookingStatus, "PENDING_PAYMENT", "Chờ thanh toán", 1);
        createEnum(enumRepo, bookingStatus, "PAID", "Đã thanh toán", 2);
        createEnum(enumRepo, bookingStatus, "CHECKED_IN", "Đã nhận phòng", 3);
        createEnum(enumRepo, bookingStatus, "CHECKED_OUT", "Đã trả phòng", 4);
        createEnum(enumRepo, bookingStatus, "COMPLETED", "Hoàn thành", 5);
        createEnum(enumRepo, bookingStatus, "CANCELLED", "Đã hủy", 6);

        // --- ROOM_STATUS ---
        EnumerationType roomStatus = createEnumType(enumTypeRepo, "ROOM_STATUS", "Trạng thái phòng");
        createEnum(enumRepo, roomStatus, "AVAILABLE", "Trống", 1);
        createEnum(enumRepo, roomStatus, "OCCUPIED", "Đang sử dụng", 2);
        createEnum(enumRepo, roomStatus, "MAINTENANCE", "Đang bảo trì", 3);

        // --- BOOKED_SERVICE_STATUS ---
        EnumerationType bookedServiceStatus = createEnumType(enumTypeRepo, "BOOKED_SERVICE_STATUS", "Trạng thái dịch vụ đã đặt");
        createEnum(enumRepo, bookedServiceStatus, "ORDERED", "Mới đặt", 1);
        createEnum(enumRepo, bookedServiceStatus, "DELIVERED", "Đã phục vụ", 2);
        createEnum(enumRepo, bookedServiceStatus, "CANCELLED", "Đã hủy", 3);

        // --- ROOM_ISSUE_STATUS ---
        EnumerationType roomIssueStatus = createEnumType(enumTypeRepo, "ROOM_ISSUE_STATUS", "Trạng thái sự cố phòng");
        createEnum(enumRepo, roomIssueStatus, "PENDING", "Chờ xử lý", 1);
        createEnum(enumRepo, roomIssueStatus, "RESOLVED", "Đã xử lý", 2);

        // --- REFUND_STATUS ---
        EnumerationType refundStatus = createEnumType(enumTypeRepo, "REFUND_STATUS", "Trạng thái hoàn tiền");
        createEnum(enumRepo, refundStatus, "PENDING", "Chờ duyệt", 1);
        createEnum(enumRepo, refundStatus, "APPROVED", "Đã duyệt", 2);
        createEnum(enumRepo, refundStatus, "REJECTED", "Đã từ chối", 3);

        // --- WORK_ASSIGNMENT_STATUS ---
        EnumerationType workStatus = createEnumType(enumTypeRepo, "WORK_ASSIGNMENT_STATUS", "Trạng thái phân công");
        createEnum(enumRepo, workStatus, "PENDING", "Chờ thực hiện", 1);
        createEnum(enumRepo, workStatus, "COMPLETED", "Hoàn thành", 2);
        createEnum(enumRepo, workStatus, "CANCELLED", "Đã hủy", 3);

        // --- SHIFT ---
        EnumerationType shift = createEnumType(enumTypeRepo, "SHIFT", "Ca làm việc");
        createEnum(enumRepo, shift, "MORNING", "Sáng", 1);
        createEnum(enumRepo, shift, "AFTERNOON", "Chiều", 2);
        createEnum(enumRepo, shift, "EVENING", "Tối", 3);

        // --- GENDER ---
        EnumerationType gender = createEnumType(enumTypeRepo, "GENDER", "Giới tính");
        createEnum(enumRepo, gender, "MALE", "Nam", 1);
        createEnum(enumRepo, gender, "FEMALE", "Nữ", 2);
        createEnum(enumRepo, gender, "OTHER", "Khác", 3);

        // --- ID_TYPE ---
        EnumerationType idType = createEnumType(enumTypeRepo, "ID_TYPE", "Loại giấy tờ");
        createEnum(enumRepo, idType, "CCCD", "Căn cước công dân", 1);
        createEnum(enumRepo, idType, "CMND", "Chứng minh nhân dân", 2);
        createEnum(enumRepo, idType, "PASSPORT", "Hộ chiếu", 3);

        // --- PAYMENT_STATUS ---
        EnumerationType paymentStatus = createEnumType(enumTypeRepo, "PAYMENT_STATUS", "Trạng thái thanh toán");
        createEnum(enumRepo, paymentStatus, "PENDING", "Chờ thanh toán", 1);
        createEnum(enumRepo, paymentStatus, "SUCCESS", "Thành công", 2);
        createEnum(enumRepo, paymentStatus, "FAILED", "Thất bại", 3);

        System.out.println(">>> Enumerations Initialized Successfully <<<");
    }

    private EnumerationType createEnumType(EnumerationTypeRepository repo, String typeId, String description) {
        EnumerationType type = new EnumerationType();
        type.setEnumTypeId(typeId);
        type.setDescription(description);
        return repo.save(type);
    }

    private void createEnum(EnumerationRepository repo, EnumerationType type, String code, String name, int seq) {
        Enumeration e = new Enumeration();
        e.setEnumType(type);
        e.setEnumCode(code);
        e.setEnumName(name);
        e.setSequenceNum(seq);
        repo.save(e);
    }
}