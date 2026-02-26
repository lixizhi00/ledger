package com.ledger.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class BudgetDTO {

    /** categoryId -> amount, 只存二级分类，一级由二级之和计算 */
    private Map<String, BigDecimal> yearly;

    /** categoryId -> amount, 由 yearly/12 自动生成 */
    private Map<String, BigDecimal> monthly;
}
