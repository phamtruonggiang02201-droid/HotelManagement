package com.example.HM.repository;

import com.example.HM.entity.Service;
import com.example.HM.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    List<Service> findByCategory(ServiceCategory category);
}
