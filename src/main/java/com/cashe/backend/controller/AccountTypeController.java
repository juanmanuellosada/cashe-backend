package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.AccountTypeService;
import com.cashe.backend.service.dto.AccountTypeCreateRequest;
import com.cashe.backend.service.dto.AccountTypeDto;
import com.cashe.backend.service.dto.AccountTypeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-types")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccountTypeController {

    private final AccountTypeService accountTypeService;

    @PostMapping
    public ResponseEntity<AccountTypeDto> createAccountType(
            @Valid @RequestBody AccountTypeCreateRequest createRequest,
            @AuthenticationPrincipal User currentUser) {
        AccountTypeDto createdAccountType = accountTypeService.createAccountType(createRequest, currentUser);
        return new ResponseEntity<>(createdAccountType, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public ResponseEntity<List<AccountTypeDto>> getAccountTypesForUser(@AuthenticationPrincipal User currentUser) {
        List<AccountTypeDto> accountTypes = accountTypeService.getAccountTypesForUser(currentUser);
        return ResponseEntity.ok(accountTypes);
    }

    @GetMapping("/predefined")
    public ResponseEntity<List<AccountTypeDto>> getPredefinedAccountTypes() {
        List<AccountTypeDto> predefinedAccountTypes = accountTypeService.getPredefinedAccountTypes();
        return ResponseEntity.ok(predefinedAccountTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountTypeDto> getAccountTypeById(@PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return accountTypeService.getAccountTypeByIdAndUser(id, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountTypeDto> updateAccountType(@PathVariable Long id,
            @Valid @RequestBody AccountTypeUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        AccountTypeDto updatedAccountType = accountTypeService.updateAccountType(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedAccountType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountType(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        accountTypeService.deleteAccountType(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}