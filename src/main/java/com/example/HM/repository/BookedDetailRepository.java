package com.example.HM.repository;

import com.example.HM.entity.BookedDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookedDetailRepository extends JpaRepository<BookedDetail, String> {
}
