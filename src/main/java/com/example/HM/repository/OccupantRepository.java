package com.example.HM.repository;

import com.example.HM.entity.Occupant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OccupantRepository extends JpaRepository<Occupant, String> {
    List<Occupant> findByBookingId(String bookingId);
}
