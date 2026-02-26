package com.ledger.controller;

import com.ledger.dto.BudgetDTO;
import com.ledger.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<BudgetDTO> get() {
        return ResponseEntity.ok(budgetService.getBudgets());
    }

    @PutMapping("/yearly")
    public ResponseEntity<BudgetDTO> updateYearly(@RequestBody Map<String, java.math.BigDecimal> yearly) {
        return ResponseEntity.ok(budgetService.updateYearly(yearly));
    }
}
