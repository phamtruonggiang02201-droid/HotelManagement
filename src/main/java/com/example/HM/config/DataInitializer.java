package com.example.HM.config;

import com.example.HM.entity.*;
import com.example.HM.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                     AccountRepository accountRepository,
                                     RoomTypeRepository roomTypeRepository,
                                     RoomRepository roomRepository,
                                     ServiceCategoryRepository categoryRepository,
                                     ExtraServiceRepository extraServiceRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
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
            if (roleRepository.findByRoleName("EMPLOYEE").isEmpty()) {
                Role employee = new Role();
                employee.setRoleName("EMPLOYEE");
                employee.setDescription("Nhân viên");
                roleRepository.save(employee);
            }
            if (roleRepository.findByRoleName("RECEPTION").isEmpty()) {
                Role reception = new Role();
                reception.setRoleName("RECEPTION");
                reception.setDescription("Lễ tân");
                roleRepository.save(reception);
            }
            if (roleRepository.findByRoleName("MANAGER").isEmpty()) {
                Role manager = new Role();
                manager.setRoleName("MANAGER");
                manager.setDescription("Quản lý");
                roleRepository.save(manager);
            }

            // 2. Init Admin Account
            if (accountRepository.findByUsername("admin").isEmpty()) {
                Role adminRole = roleRepository.findByRoleName("ADMIN").get();
                
                Account admin = new Account();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setEmail("admin@luxestay.com");
                admin.setFirstName("Hệ thống");
                admin.setLastName("Admin");
                admin.setRole(adminRole);
                admin.setStatus(true);
                admin.setEmailVerified(true);
                admin.setNationality("Việt Nam");
                admin.setIdType("CCCD");
                admin.setIdNumber("000000000000");
                admin.setJobTitle("Quản trị hệ thống");
                
                accountRepository.save(admin);
                System.out.println(">>> Default Admin Account Created: admin / Admin@123");
            } else {
                // Update existing admin if jobTitle is missing
                accountRepository.findByUsername("admin").ifPresent(a -> {
                    if (a.getJobTitle() == null || a.getJobTitle().isEmpty()) {
                        a.setJobTitle("Quản trị hệ thống");
                        accountRepository.save(a);
                    }
                });
            }

            // 3. Init Manager Account
            if (accountRepository.findByUsername("manager").isEmpty()) {
                Role managerRole = roleRepository.findByRoleName("MANAGER").get();
                Account manager = new Account();
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("Manager@123"));
                manager.setEmail("manager@luxestay.com");
                manager.setFirstName("Nguyễn");
                manager.setLastName("Quản Lý");
                manager.setRole(managerRole);
                manager.setStatus(true);
                manager.setEmailVerified(true);
                manager.setNationality("Việt Nam");
                manager.setIdType("CCCD");
                manager.setIdNumber("111111111111");
                manager.setJobTitle("Quản lý khách sạn");
                accountRepository.save(manager);
                System.out.println(">>> Default Manager Account Created: manager / Manager@123");
            } else {
                // Update existing manager if jobTitle is missing
                accountRepository.findByUsername("manager").ifPresent(a -> {
                    if (a.getJobTitle() == null || a.getJobTitle().isEmpty()) {
                        a.setJobTitle("Quản lý khách sạn");
                        accountRepository.save(a);
                    }
                });
            }

            // 4. Init Receptionist Account
            if (accountRepository.findByUsername("receptionist").isEmpty()) {
                Role receptionRole = roleRepository.findByRoleName("RECEPTION").get();
                Account reception = new Account();
                reception.setUsername("receptionist");
                reception.setPassword(passwordEncoder.encode("Reception@123"));
                reception.setEmail("receptionists@luxestay.com");
                reception.setFirstName("Trần");
                reception.setLastName("Lễ Tân");
                reception.setRole(receptionRole);
                reception.setStatus(true);
                reception.setEmailVerified(true);
                reception.setNationality("Việt Nam");
                reception.setIdType("CCCD");
                reception.setIdNumber("222222222222");
                reception.setJobTitle("Lễ tân trưởng");
                accountRepository.save(reception);
                System.out.println(">>> Default Receptionist Account Created: receptionist / Reception@123");
            } else {
                // Update existing receptionist if jobTitle is missing
                accountRepository.findByUsername("receptionist").ifPresent(a -> {
                    if (a.getJobTitle() == null || a.getJobTitle().isEmpty()) {
                        a.setJobTitle("Lễ tân trưởng");
                        accountRepository.save(a);
                    }
                });
            }

            // 4.5. Init 20 sample Receptionists for testing
            if (accountRepository.findByUsername("staff1").isEmpty()) {
                Role receptionRole = roleRepository.findByRoleName("RECEPTION").get();
                String[] firstNames = {"Anh", "Bình", "Cường", "Dũng", "Em", "Linh", "Minh", "Nam", "Nga", "Oanh", "Phúc", "Quang", "Sơn", "Tâm", "Uyên", "Vinh", "Xuân", "Yến", "Hoàng", "Tuấn"};
                String[] lastNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Đặng", "Bùi", "Đỗ"};

                for (int i = 1; i <= 20; i++) {
                    String username = "staff" + i;
                    if (accountRepository.findByUsername(username).isEmpty()) {
                        Account staff = new Account();
                        staff.setUsername(username);
                        staff.setPassword(passwordEncoder.encode("Staff@123"));
                        staff.setEmail("staff" + i + "@luxestay.com");
                        staff.setFirstName(firstNames[i % firstNames.length]);
                        staff.setLastName(lastNames[i % lastNames.length]);
                        staff.setRole(receptionRole);
                        staff.setJobTitle("Lễ tân");
                        staff.setStatus(true);
                        staff.setEmailVerified(true);
                        staff.setPhone("090" + String.format("%07d", i));
                        staff.setNationality("Việt Nam");
                        staff.setIdType("CCCD");
                        staff.setIdNumber("1000000000" + String.format("%02d", i));
                        
                        accountRepository.save(staff);
                    }
                }
                System.out.println(">>> 20 Sample Receptionists Created");
            }

            // 5. Init Room Types
            if (roomTypeRepository.count() == 0) {
                RoomType std = new RoomType();
                std.setTypeName("Standard");
                std.setDescription("Phòng tiêu chuẩn, đầy đủ tiện nghi cơ bản");
                std.setCapacity(2);
                std.setPrice(new BigDecimal("500000"));
                std.setRoomImage("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=800&q=80");
                roomTypeRepository.save(std);

                RoomType dlx = new RoomType();
                dlx.setTypeName("Deluxe");
                dlx.setDescription("Phòng cao cấp, không gian rộng rãi");
                dlx.setCapacity(2);
                dlx.setPrice(new BigDecimal("850000"));
                dlx.setRoomImage("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80");
                roomTypeRepository.save(dlx);

                RoomType suite = new RoomType();
                suite.setTypeName("Suite");
                suite.setDescription("Phòng sang trọng nhất với view đẹp");
                suite.setCapacity(4);
                suite.setPrice(new BigDecimal("1500000"));
                suite.setRoomImage("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80");
                roomTypeRepository.save(suite);
                System.out.println(">>> Sample Room Types Created");
            }

            // 6. Init Rooms
            if (roomRepository.count() == 0) {
                RoomType std = roomTypeRepository.findByTypeName("Standard").get();
                RoomType dlx = roomTypeRepository.findByTypeName("Deluxe").get();
                
                Room r101 = new Room();
                r101.setRoomName("P.101");
                r101.setRoomType(std);
                r101.setStatus("AVAILABLE");
                roomRepository.save(r101);

                Room r102 = new Room();
                r102.setRoomName("P.102");
                r102.setRoomType(std);
                r102.setStatus("AVAILABLE");
                roomRepository.save(r102);

                Room r201 = new Room();
                r201.setRoomName("P.201");
                r201.setRoomType(dlx);
                r201.setStatus("AVAILABLE");
                roomRepository.save(r201);
                System.out.println(">>> Sample Rooms Created");
            }

            // 7. Init Service Categories & Services
            if (categoryRepository.count() == 0) {
                ServiceCategory food = new ServiceCategory();
                food.setCategoryName("Ẩm thực");
                food.setDescription("Dịch vụ ăn uống");
                categoryRepository.save(food);

                ServiceCategory luxury = new ServiceCategory();
                luxury.setCategoryName("Thư giãn");
                luxury.setDescription("Spa, Massage, Sauna");
                categoryRepository.save(luxury);

                // Add Services
                ExtraService breakfast = new ExtraService();
                breakfast.setServiceName("Buffet Sáng");
                breakfast.setPrice(new BigDecimal("150000"));
                breakfast.setCategory(food);
                extraServiceRepository.save(breakfast);

                ExtraService massage = new ExtraService();
                massage.setServiceName("Massage Body (60p)");
                massage.setPrice(new BigDecimal("350000"));
                massage.setCategory(luxury);
                extraServiceRepository.save(massage);
                System.out.println(">>> Sample Services Created");
            }
        };
    }
}
