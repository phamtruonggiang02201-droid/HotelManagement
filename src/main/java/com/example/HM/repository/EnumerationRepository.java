package com.example.HM.repository;

import com.example.HM.entity.Enumeration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnumerationRepository extends JpaRepository<Enumeration, String> {

    List<Enumeration> findByEnumType_EnumTypeIdOrderBySequenceNumAsc(String enumTypeId);

    Optional<Enumeration> findByEnumType_EnumTypeIdAndEnumCode(String enumTypeId, String enumCode);

    boolean existsByEnumType_EnumTypeIdAndEnumCode(String enumTypeId, String enumCode);
}
