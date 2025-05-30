package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.AccountTypeIcon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeDto {
    private Long id;
    private String name;
    private AccountTypeIcon icon;
    private Boolean isPredefined;
    private Long userId; // Null si es predefinido/global
}