package com.cashe.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private Long id;
    private String name;
    private String bankName;
    private BigDecimal creditLimit;
    private Integer billingCycleDay;
    private Integer paymentDueDay;
    private String currencyCode;
    private String currencySymbol;
    private Long linkedPaymentAccountId;
    private String linkedPaymentAccountName;
    private Boolean isArchived;
    private BigDecimal currentDebt; // Calculado en el servicio
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}