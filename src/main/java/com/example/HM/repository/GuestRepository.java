package com.example.HM.repository;

import com.example.HM.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, String> {
    Optional<Guest> findByEmail(String email);
    List<Guest> findAllByEmail(String email);
    Optional<Guest> findByPhone(String phone);
}
