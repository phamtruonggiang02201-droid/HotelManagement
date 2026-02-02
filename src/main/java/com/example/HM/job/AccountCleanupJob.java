package com.example.HM.job;

import com.example.HM.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AccountCleanupJob {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Job chạy mỗi 5 phút để dọn dẹp các tài khoản chưa xác nhận và đã hết hạn token
     */
    @Scheduled(fixedRate = 300000) // 300,000 ms = 5 minutes
    public void cleanupExpiredAccounts() {
        log.info("Bắt đầu dọn dẹp tài khoản chưa xác nhận hết hạn...");
        try {
            accountRepository.deleteExpiredUnverifiedAccounts(LocalDateTime.now());
            log.info("Dọn dẹp tài khoản hết hạn hoàn tất.");
        } catch (Exception e) {
            log.error("Lỗi khi dọn dẹp tài khoản hết hạn: {}", e.getMessage());
        }
    }
}
