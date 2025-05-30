package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.AccountType;
import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.AccountTypeCreateRequest;
import com.cashe.backend.service.dto.AccountTypeDto;
import com.cashe.backend.service.dto.AccountTypeUpdateRequest;
import com.cashe.backend.common.exception.OperationNotAllowedException;

public class AccountTypeMapper {

    public static AccountTypeDto toDto(AccountType accountType) {
        if (accountType == null) {
            return null;
        }
        AccountTypeDto dto = new AccountTypeDto();
        dto.setId(accountType.getId());
        dto.setName(accountType.getName());
        dto.setIcon(accountType.getIcon());
        dto.setIsPredefined(accountType.getIsPredefined());
        if (accountType.getUser() != null) {
            dto.setUserId(accountType.getUser().getId());
        }
        return dto;
    }

    public static AccountType toEntity(AccountTypeCreateRequest createRequest, User user) {
        if (createRequest == null) {
            return null;
        }
        AccountType accountType = new AccountType();
        accountType.setName(createRequest.getName());
        accountType.setIcon(createRequest.getIcon());
        accountType.setUser(user); // Asociar con el usuario que lo crea
        accountType.setIsPredefined(false); // Los creados por API no son predefinidos
        return accountType;
    }

    public static void updateEntityFromRequest(AccountTypeUpdateRequest updateRequest, AccountType accountType) {
        if (updateRequest == null || accountType == null) {
            return;
        }
        if (accountType.getIsPredefined()) {
            throw new OperationNotAllowedException("Predefined account types cannot be modified.");
        }
        if (updateRequest.getName() != null) {
            accountType.setName(updateRequest.getName());
        }
        if (updateRequest.getIcon() != null) {
            accountType.setIcon(updateRequest.getIcon());
        }
    }
}