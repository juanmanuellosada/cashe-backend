package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.AccountTypeIcon; // Asumiendo que tenemos un enum para iconos
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String name;
    // AccountType details
    private Long accountTypeId;
    private String accountTypeName;
    private AccountTypeIcon accountTypeIcon; // O String si no es un enum
    private Boolean accountTypeIsPredefined;
    // Currency details
    private String currencyCode;
    private String currencySymbol;
    private String bankName;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance; // Calculado
    private Boolean includeInNetWorth;
    private Boolean isArchived;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}