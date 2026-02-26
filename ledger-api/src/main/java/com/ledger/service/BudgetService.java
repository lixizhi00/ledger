package com.ledger.service;

import com.ledger.dto.BudgetDTO;
import com.ledger.entity.Budget;
import com.ledger.repository.BudgetRepository;
import com.ledger.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetDTO getBudgets() {
        String userId = UserContext.getUserId();
        List<Budget> yearly = budgetRepository.findByUserIdAndType(userId, "yearly");
        List<Budget> monthly = budgetRepository.findByUserIdAndType(userId, "monthly");
        BudgetDTO dto = new BudgetDTO();
        Map<String, BigDecimal> yMap = new HashMap<>();
        Map<String, BigDecimal> mMap = new HashMap<>();
        yearly.forEach(b -> yMap.put(b.getCategoryId(), b.getAmount()));
        monthly.forEach(b -> mMap.put(b.getCategoryId(), b.getAmount()));
        dto.setYearly(yMap);
        dto.setMonthly(mMap);
        return dto;
    }

    @Transactional
    public BudgetDTO updateYearly(Map<String, BigDecimal> yearly) {
        String userId = UserContext.getUserId();
        log.info("用户 {} 更新年预算，涉及 {} 个分类", userId, yearly.size());
        yearly.forEach((categoryId, amount) -> {
            Budget b = budgetRepository.findByUserIdAndCategoryIdAndType(userId, categoryId, "yearly")
                    .orElse(Budget.builder().categoryId(categoryId).type("yearly").userId(userId).amount(BigDecimal.ZERO).build());
            b.setAmount(amount != null ? amount : BigDecimal.ZERO);
            budgetRepository.save(b);
            Budget m = budgetRepository.findByUserIdAndCategoryIdAndType(userId, categoryId, "monthly")
                    .orElse(Budget.builder().categoryId(categoryId).type("monthly").userId(userId).amount(BigDecimal.ZERO).build());
            m.setAmount(b.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
            budgetRepository.save(m);
        });
        return getBudgets();
    }

    @Transactional
    public void syncParentBudgets(List<String> parentIds, Map<String, BigDecimal> childYearlySums) {
        String userId = UserContext.getUserId();
        for (String parentId : parentIds) {
            BigDecimal sum = childYearlySums.getOrDefault(parentId, BigDecimal.ZERO);
            Budget y = budgetRepository.findByUserIdAndCategoryIdAndType(userId, parentId, "yearly")
                    .orElse(Budget.builder().categoryId(parentId).type("yearly").userId(userId).amount(BigDecimal.ZERO).build());
            y.setAmount(sum);
            budgetRepository.save(y);
            Budget m = budgetRepository.findByUserIdAndCategoryIdAndType(userId, parentId, "monthly")
                    .orElse(Budget.builder().categoryId(parentId).type("monthly").userId(userId).amount(BigDecimal.ZERO).build());
            m.setAmount(sum.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
            budgetRepository.save(m);
        }
    }
}
