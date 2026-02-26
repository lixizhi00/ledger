package com.ledger.service;

import com.ledger.dto.CategoryDTO;
import com.ledger.entity.Category;
import com.ledger.exception.ResourceNotFoundException;
import com.ledger.repository.CategoryRepository;
import com.ledger.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> findAll() {
        String userId = UserContext.getUserId();
        return categoryRepository.findByUserIdOrderBySortOrderAsc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CategoryDTO> getTree() {
        List<CategoryDTO> flat = findAll();
        return flat; // 前端按 parentId 自己组树，保持与前端一致
    }

    @Transactional
    public CategoryDTO save(CategoryDTO dto) {
        String userId = UserContext.getUserId();
        Category c;
        if (dto.getId() != null && !dto.getId().isBlank()) {
            c = categoryRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("分类", dto.getId()));
            if (!userId.equals(c.getUserId())) {
                throw new IllegalArgumentException("无权修改该分类");
            }
            c.setName(dto.getName());
            if (dto.getSortOrder() != null) c.setSortOrder(dto.getSortOrder());
            log.info("用户 {} 更新分类 {}", userId, dto.getId());
        } else {
            c = Category.builder()
                    .id("c-" + UUID.randomUUID().toString().replace("-", ""))
                    .name(dto.getName())
                    .parentId(dto.getParentId())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 999)
                    .userId(userId)
                    .build();
            log.info("用户 {} 新增分类 {}", userId, c.getId());
        }
        c = categoryRepository.save(c);
        return toDTO(c);
    }

    @Transactional
    public void delete(String id) {
        String userId = UserContext.getUserId();
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", id));
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权删除该分类");
        }
        log.info("用户 {} 删除分类 {}", userId, id);
        if (c.getParentId() == null || c.getParentId().isBlank()) {
            categoryRepository.findByUserIdAndParentIdOrderBySortOrderAsc(userId, id)
                    .forEach(categoryRepository::delete);
        }
        categoryRepository.delete(c);
    }

    @Transactional
    public void updateSortOrder(String id, int sortOrder) {
        String userId = UserContext.getUserId();
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", id));
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权修改该分类");
        }
        c.setSortOrder(sortOrder);
        categoryRepository.save(c);
        log.debug("用户 {} 更新分类 {} 排序为 {}", userId, id, sortOrder);
    }

    public String getCategoryDisplayName(String id, List<CategoryDTO> categories) {
        if (id == null) return "";
        return categories.stream().filter(x -> id.equals(x.getId())).findFirst()
                .map(cat -> {
                    if (cat.getParentId() != null && !cat.getParentId().isBlank()) {
                        String parent = categories.stream().filter(p -> p.getId().equals(cat.getParentId()))
                                .map(CategoryDTO::getName).findFirst().orElse("");
                        return parent.isEmpty() ? cat.getName() : parent + " > " + cat.getName();
                    }
                    return cat.getName();
                }).orElse("未分类");
    }

    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setParentId(c.getParentId());
        dto.setSortOrder(c.getSortOrder());
        return dto;
    }
}
