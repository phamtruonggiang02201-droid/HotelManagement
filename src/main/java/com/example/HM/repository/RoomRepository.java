package com.example.HM.repository;

import com.example.HM.entity.Room;
import com.example.HM.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    Page<Room> findByRoomType(RoomType roomType, Pageable pageable);
    Page<Room> findByStatus(String status, Pageable pageable);
    Page<Room> findByRoomTypeAndStatus(RoomType roomType, String status, Pageable pageable);
    long countByRoomTypeAndStatus(RoomType roomType, String status);

    // Uniqueness check
    boolean existsByRoomName(String roomName);
    boolean existsByRoomNameAndIdNot(String roomName, String id);

    // Search and Pagination
    @Query("SELECT r FROM Room r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:typeId IS NULL OR :typeId = '' OR r.roomType.id = :typeId) AND " +
           "(:minPrice IS NULL OR r.roomType.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR r.roomType.price <= :maxPrice)")
    Page<Room> searchRoomsAdvanced(
            @Param("keyword") String keyword, 
            @Param("typeId") String typeId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable);

    // Check if room has bookings
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b JOIN b.rooms r WHERE r.id = :roomId")
    boolean hasBookings(@Param("roomId") String roomId);
}
