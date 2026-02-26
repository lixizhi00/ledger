package com.ledger.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RecordService recordService;

    public BigDecimal getTodayTotal() {
        LocalDate today = LocalDate.now();
        return recordService.getTotalByDateRange(today, today, null);
    }

    public BigDecimal getMonthTotal() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        return recordService.getTotalByDateRange(start, now, null);
    }

    public BigDecimal getYearTotal() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfYear(1);
        return recordService.getTotalByDateRange(start, now, null);
    }
}
