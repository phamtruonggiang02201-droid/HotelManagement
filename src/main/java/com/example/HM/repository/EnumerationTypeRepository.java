package com.example.HM.repository;

import com.example.HM.entity.EnumerationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnumerationTypeRepository extends JpaRepository<EnumerationType, String> {

    Optional<EnumerationType> findByEnumTypeId(String enumTypeId);

    boolean existsByEnumTypeId(String enumTypeId);
}
