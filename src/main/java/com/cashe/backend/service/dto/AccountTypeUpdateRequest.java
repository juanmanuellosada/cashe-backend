package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.AccountTypeIcon;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeUpdateRequest {
    @Size(max = 100)
    private String name;

    private AccountTypeIcon icon;
}