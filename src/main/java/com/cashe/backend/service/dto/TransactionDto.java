package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.TransactionEntryType;
import com.cashe.backend.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String description;
    private BigDecimal amount;
    private String currencyCode;
    private String currencySymbol;
    private OffsetDateTime transactionDate;
    private TransactionStatus status;
    private String notes;
    private Long categoryId;
    private String categoryName;
    private Long accountId;
    private String accountName;
    private Long cardId;
    private String cardLastFourDigits; // o nombre de la tarjeta
    private TransactionEntryType entryType;
    private Integer attachmentsCount; // Número de adjuntos
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}