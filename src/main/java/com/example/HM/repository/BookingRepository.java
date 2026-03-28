package com.example.HM.repository;

import com.example.HM.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    Page<Booking> findByAccount_Id(String accountId, Pageable pageable);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    Page<Booking> findByStatus(String status, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND LOWER(CAST(b.id AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Booking> findByStatusAndKeyword(String status, String keyword, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.checkIn = :checkIn")
    Page<Booking> findByStatusAndCheckIn(String status, java.time.LocalDate checkIn, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.checkIn = :checkIn AND LOWER(CAST(b.id AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Booking> findByStatusAndCheckInAndKeyword(String status, java.time.LocalDate checkIn, String keyword, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.checkOut = :checkOut")
    Page<Booking> findByStatusAndCheckOut(String status, java.time.LocalDate checkOut, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"rooms", "guest", "bookedRooms", "bookedRooms.roomType", "payments"})
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.checkOut = :checkOut AND LOWER(CAST(b.id AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Booking> findByStatusAndCheckOutAndKeyword(String status, java.time.LocalDate checkOut, String keyword, Pageable pageable);

    List<Booking> findByCheckInBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.rooms")
    List<Booking> findAllWithRooms();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Long countBookingsBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkIn BETWEEN :startDate AND :endDate AND (b.status = 'CHECKED_IN' OR b.status = 'CHECKED_OUT' OR b.status = 'PAID')")
    Long countCheckInsBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    @Query("SELECT CAST(b.createdAt AS date) as date, COUNT(b) as count FROM Booking b " +
           "WHERE b.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(b.createdAt AS date) ORDER BY CAST(b.createdAt AS date)")
    List<Object[]> getDailyBookingCountBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT b FROM Booking b JOIN b.rooms r WHERE r.id = :roomId AND b.status = 'CHECKED_IN'")
    java.util.Optional<Booking> findActiveBookingByRoom(@org.springframework.data.repository.query.Param("roomId") String roomId);

    @Query("SELECT br.roomType.typeName, COUNT(DISTINCT b) FROM Booking b JOIN b.bookedRooms br " +
           "WHERE b.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY br.roomType.typeName ORDER BY COUNT(DISTINCT b) DESC")
    List<Object[]> countBookingsByRoomType(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT r) FROM Booking b JOIN b.rooms r " +
           "WHERE b.status = 'CHECKED_IN' AND :date BETWEEN b.checkIn AND b.checkOut")
    Long countOccupiedRoomsAtDate(java.time.LocalDate date);

    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookedRooms br WHERE " +
           "br.roomType.id = :typeId AND " +
           "b.status NOT IN ('CANCELLED') AND " +
           "b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<Booking> findOverlappingBookingsByType(
            @Param("typeId") String typeId,
            @Param("checkIn") java.time.LocalDate checkIn,
            @Param("checkOut") java.time.LocalDate checkOut);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"guest"})
    @Query("SELECT b FROM Booking b")
    List<Booking> findAllWithGuest();
}
