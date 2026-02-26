package com.ledger.service;

import com.ledger.dto.RecordDTO;
import com.ledger.entity.Record;
import com.ledger.exception.ResourceNotFoundException;
import com.ledger.repository.RecordRepository;
import com.ledger.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final RecordRepository recordRepository;

    @Transactional
    public RecordDTO add(RecordDTO dto) {
        String userId = UserContext.getUserId();
        Record r = Record.builder()
                .amount(dto.getAmount())
                .categoryId(dto.getCategoryId())
                .date(dto.getDate())
                .note(dto.getNote())
                .userId(userId)
                .build();
        r = recordRepository.save(r);
        log.info("用户 {} 新增支出记录 {}", userId, r.getId());
        return toDTO(r);
    }

    @Transactional
    public RecordDTO update(String id, RecordDTO dto) {
        String userId = UserContext.getUserId();
        Record r = recordRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new ResourceNotFoundException("支出记录", id));
        r.setAmount(dto.getAmount());
        r.setCategoryId(dto.getCategoryId());
        r.setDate(dto.getDate());
        r.setNote(dto.getNote());
        r = recordRepository.save(r);
        log.info("用户 {} 更新支出记录 {}", userId, id);
        return toDTO(r);
    }

    @Transactional
    public void delete(String id) {
        String userId = UserContext.getUserId();
        Record r = recordRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new ResourceNotFoundException("支出记录", id));
        recordRepository.delete(r);
        log.info("用户 {} 删除支出记录 {}", userId, id);
    }

    public List<RecordDTO> findByDateRange(LocalDate start, LocalDate end, String categoryId) {
        String userId = UserContext.getUserId();
        List<Record> list;
        if (categoryId != null && !categoryId.isBlank()) {
            list = recordRepository.findByUserIdAndCategoryIdAndDateBetweenOrderByDateDesc(
                    userId, categoryId, start, end);
        } else {
            list = recordRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BigDecimal getTotalByDateRange(LocalDate start, LocalDate end, String categoryId) {
        List<RecordDTO> list = findByDateRange(start, end, categoryId);
        return list.stream().map(RecordDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private RecordDTO toDTO(Record r) {
        RecordDTO dto = new RecordDTO();
        dto.setId(r.getId());
        dto.setAmount(r.getAmount());
        dto.setCategoryId(r.getCategoryId());
        dto.setDate(r.getDate());
        dto.setNote(r.getNote());
        return dto;
    }
}
