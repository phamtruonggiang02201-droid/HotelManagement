package com.example.HM.repository;

import com.example.HM.entity.ExtraService;
import com.example.HM.entity.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtraServiceRepository extends JpaRepository<ExtraService, String> {
    Page<ExtraService> findByCategory(ServiceCategory category, Pageable pageable);

    // Uniqueness check
    boolean existsByServiceName(String serviceName);
    boolean existsByServiceNameAndIdNot(String serviceName, String id);

    // Search and Pagination
    Page<ExtraService> findByServiceNameContainingIgnoreCase(String keyword, Pageable pageable);
}
