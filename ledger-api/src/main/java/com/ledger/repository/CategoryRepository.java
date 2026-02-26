package com.ledger.repository;

import com.ledger.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUserIdOrderBySortOrderAsc(String userId);

    List<Category> findByUserIdAndParentIdIsNullOrderBySortOrderAsc(String userId);

    List<Category> findByUserIdAndParentIdOrderBySortOrderAsc(String userId, String parentId);
}
