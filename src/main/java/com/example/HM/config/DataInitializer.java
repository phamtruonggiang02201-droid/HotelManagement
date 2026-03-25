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
            if (accountRepository.findByUsername("admin123").isEmpty()) {
                Role adminRole = roleRepository.findByRoleName("ADMIN").get();
                
                Account admin = new Account();
                admin.setUsername("admin123");
                admin.setPassword(passwordEncoder.encode("Admin@1231"));
                admin.setEmail("admin123@luxestay.com");
                admin.setFirstName("Hệ thống");
                admin.setLastName("Admin1");
                admin.setRole(adminRole);
                admin.setStatus(true);
                admin.setEmailVerified(true);
                admin.setNationality("Việt Nam");
                admin.setIdType("CCCD");
                admin.setIdNumber("000000000000");
                
                accountRepository.save(admin);
                System.out.println(">>> Default Admin Account Created: admin / Admin@123");
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
