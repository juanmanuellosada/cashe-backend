package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.*;
import com.cashe.backend.domain.enums.TransactionEntryType;
import com.cashe.backend.domain.enums.TransactionStatus;
import com.cashe.backend.repository.*;
import com.cashe.backend.repository.dto.CategorySummary;
import com.cashe.backend.repository.dto.TimePeriodSummary;
import com.cashe.backend.service.dto.TransactionCreateRequest;
import com.cashe.backend.service.dto.TransactionDto;
import com.cashe.backend.service.dto.TransactionUpdateRequest;
import com.cashe.backend.service.mapper.TransactionMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;
    private final @Lazy AccountService accountService;
    private final @Lazy CardService cardService;

    @Override
    @Transactional
    public TransactionDto createTransaction(TransactionCreateRequest createRequest, User user) {
        Currency currency = currencyRepository.findById(createRequest.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", createRequest.getCurrencyCode()));

        Category category = null;
        if (createRequest.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUser(createRequest.getCategoryId(), user)
                    .orElseGet(() -> categoryRepository.findByIdAndUserIsNull(createRequest.getCategoryId())
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                    createRequest.getCategoryId())));
            if (Boolean.TRUE.equals(category.getIsArchived())) {
                throw new OperationNotAllowedException("Cannot assign an archived category to a new transaction.");
            }
        }

        Account account = null;
        Card card = null;
        BigDecimal amount = createRequest.getAmount();
        TransactionEntryType entryType = createRequest.getEntryType();

        if (createRequest.getAccountId() != null) {
            account = accountService.getAccountEntityByIdAndUser(createRequest.getAccountId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "id", createRequest.getAccountId()));
            if (Boolean.TRUE.equals(account.getIsArchived())) {
                throw new OperationNotAllowedException("Cannot assign an archived account to a new transaction.");
            }
            if (!account.getCurrency().getCode().equals(currency.getCode())) {
                throw new OperationNotAllowedException("Transaction currency (" + currency.getCode() +
                        ") does not match account currency (" + account.getCurrency().getCode() + ").");
            }
            BigDecimal balanceChange = entryType == TransactionEntryType.CREDIT ? amount : amount.negate();
            accountService.updateBalance(account.getId(), balanceChange);

        } else if (createRequest.getCardId() != null) {
            card = cardService.getCardEntityByIdAndUser(createRequest.getCardId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "id", createRequest.getCardId()));
            if (Boolean.TRUE.equals(card.getIsArchived())) {
                throw new OperationNotAllowedException("Cannot assign an archived card to a new transaction.");
            }
            if (!card.getCurrency().getCode().equals(currency.getCode())) {
                throw new OperationNotAllowedException("Transaction currency (" + currency.getCode() +
                        ") does not match card currency (" + card.getCurrency().getCode() + ").");
            }
            BigDecimal balanceChange = entryType == TransactionEntryType.CREDIT ? amount : amount.negate();
            cardService.updateBalance(card.getId(), balanceChange);
        } else {
            throw new OperationNotAllowedException("Transaction must be associated with an account or a card.");
        }

        Transaction transaction = TransactionMapper.toEntity(createRequest, user, currency, category, account, card);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionMapper.toDto(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionDto> getTransactionByIdAndUser(Long id, User user) {
        return transactionRepository.findByIdAndUser(id, user).map(TransactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> getAllTransactions(User user,
            Pageable pageable,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            TransactionEntryType entryType,
            Long categoryId,
            Long accountId,
            Long cardId,
            TransactionStatus status,
            String descriptionLike) {

        Specification<Transaction> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user"), user));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (entryType != null) {
                predicates.add(criteriaBuilder.equal(root.get("entryType"), entryType));
            }
            if (categoryId != null) {
                if (categoryId.equals(0L)) {
                    predicates.add(criteriaBuilder.isNull(root.get("category")));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
                }
            }
            if (accountId != null) {
                if (accountId.equals(0L)) {
                    predicates.add(criteriaBuilder.isNull(root.get("account")));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("account").get("id"), accountId));
                }
            }
            if (cardId != null) {
                if (cardId.equals(0L)) {
                    predicates.add(criteriaBuilder.isNull(root.get("card")));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("card").get("id"), cardId));
                }
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (descriptionLike != null && !descriptionLike.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                        "%" + descriptionLike.toLowerCase() + "%"));
            }

            if (pageable.getSort().isUnsorted()) {
                query.orderBy(criteriaBuilder.desc(root.get("transactionDate")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> transactionsPage = transactionRepository.findAll(spec, pageable);
        return transactionsPage.map(TransactionMapper::toDto);
    }

    @Override
    @Transactional
    public TransactionDto updateTransaction(Long id, TransactionUpdateRequest updateRequest, User user) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!existingTransaction.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("User not authorized to update this transaction.");
        }

        BigDecimal oldAmount = existingTransaction.getAmount();
        TransactionEntryType oldEntryType = existingTransaction.getEntryType();
        Account oldAccount = existingTransaction.getAccount();
        Card oldCard = existingTransaction.getCard();

        if (oldAccount != null) {
            BigDecimal balanceReversal = oldEntryType == TransactionEntryType.CREDIT ? oldAmount.negate() : oldAmount;
            accountService.updateBalance(oldAccount.getId(), balanceReversal);
        } else if (oldCard != null) {
            BigDecimal balanceReversal = oldEntryType == TransactionEntryType.CREDIT ? oldAmount.negate() : oldAmount;
            cardService.updateBalance(oldCard.getId(), balanceReversal);
        }

        Category newCategory = existingTransaction.getCategory();
        if (updateRequest.getCategoryId() != null) {
            if (updateRequest.getCategoryId().equals(0L)) {
                newCategory = null;
            } else {
                newCategory = categoryRepository.findByIdAndUser(updateRequest.getCategoryId(), user)
                        .orElseGet(() -> categoryRepository.findByIdAndUserIsNull(updateRequest.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                        updateRequest.getCategoryId())));
                if (Boolean.TRUE.equals(newCategory.getIsArchived())) {
                    throw new OperationNotAllowedException("Cannot assign an archived category.");
                }
            }
        }
        TransactionMapper.updateEntityFromRequest(existingTransaction, updateRequest, newCategory);

        Transaction savedTransaction = transactionRepository.save(existingTransaction);

        BigDecimal newAmount = savedTransaction.getAmount();
        TransactionEntryType currentEntryType = savedTransaction.getEntryType();

        if (oldAccount != null) {
            BigDecimal balanceChange = currentEntryType == TransactionEntryType.CREDIT ? newAmount : newAmount.negate();
            accountService.updateBalance(oldAccount.getId(), balanceChange);
        } else if (oldCard != null) {
            BigDecimal balanceChange = currentEntryType == TransactionEntryType.CREDIT ? newAmount : newAmount.negate();
            cardService.updateBalance(oldCard.getId(), balanceChange);
        }

        return TransactionMapper.toDto(savedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("User not authorized to delete this transaction.");
        }

        BigDecimal amount = transaction.getAmount();
        TransactionEntryType entryType = transaction.getEntryType();
        Account account = transaction.getAccount();
        Card card = transaction.getCard();

        if (account != null) {
            BigDecimal balanceReversal = entryType == TransactionEntryType.CREDIT ? amount.negate() : amount;
            accountService.updateBalance(account.getId(), balanceReversal);
        } else if (card != null) {
            BigDecimal balanceReversal = entryType == TransactionEntryType.CREDIT ? amount.negate() : amount;
            cardService.updateBalance(card.getId(), balanceReversal);
        }

        transactionRepository.delete(transaction);
    }

    @Override
    @Transactional
    public TransactionDto approveTransaction(Long id, User user) {
        return updateTransactionStatus(id, TransactionStatus.APPROVED, user);
    }

    @Override
    @Transactional
    public TransactionDto rejectTransaction(Long id, User user) {
        return updateTransactionStatus(id, TransactionStatus.REJECTED, user);
    }

    @Override
    @Transactional
    public TransactionDto updateTransactionStatus(Long id, TransactionStatus newStatus, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        transaction.setStatus(newStatus);
        return TransactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummary> getCategorySummaries(User user, OffsetDateTime startDate, OffsetDateTime endDate,
            String entryTypeString) {
        TransactionEntryType entryType = null;
        if (entryTypeString != null && !entryTypeString.trim().isEmpty()) {
            try {
                entryType = TransactionEntryType.valueOf(entryTypeString.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid entryTypeString provided: {}. Fetching for all types.", entryTypeString);
            }
        }
        return transactionRepository.findCategorySummariesByUserAndDateRangeAndEntryType(user, startDate, endDate,
                entryType);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getSummaryOverTime(User user, OffsetDateTime startDate,
            OffsetDateTime endDate, String granularity, String entryTypeStringFromRequest) {

        String granularityForQuery;
        switch (granularity.toLowerCase()) {
            case "daily":
            case "day":
                granularityForQuery = "day";
                break;
            case "monthly":
            case "month":
                granularityForQuery = "month";
                break;
            case "yearly":
            case "year":
                granularityForQuery = "year";
                break;
            default:
                throw new IllegalArgumentException("Invalid granularity: " + granularity +
                        ". Allowed values are daily, monthly, yearly.");
        }

        TransactionEntryType entryTypeEnum = null;
        String entryTypeStringForQuery = null;
        if (entryTypeStringFromRequest != null && !entryTypeStringFromRequest.trim().isEmpty()) {
            try {
                entryTypeEnum = TransactionEntryType.valueOf(entryTypeStringFromRequest.toUpperCase());
                entryTypeStringForQuery = entryTypeEnum.name();
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid entryTypeString provided for getSummaryOverTime: {}. Fetching for all types.",
                        entryTypeStringFromRequest);
            }
        }

        List<Object[]> results = transactionRepository.findSummaryOverTimeNative(
                user.getId(), startDate, endDate, granularityForQuery, entryTypeStringForQuery);

        List<TimePeriodSummary> summaries = results.stream().map(row -> {
            String periodLabel = (String) row[0];
            TransactionEntryType type = TransactionEntryType.valueOf((String) row[1]);
            BigDecimal totalAmount = (BigDecimal) row[2];
            Long transactionCount = ((Number) row[3]).longValue();
            return new TimePeriodSummary(periodLabel, type, totalAmount, transactionCount);
        }).collect(Collectors.toList());

        Map<TransactionEntryType, List<TimePeriodSummary>> groupedByType = summaries.stream()
                .collect(Collectors.groupingBy(TimePeriodSummary::getEntryType));

        Map<String, List<Map<String, Object>>> finalResult = new LinkedHashMap<>();
        groupedByType.forEach((type, periodSummaries) -> {
            List<Map<String, Object>> periodData = periodSummaries.stream()
                    .map(summary -> {
                        Map<String, Object> dataPoint = new LinkedHashMap<>();
                        dataPoint.put("period", summary.getPeriodLabel());
                        dataPoint.put("totalAmount", summary.getTotalAmount());
                        dataPoint.put("transactionCount", summary.getTransactionCount());
                        return dataPoint;
                    })
                    .collect(Collectors.toList());
            finalResult.put(type.name(), periodData);
        });

        return finalResult;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, BigDecimal>> getCashFlow(User user, int year, Optional<Integer> monthOpt) {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();

        if (monthOpt.isPresent()) {
            int month = monthOpt.get();
            YearMonth yearMonth = YearMonth.of(year, month);
            OffsetDateTime startDate = yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            OffsetDateTime endDate = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneOffset.UTC)
                    .toOffsetDateTime();

            BigDecimal income = transactionRepository.sumAmountByUserAndDateRangeAndEntryType(user, startDate, endDate,
                    TransactionEntryType.CREDIT);
            BigDecimal expense = transactionRepository.sumAmountByUserAndDateRangeAndEntryType(user, startDate, endDate,
                    TransactionEntryType.DEBIT);
            income = income == null ? BigDecimal.ZERO : income;
            expense = expense == null ? BigDecimal.ZERO : expense;
            BigDecimal netFlow = income.subtract(expense);

            Map<String, BigDecimal> monthlyData = new LinkedHashMap<>();
            monthlyData.put("income", income);
            monthlyData.put("expense", expense);
            monthlyData.put("netFlow", netFlow);

            String monthName = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH).toUpperCase();
            result.put(monthName, monthlyData);

        } else {
            for (int m = 1; m <= 12; m++) {
                YearMonth yearMonth = YearMonth.of(year, m);
                OffsetDateTime startDate = yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
                OffsetDateTime endDate = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneOffset.UTC)
                        .toOffsetDateTime();

                BigDecimal income = transactionRepository.sumAmountByUserAndDateRangeAndEntryType(user, startDate,
                        endDate, TransactionEntryType.CREDIT);
                BigDecimal expense = transactionRepository.sumAmountByUserAndDateRangeAndEntryType(user, startDate,
                        endDate, TransactionEntryType.DEBIT);
                income = income == null ? BigDecimal.ZERO : income;
                expense = expense == null ? BigDecimal.ZERO : expense;
                BigDecimal netFlow = income.subtract(expense);

                Map<String, BigDecimal> monthlyData = new LinkedHashMap<>();
                monthlyData.put("income", income);
                monthlyData.put("expense", expense);
                monthlyData.put("netFlow", netFlow);

                String monthName = Month.of(m).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH).toUpperCase();
                result.put(monthName, monthlyData);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getFinancialStatistics(User user, OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            logger.warn("getFinancialStatistics called with invalid date range: startDate={}, endDate={}", startDate,
                    endDate);
            return Collections.emptyMap();
        }

        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndDateRangeAndEntryType(user, startDate, endDate,
                TransactionEntryType.CREDIT);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserAndDateRangeAndEntryType(user, startDate,
                endDate, TransactionEntryType.DEBIT);

        totalIncome = totalIncome == null ? BigDecimal.ZERO : totalIncome;
        totalExpense = totalExpense == null ? BigDecimal.ZERO : totalExpense;

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        long numberOfDays = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
        if (numberOfDays <= 0)
            numberOfDays = 1;

        BigDecimal averageDailyIncome = totalIncome.divide(new BigDecimal(numberOfDays), 2, RoundingMode.HALF_UP);
        BigDecimal averageDailyExpense = totalExpense.divide(new BigDecimal(numberOfDays), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> statistics = new LinkedHashMap<>();
        statistics.put("totalIncome", totalIncome);
        statistics.put("totalExpense", totalExpense);
        statistics.put("netSavings", netSavings);
        statistics.put("averageDailyIncome", averageDailyIncome);
        statistics.put("averageDailyExpense", averageDailyExpense);
        statistics.put("numberOfDaysInPeriod", new BigDecimal(numberOfDays));

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByUser(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user, pageable)
                .map(TransactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByCard(Long cardId, User user, Pageable pageable) {
        Card card = cardService.getCardEntityByIdAndUser(cardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
        return transactionRepository.findByCardOrderByTransactionDateDesc(card, pageable)
                .map(TransactionMapper::toDto);
    }
}