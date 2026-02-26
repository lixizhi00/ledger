package com.ledger.controller;

import com.ledger.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> today() {
        BigDecimal total = statsService.getTodayTotal();
        return ResponseEntity.ok(Map.of("total", total, "formatted", "¥ " + total.setScale(2, java.math.RoundingMode.HALF_UP)));
    }

    @GetMapping("/month")
    public ResponseEntity<Map<String, Object>> month() {
        BigDecimal total = statsService.getMonthTotal();
        return ResponseEntity.ok(Map.of("total", total, "formatted", "¥ " + total.setScale(2, java.math.RoundingMode.HALF_UP)));
    }

    @GetMapping("/year")
    public ResponseEntity<Map<String, Object>> year() {
        BigDecimal total = statsService.getYearTotal();
        return ResponseEntity.ok(Map.of("total", total, "formatted", "¥ " + total.setScale(2, java.math.RoundingMode.HALF_UP)));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        BigDecimal today = statsService.getTodayTotal();
        BigDecimal month = statsService.getMonthTotal();
        BigDecimal year = statsService.getYearTotal();
        return ResponseEntity.ok(Map.of(
                "today", today,
                "month", month,
                "year", year,
                "todayFormatted", "¥ " + today.setScale(2, java.math.RoundingMode.HALF_UP),
                "monthFormatted", "¥ " + month.setScale(2, java.math.RoundingMode.HALF_UP),
                "yearFormatted", "¥ " + year.setScale(2, java.math.RoundingMode.HALF_UP)
        ));
    }
}
