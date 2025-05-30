package com.cashe.backend.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Long accountTypeId;

    @NotBlank
    @Size(min = 3, max = 3) // e.g., "USD"
    private String currencyCode;

    @Size(max = 100)
    private String bankName;

    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;

    @NotNull
    private Boolean includeInNetWorth = true;
}