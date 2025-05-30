package com.cashe.backend.repository.dto;

import com.cashe.backend.domain.enums.TransactionEntryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimePeriodSummary {
    private String periodLabel; // e.g., "2023-01-15", "2023-01", "2023"
    private TransactionEntryType entryType; // Para agrupar por ingreso/egreso
    private BigDecimal totalAmount;
    private Long transactionCount;
}