package com.cashe.backend.service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String bankName;

    @PositiveOrZero
    private BigDecimal creditLimit;

    @Min(1)
    @Max(31)
    private Integer billingCycleDay;

    @Min(1)
    @Max(31)
    private Integer paymentDueDay;

    @NotBlank
    @Size(min = 3, max = 3) // e.g., "USD"
    private String currencyCode;

    private Long linkedPaymentAccountId;
}