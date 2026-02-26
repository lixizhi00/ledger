package com.ledger.repository;

import com.ledger.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndType(String userId, String type);

    Optional<Budget> findByUserIdAndCategoryIdAndType(String userId, String categoryId, String type);
}
