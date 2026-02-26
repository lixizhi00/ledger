package com.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {

    private String id;

    @NotBlank
    private String name;

    private String parentId;

    private Integer sortOrder;
}
