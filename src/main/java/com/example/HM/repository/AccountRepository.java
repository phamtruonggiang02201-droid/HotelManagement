package com.example.HM.repository;

import com.example.HM.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByVerificationToken(String token);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Account> findByResetToken(String token);

    Page<Account> findAllByRole_RoleNameIn(List<String> roleNames, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.role.roleName IN :roleNames AND (" +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Account> searchEmployees(@Param("search") String search, @Param("roleNames") List<String> roleNames, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE " +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Account> searchAllAccounts(@Param("search") String search, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.emailVerified = false AND a.verificationTokenExpiry < :now")
    void deleteExpiredUnverifiedAccounts(LocalDateTime now);
}
