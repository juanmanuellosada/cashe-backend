package com.cashe.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferDto {
    private Long id;
    private Long userId;
    private String description;
    private BigDecimal amount;
    private String currencyCode;
    private OffsetDateTime transferDate;
    private Long fromAccountId;
    private String fromAccountName;
    private Long toAccountId;
    private String toAccountName;
    private Long toCardId;
    private String toCardName;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}