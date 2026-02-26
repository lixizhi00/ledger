package com.ledger.controller;

import com.ledger.dto.RecordDTO;
import com.ledger.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    public ResponseEntity<RecordDTO> add(@Valid @RequestBody RecordDTO dto) {
        return ResponseEntity.ok(recordService.add(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordDTO> update(@PathVariable String id, @Valid @RequestBody RecordDTO dto) {
        return ResponseEntity.ok(recordService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        recordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RecordDTO>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String categoryId) {
        return ResponseEntity.ok(recordService.findByDateRange(start, end, categoryId));
    }
}
