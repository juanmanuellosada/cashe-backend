package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.AccountService;
import com.cashe.backend.service.dto.AccountCreateRequest;
import com.cashe.backend.service.dto.AccountDto;
import com.cashe.backend.service.dto.AccountUpdateRequest;
import com.cashe.backend.service.dto.TransactionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountCreateRequest createRequest,
            @AuthenticationPrincipal User currentUser) {
        AccountDto createdAccount = accountService.createAccount(createRequest, currentUser);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return accountService.getAccountByIdAndUser(id, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts(@AuthenticationPrincipal User currentUser) {
        List<AccountDto> accounts = accountService.getActiveAccountsByUser(currentUser);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AccountDto>> getAllAccountsIncludingArchived(@AuthenticationPrincipal User currentUser) {
        List<AccountDto> accounts = accountService.getAllAccountsByUser(currentUser);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        AccountDto updatedAccount = accountService.updateAccount(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedAccount);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<AccountDto> archiveAccount(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        AccountDto archivedAccount = accountService.archiveAccount(id, currentUser);
        return ResponseEntity.ok(archivedAccount);
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<AccountDto> unarchiveAccount(@PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        AccountDto unarchivedAccount = accountService.unarchiveAccount(id, currentUser);
        return ResponseEntity.ok(unarchivedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        accountService.deleteAccount(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<TransactionDto>> getAccountTransactions(
            @PathVariable Long accountId,
            @AuthenticationPrincipal User currentUser,
            Pageable pageable) {
        Page<TransactionDto> transactions = accountService.getTransactionsByAccount(accountId, currentUser, pageable);
        return ResponseEntity.ok(transactions);
    }
}