package com.cashe.backend.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalAmount;
    private Long transactionCount;
}