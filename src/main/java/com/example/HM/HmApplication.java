package com.example.HM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HmApplication {

	public static void main(String[] args) {
		SpringApplication.run(HmApplication.class, args);
	}

    @jakarta.annotation.PostConstruct
    public void init() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println(">>> Hệ thống đã chuyển sang múi giờ: " + java.util.TimeZone.getDefault().getID() + " | Hiện tại: " + java.time.LocalDateTime.now());
    }
}
