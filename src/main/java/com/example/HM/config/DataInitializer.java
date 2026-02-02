package com.example.HM.config;

import com.example.HM.entity.*;
import com.example.HM.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                     AccountRepository accountRepository,
                                     RoomTypeRepository roomTypeRepository,
                                     RoomRepository roomRepository,
                                     ServiceCategoryRepository categoryRepository,
                                     ServiceRepository serviceRepository,
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
                
                accountRepository.save(admin);
                System.out.println(">>> Default Admin Account Created: admin / Admin@123");
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
                accountRepository.save(manager);
                System.out.println(">>> Default Manager Account Created: manager / Manager@123");
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
                accountRepository.save(reception);
                System.out.println(">>> Default Receptionist Account Created: receptionist / Reception@123");
            }

            // 5. Init Room Types
            if (roomTypeRepository.count() == 0) {
                RoomType std = new RoomType();
                std.setTypeName("Standard");
                std.setDescription("Phòng tiêu chuẩn, đầy đủ tiện nghi cơ bản");
                std.setCapacity(2);
                roomTypeRepository.save(std);

                RoomType dlx = new RoomType();
                dlx.setTypeName("Deluxe");
                dlx.setDescription("Phòng cao cấp, không gian rộng rãi");
                dlx.setCapacity(2);
                roomTypeRepository.save(dlx);

                RoomType suite = new RoomType();
                suite.setTypeName("Suite");
                suite.setDescription("Phòng sang trọng nhất với view đẹp");
                suite.setCapacity(4);
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
                r101.setPrice(new java.math.BigDecimal("500000"));
                r101.setStatus("AVAILABLE");
                roomRepository.save(r101);

                Room r102 = new Room();
                r102.setRoomName("P.102");
                r102.setRoomType(std);
                r102.setPrice(new java.math.BigDecimal("500000"));
                r102.setStatus("AVAILABLE");
                roomRepository.save(r102);

                Room r201 = new Room();
                r201.setRoomName("P.201");
                r201.setRoomType(dlx);
                r201.setPrice(new java.math.BigDecimal("850000"));
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
                Service breakfast = new Service();
                breakfast.setServiceName("Buffet Sáng");
                breakfast.setPrice(new java.math.BigDecimal("150000"));
                breakfast.setCategory(food);
                serviceRepository.save(breakfast);

                Service massage = new Service();
                massage.setServiceName("Massage Body (60p)");
                massage.setPrice(new java.math.BigDecimal("350000"));
                massage.setCategory(luxury);
                serviceRepository.save(massage);
                System.out.println(">>> Sample Services Created");
            }
        };
    }
}
