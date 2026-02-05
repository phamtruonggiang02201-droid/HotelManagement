package com.example.HM.repository;

import com.example.HM.entity.Room;
import com.example.HM.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByRoomType(RoomType roomType);
    List<Room> findByStatus(String status);
    boolean existsByRoomName(String roomName);
}
