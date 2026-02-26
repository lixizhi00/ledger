package com.ledger.repository;

import com.ledger.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, String> {

    java.util.Optional<Record> findByUserIdAndId(String userId, String id);

    List<Record> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate start, LocalDate end);

    List<Record> findByUserIdAndCategoryIdAndDateBetweenOrderByDateDesc(
            String userId, String categoryId, LocalDate start, LocalDate end);

    boolean existsByUserId(String userId);
}
