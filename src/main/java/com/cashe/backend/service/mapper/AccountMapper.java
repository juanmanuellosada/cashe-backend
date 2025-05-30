package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.AccountType;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.service.dto.AccountCreateRequest;
import com.cashe.backend.service.dto.AccountDto;
import com.cashe.backend.service.dto.AccountUpdateRequest;

public class AccountMapper {

    public static AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }

        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setName(account.getName());

        if (account.getAccountType() != null) {
            dto.setAccountTypeId(account.getAccountType().getId());
            dto.setAccountTypeName(account.getAccountType().getName());
            dto.setAccountTypeIcon(account.getAccountType().getIcon()); // Asumiendo que AccountType tiene getIcon()
            dto.setAccountTypeIsPredefined(account.getAccountType().getIsPredefined());
        }

        if (account.getCurrency() != null) {
            dto.setCurrencyCode(account.getCurrency().getCode());
            dto.setCurrencySymbol(account.getCurrency().getSymbol());
        }

        dto.setBankName(account.getBankName());
        dto.setInitialBalance(account.getInitialBalance());
        // currentBalance se calculará en el servicio
        dto.setIncludeInNetWorth(account.getIncludeInNetWorth());
        dto.setIsArchived(account.getIsArchived());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());

        return dto;
    }

    public static Account toEntity(AccountDto dto) {
        if (dto == null) {
            return null;
        }
        Account account = new Account();
        account.setId(dto.getId());
        account.setName(dto.getName());

        // Para mapear de DTO a entidad, usualmente no reconstruimos completamente
        // las entidades asociadas (AccountType, Currency) solo con sus IDs.
        // El servicio se encargará de obtener estas entidades de la BD.
        // Aquí podríamos setear solo los IDs si fuera necesario, o dejarlo al servicio.

        account.setBankName(dto.getBankName());
        account.setInitialBalance(dto.getInitialBalance());
        account.setIncludeInNetWorth(dto.getIncludeInNetWorth());
        account.setIsArchived(dto.getIsArchived());
        // createdAt y updatedAt son gestionados por la BD o JPA.
        return account;
    }

    public static Account toEntity(AccountCreateRequest createRequest, AccountType accountType, Currency currency) {
        if (createRequest == null) {
            return null;
        }
        Account account = new Account();
        account.setName(createRequest.getName());
        account.setAccountType(accountType);
        account.setCurrency(currency);
        account.setBankName(createRequest.getBankName());
        account.setInitialBalance(createRequest.getInitialBalance());
        account.setIncludeInNetWorth(createRequest.getIncludeInNetWorth());
        account.setIsArchived(false); // Por defecto no está archivada al crear
        return account;
    }

    public static void updateEntityFromRequest(AccountUpdateRequest updateRequest, Account account) {
        if (updateRequest == null || account == null) {
            return;
        }

        if (updateRequest.getName() != null) {
            account.setName(updateRequest.getName());
        }
        // El cambio de accountTypeId se manejará en el servicio para cargar la entidad
        // AccountType
        if (updateRequest.getBankName() != null) {
            account.setBankName(updateRequest.getBankName());
        }
        if (updateRequest.getIncludeInNetWorth() != null) {
            account.setIncludeInNetWorth(updateRequest.getIncludeInNetWorth());
        }
    }
}