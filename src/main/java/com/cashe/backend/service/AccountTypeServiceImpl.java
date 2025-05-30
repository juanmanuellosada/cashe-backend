package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.AccountType;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.AccountRepository;
import com.cashe.backend.repository.AccountTypeRepository;
import com.cashe.backend.service.dto.AccountTypeCreateRequest;
import com.cashe.backend.service.dto.AccountTypeDto;
import com.cashe.backend.service.dto.AccountTypeUpdateRequest;
import com.cashe.backend.service.mapper.AccountTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AccountTypeServiceImpl implements AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository; // Para validar si el tipo de cuenta está en uso

    @Override
    @Transactional
    public AccountTypeDto createAccountType(AccountTypeCreateRequest createRequest, User user) {
        accountTypeRepository.findByNameAndUser(createRequest.getName(), user)
                .ifPresent(at -> {
                    throw new OperationNotAllowedException(
                            "Account type with name '" + createRequest.getName() + "' already exists for this user.");
                });

        AccountType accountType = AccountTypeMapper.toEntity(createRequest, user);
        AccountType savedAccountType = accountTypeRepository.save(accountType);
        return AccountTypeMapper.toDto(savedAccountType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountTypeDto> getAccountTypeByIdAndUser(Long id, User user) {
        return accountTypeRepository.findByIdAndUserAndIsPredefinedFalse(id, user)
                .map(AccountTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountTypeDto> getAccountTypesForUser(User user) {
        List<AccountType> userTypes = accountTypeRepository.findByUserAndIsPredefinedFalseOrderByNameAsc(user);
        List<AccountType> predefinedTypes = accountTypeRepository.findByIsPredefinedTrueOrderByNameAsc();

        return Stream.concat(predefinedTypes.stream(), userTypes.stream())
                .map(AccountTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountTypeDto updateAccountType(Long id, AccountTypeUpdateRequest updateRequest, User user) {
        AccountType accountType = accountTypeRepository.findByIdAndUserAndIsPredefinedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account type", "id", id + " for this user, or it's a predefined type."));

        if (updateRequest.getName() != null && !updateRequest.getName().equals(accountType.getName())) {
            accountTypeRepository.findByNameAndUser(updateRequest.getName(), user)
                    .ifPresent(existingWithNewName -> {
                        if (!existingWithNewName.getId().equals(accountType.getId())) {
                            throw new OperationNotAllowedException(
                                    "Another account type with name '" + updateRequest.getName()
                                            + "' already exists for this user.");
                        }
                    });
        }

        AccountTypeMapper.updateEntityFromRequest(updateRequest, accountType);
        AccountType updatedAccountType = accountTypeRepository.save(accountType);
        return AccountTypeMapper.toDto(updatedAccountType);
    }

    @Override
    @Transactional
    public void deleteAccountType(Long id, User user) {
        AccountType accountType = accountTypeRepository.findByIdAndUserAndIsPredefinedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account type", "id", id
                        + " for this user, or it's a predefined type and cannot be deleted."));

        long accountsUsingType = accountRepository.countByAccountType(accountType);
        if (accountsUsingType > 0) {
            throw new OperationNotAllowedException(
                    "Cannot delete account type: it is currently in use by " + accountsUsingType + " account(s).");
        }

        accountTypeRepository.delete(accountType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountTypeDto> getPredefinedAccountTypes() {
        return accountTypeRepository.findByIsPredefinedTrueOrderByNameAsc().stream()
                .map(AccountTypeMapper::toDto)
                .collect(Collectors.toList());
    }
}