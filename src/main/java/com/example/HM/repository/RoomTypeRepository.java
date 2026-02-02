package com.example.HM.repository;

import com.example.HM.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    Optional<RoomType> findByTypeName(String typeName);
}
