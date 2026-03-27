package com.example.HM.repository;

import com.example.HM.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    Optional<RoomType> findByTypeName(String typeName);

    // Uniqueness check
    boolean existsByTypeName(String typeName);
    boolean existsByTypeNameAndIdNot(String typeName, String id);

    // Search and Pagination
    @org.springframework.data.jpa.repository.Query("SELECT rt FROM RoomType rt WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(rt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:typeId IS NULL OR :typeId = '' OR rt.id = :typeId)")
    Page<RoomType> searchRoomTypes(@org.springframework.data.repository.query.Param("keyword") String keyword, 
                                 @org.springframework.data.repository.query.Param("typeId") String typeId, 
                                 Pageable pageable);

    Page<RoomType> findByTypeNameContainingIgnoreCase(String keyword, Pageable pageable);
}
