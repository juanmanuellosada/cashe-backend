package com.cashe.backend.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateRequest {

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

    private Long linkedPaymentAccountId;
    // No se permite cambiar currencyCode aquí
}