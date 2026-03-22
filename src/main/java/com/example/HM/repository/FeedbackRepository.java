package com.example.HM.repository;

import com.example.HM.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Feedback> findByRatingOrderByCreatedAtDesc(Integer rating, Pageable pageable);
    Optional<Feedback> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
    
    Optional<Feedback> findByBookingAndRoomType(com.example.HM.entity.Booking booking, com.example.HM.entity.RoomType roomType);
    Optional<Feedback> findByBookingAndExtraService(com.example.HM.entity.Booking booking, com.example.HM.entity.ExtraService extraService);
    
    Page<Feedback> findByRoomType_IdOrderByCreatedAtDesc(String roomTypeId, Pageable pageable);
    
    long countByRoomType_Id(String roomTypeId);
    
    @org.springframework.data.jpa.repository.Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.roomType.id = :roomTypeId")
    Double getAverageRatingByRoomTypeId(String roomTypeId);
}
