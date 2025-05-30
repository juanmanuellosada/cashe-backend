package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.cashe.backend.service.dto.AccountCreateRequest;
import com.cashe.backend.service.dto.AccountDto;
import com.cashe.backend.service.dto.AccountUpdateRequest;
import com.cashe.backend.service.dto.TransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cashe.backend.domain.Account;

public interface AccountService {

    AccountDto createAccount(AccountCreateRequest createRequest, User user);

    Optional<AccountDto> getAccountByIdAndUser(Long id, User user);

    List<AccountDto> getActiveAccountsByUser(User user);

    List<AccountDto> getAllAccountsByUser(User user); // Incluye archivadas

    AccountDto updateAccount(Long id, AccountUpdateRequest updateRequest, User user);

    AccountDto archiveAccount(Long id, User user);

    AccountDto unarchiveAccount(Long id, User user);

    void deleteAccount(Long id, User user); // Con validación de no estar en uso y saldo cero

    Optional<BigDecimal> getCurrentBalance(Long accountId, User user);

    // Para listar transacciones de una cuenta
    Page<TransactionDto> getTransactionsByAccount(Long accountId, User user, Pageable pageable);

    Optional<Account> getAccountEntityByIdAndUser(Long id, User user);

    void updateBalance(Long accountId, BigDecimal amountChange);
}