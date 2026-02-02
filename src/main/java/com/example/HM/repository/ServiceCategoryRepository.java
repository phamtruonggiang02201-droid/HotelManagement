package com.example.HM.repository;

import com.example.HM.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, String> {
    Optional<ServiceCategory> findByCategoryName(String categoryName);
}
