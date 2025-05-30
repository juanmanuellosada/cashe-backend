package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.AccountType;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.Transaction;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.AccountRepository;
import com.cashe.backend.repository.AccountTypeRepository;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.repository.TransactionRepository;
import com.cashe.backend.repository.TransferRepository;
import com.cashe.backend.service.dto.AccountCreateRequest;
import com.cashe.backend.service.dto.AccountDto;
import com.cashe.backend.service.dto.AccountUpdateRequest;
import com.cashe.backend.service.dto.TransactionDto;
import com.cashe.backend.service.mapper.AccountMapper;
import com.cashe.backend.service.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;

    @Override
    @Transactional
    public AccountDto createAccount(AccountCreateRequest createRequest, User user) {
        accountRepository.findByNameAndUser(createRequest.getName(), user)
                .ifPresent(acc -> {
                    throw new OperationNotAllowedException(
                            "Account with name '" + createRequest.getName() + "' already exists for this user.");
                });

        AccountType accountType = accountTypeRepository.findById(createRequest.getAccountTypeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("AccountType", "id", createRequest.getAccountTypeId()));

        if (accountType.getUser() != null && !accountType.getUser().getId().equals(user.getId())
                && !accountType.getIsPredefined()) {
            throw new OperationNotAllowedException("AccountType does not belong to the user and is not predefined.");
        }

        Currency currency = currencyRepository.findById(createRequest.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", createRequest.getCurrencyCode()));

        Account account = AccountMapper.toEntity(createRequest, accountType, currency);
        account.setUser(user);

        Account savedAccount = accountRepository.save(account);
        AccountDto accountDto = AccountMapper.toDto(savedAccount);
        if (accountDto != null) {
            accountDto.setCurrentBalance(savedAccount.getInitialBalance());
        }
        return accountDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDto> getAccountByIdAndUser(Long id, User user) {
        Optional<Account> accountOpt = accountRepository.findByIdAndUser(id, user);
        return accountOpt.map(account -> {
            AccountDto dto = AccountMapper.toDto(account);
            if (dto != null) {
                BigDecimal currentBalance = getCurrentBalance(account.getId(), user)
                        .orElse(account.getInitialBalance());
                dto.setCurrentBalance(currentBalance);
            }
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getActiveAccountsByUser(User user) {
        return accountRepository.findByUserAndIsArchivedFalseOrderByNameAsc(user).stream()
                .map(account -> {
                    AccountDto dto = AccountMapper.toDto(account);
                    if (dto != null) {
                        BigDecimal currentBalance = getCurrentBalance(account.getId(), user)
                                .orElse(account.getInitialBalance());
                        dto.setCurrentBalance(currentBalance);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccountsByUser(User user) {
        return accountRepository.findByUserOrderByNameAsc(user).stream()
                .map(account -> {
                    AccountDto dto = AccountMapper.toDto(account);
                    if (dto != null) {
                        BigDecimal currentBalance = getCurrentBalance(account.getId(), user)
                                .orElse(account.getInitialBalance());
                        dto.setCurrentBalance(currentBalance);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDto updateAccount(Long id, AccountUpdateRequest updateRequest, User user) {
        Account existingAccount = getAccountEntityByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        if (updateRequest.getName() != null && !existingAccount.getName().equals(updateRequest.getName())) {
            accountRepository.findByNameAndUser(updateRequest.getName(), user)
                    .ifPresent(acc -> {
                        if (!acc.getId().equals(existingAccount.getId())) {
                            throw new OperationNotAllowedException(
                                    "Another account with name '" + updateRequest.getName()
                                            + "' already exists for this user.");
                        }
                    });
        }

        AccountMapper.updateEntityFromRequest(updateRequest, existingAccount);

        if (updateRequest.getAccountTypeId() != null &&
                (existingAccount.getAccountType() == null
                        || !existingAccount.getAccountType().getId().equals(updateRequest.getAccountTypeId()))) {
            AccountType newAccountType = accountTypeRepository.findById(updateRequest.getAccountTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "AccountType", "id", updateRequest.getAccountTypeId()));
            if (newAccountType.getUser() != null && !newAccountType.getUser().getId().equals(user.getId())
                    && !newAccountType.getIsPredefined()) {
                throw new OperationNotAllowedException(
                        "New AccountType does not belong to the user and is not predefined.");
            }
            existingAccount.setAccountType(newAccountType);
        }

        Account savedAccount = accountRepository.save(existingAccount);
        AccountDto accountDto = AccountMapper.toDto(savedAccount);
        if (accountDto != null) {
            BigDecimal currentBalance = getCurrentBalance(savedAccount.getId(), user)
                    .orElse(savedAccount.getInitialBalance());
            accountDto.setCurrentBalance(currentBalance);
        }
        return accountDto;
    }

    @Override
    @Transactional
    public AccountDto archiveAccount(Long id, User user) {
        Account account = getAccountEntityByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setIsArchived(true);
        Account savedAccount = accountRepository.save(account);
        AccountDto accountDto = AccountMapper.toDto(savedAccount);
        if (accountDto != null) {
            BigDecimal currentBalance = getCurrentBalance(savedAccount.getId(), user)
                    .orElse(savedAccount.getInitialBalance());
            accountDto.setCurrentBalance(currentBalance);
        }
        return accountDto;
    }

    @Override
    @Transactional
    public AccountDto unarchiveAccount(Long id, User user) {
        Account account = getAccountEntityByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setIsArchived(false);
        Account savedAccount = accountRepository.save(account);
        AccountDto accountDto = AccountMapper.toDto(savedAccount);
        if (accountDto != null) {
            BigDecimal currentBalance = getCurrentBalance(savedAccount.getId(), user)
                    .orElse(savedAccount.getInitialBalance());
            accountDto.setCurrentBalance(currentBalance);
        }
        return accountDto;
    }

    @Override
    @Transactional
    public void deleteAccount(Long id, User user) {
        Account accountToDelete = getAccountEntityByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        long transactionCount = transactionRepository.countByAccount(accountToDelete);
        if (transactionCount > 0) {
            throw new OperationNotAllowedException("Account cannot be deleted because it has associated transactions.");
        }

        long transferCountAsOrigin = transferRepository.countByFromAccount(accountToDelete);
        long transferCountAsDestination = transferRepository.countByToAccount(accountToDelete);
        if (transferCountAsOrigin > 0 || transferCountAsDestination > 0) {
            throw new OperationNotAllowedException("Account cannot be deleted because it is part of a transfer.");
        }

        BigDecimal currentBalance = getCurrentBalance(id, user).orElse(accountToDelete.getInitialBalance());
        if (currentBalance.compareTo(BigDecimal.ZERO) != 0) {
            throw new OperationNotAllowedException(
                    "Account cannot be deleted because its current balance is not zero. Current balance: "
                            + currentBalance);
        }

        accountRepository.delete(accountToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getCurrentBalance(Long accountId, User user) {
        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        BigDecimal totalCredit = transactionRepository.sumAmountByAccountAndEntryType(account,
                com.cashe.backend.domain.enums.TransactionEntryType.CREDIT);
        BigDecimal totalDebit = transactionRepository.sumAmountByAccountAndEntryType(account,
                com.cashe.backend.domain.enums.TransactionEntryType.DEBIT);

        BigDecimal totalTransfersIn = transferRepository.sumAmountByDestinationAccount(account);
        BigDecimal totalTransfersOut = transferRepository.sumAmountByOriginAccount(account);

        totalCredit = totalCredit == null ? BigDecimal.ZERO : totalCredit;
        totalDebit = totalDebit == null ? BigDecimal.ZERO : totalDebit;
        totalTransfersIn = totalTransfersIn == null ? BigDecimal.ZERO : totalTransfersIn;
        totalTransfersOut = totalTransfersOut == null ? BigDecimal.ZERO : totalTransfersOut;

        return Optional.of(account.getInitialBalance()
                .add(totalCredit)
                .subtract(totalDebit)
                .add(totalTransfersIn)
                .subtract(totalTransfersOut));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByAccount(Long accountId, User user, Pageable pageable) {
        Account account = getAccountEntityByIdAndUser(accountId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        Page<Transaction> transactionsPage = transactionRepository.findByAccountOrderByTransactionDateDesc(account,
                pageable);
        return transactionsPage.map(TransactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountEntityByIdAndUser(Long id, User user) {
        return accountRepository.findByIdAndUser(id, user);
    }

    @Override
    @Transactional
    public void updateBalance(Long accountId, BigDecimal amountChange) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        account.setBalance(account.getBalance().add(amountChange));
        accountRepository.save(account);
        logger.info("Updated balance for account ID {}: change={}, new balance={}",
                accountId, amountChange, account.getBalance());
    }
}