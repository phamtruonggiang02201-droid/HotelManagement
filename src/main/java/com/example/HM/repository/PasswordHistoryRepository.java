package com.example.HM.repository;

import com.example.HM.entity.Account;
import com.example.HM.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, String> {
    List<PasswordHistory> findByAccountOrderByCreatedAtDesc(Account account);
}
