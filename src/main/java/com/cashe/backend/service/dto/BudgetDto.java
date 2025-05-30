package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.BudgetPeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {

    private Long id;
    private Long userId;
    private String name;
    private BigDecimal amountLimit;
    private BigDecimal currentSpending; // Calculado
    private CurrencyDto currency; // Usamos CurrencyDto aquí
    private BudgetPeriodType periodType;
    private LocalDate startDate;
    private LocalDate endDate; // Puede ser nulo
    private Boolean isActive;
    private String notes;
    private Set<CategoryDto> categories; // Usamos CategoryDto aquí
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}