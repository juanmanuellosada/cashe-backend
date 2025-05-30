package com.cashe.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDto {
    private String code; // ISO 4217 (e.g., "USD", "EUR")
    private String name;
    private String symbol;
    private BigDecimal exchangeRate; // Con respecto a la moneda base
    private Boolean isBaseCurrency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}