package com.example.HM.repository;

import com.example.HM.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByVerificationToken(String token);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Account> findByResetToken(String token);
    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.emailVerified = false AND a.verificationTokenExpiry < :now")
    void deleteExpiredUnverifiedAccounts(LocalDateTime now);
    List<Account> findAllByIsDeletedFalseOrderByCreatedAtDesc();

}
