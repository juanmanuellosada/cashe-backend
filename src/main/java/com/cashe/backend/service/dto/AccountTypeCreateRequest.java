package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.AccountTypeIcon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private AccountTypeIcon icon;
}