package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Budget;
import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.BudgetRepository;
import com.cashe.backend.repository.CategoryRepository;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.repository.TransactionRepository;
import com.cashe.backend.service.dto.*;
import com.cashe.backend.service.mapper.BudgetMapper;
import com.cashe.backend.service.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyRepository currencyRepository;
    private final CategoryRepository categoryRepository;

    private BigDecimal calculateCurrentSpending(Budget budget) {
        if (budget == null || budget.getUser() == null || budget.getCategories() == null
                || budget.getCategories().isEmpty()) {
            return BigDecimal.ZERO;
        }

        User user = budget.getUser();
        Set<Category> categories = budget.getCategories();
        LocalDate rawStartDate = budget.getStartDate();
        LocalDate rawEndDate = budget.getEndDate();

        OffsetDateTime budgetPeriodStartDateTime = rawStartDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime budgetPeriodEndDateTime;

        if (rawEndDate != null) {
            budgetPeriodEndDateTime = rawEndDate.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toOffsetDateTime();
        } else {
            switch (budget.getPeriodType()) {
                case WEEKLY:
                    budgetPeriodEndDateTime = rawStartDate.plusWeeks(1).minusDays(1).atTime(LocalTime.MAX)
                            .atZone(ZoneOffset.UTC).toOffsetDateTime();
                    break;
                case MONTHLY:
                    budgetPeriodEndDateTime = rawStartDate.plusMonths(1).minusDays(1).atTime(LocalTime.MAX)
                            .atZone(ZoneOffset.UTC).toOffsetDateTime();
                    break;
                case YEARLY:
                    budgetPeriodEndDateTime = rawStartDate.plusYears(1).minusDays(1).atTime(LocalTime.MAX)
                            .atZone(ZoneOffset.UTC).toOffsetDateTime();
                    break;
                case CUSTOM:
                default:
                    if (Boolean.TRUE.equals(budget.getIsActive())) {
                        budgetPeriodEndDateTime = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MAX)
                                .atZone(ZoneOffset.UTC).toOffsetDateTime();
                    } else {
                        return BigDecimal.ZERO;
                    }
                    budgetPeriodEndDateTime = budgetPeriodEndDateTime.plusDays(1).with(LocalTime.MIN);
            }
        }

        OffsetDateTime queryEndDate = budgetPeriodEndDateTime.plusDays(1).with(LocalTime.MIN);

        BigDecimal sum = transactionRepository.sumByUserAndCategoriesInAndTransactionDateBetweenAndEntryTypeAndStatus(
                user,
                categories,
                budgetPeriodStartDateTime,
                queryEndDate,
                com.cashe.backend.domain.enums.TransactionEntryType.DEBIT);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    private Set<CategoryDto> getCategoryDtos(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptySet();
        }
        return categories.stream().map(CategoryMapper::toDto).collect(Collectors.toSet());
    }

    private BudgetDto mapBudgetToDto(Budget budget) {
        BigDecimal currentSpending = calculateCurrentSpending(budget);
        Set<CategoryDto> categoryDtos = getCategoryDtos(budget.getCategories());
        return BudgetMapper.toDto(budget, currentSpending, categoryDtos);
    }

    @Override
    @Transactional
    public BudgetDto createBudget(BudgetCreateRequest createRequest, User user) {
        budgetRepository.findByNameAndUser(createRequest.getName(), user)
                .ifPresent(b -> {
                    throw new OperationNotAllowedException(
                            "Budget with name '" + createRequest.getName() + "' already exists for this user.");
                });

        Currency currency = currencyRepository.findById(createRequest.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", createRequest.getCurrencyCode()));

        Set<Category> categories = new HashSet<>();
        if (createRequest.getCategoryIds() != null && !createRequest.getCategoryIds().isEmpty()) {
            categories = createRequest.getCategoryIds().stream()
                    .map(categoryId -> {
                        Category cat = categoryRepository.findByIdAndUser(categoryId, user)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                        categoryId + " for user " + user.getId()));
                        if (Boolean.TRUE.equals(cat.getIsArchived())) {
                            throw new OperationNotAllowedException(
                                    "Cannot assign an archived category (" + cat.getName() + ") to a budget.");
                        }
                        return cat;
                    })
                    .collect(Collectors.toSet());
        }

        Budget budget = BudgetMapper.toEntity(createRequest, user, currency, categories);
        Budget savedBudget = budgetRepository.save(budget);
        return mapBudgetToDto(savedBudget);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BudgetDto> getBudgetByIdAndUser(Long id, User user) {
        return budgetRepository.findByIdAndUser(id, user).map(this::mapBudgetToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetDto> findBudgets(User user, Specification<Budget> spec, Pageable pageable) {
        Specification<Budget> userSpec = (root, query, cb) -> cb.equal(root.get("user"), user);
        Page<Budget> budgetsPage = budgetRepository.findAll(userSpec.and(spec), pageable);
        List<BudgetDto> budgetDtos = budgetsPage.getContent().stream()
                .map(this::mapBudgetToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(budgetDtos, pageable, budgetsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDto> getActiveBudgetsByUser(User user) {
        return budgetRepository.findByUserAndIsActiveTrueOrderByStartDateDescNameAsc(user).stream()
                .map(this::mapBudgetToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetDto updateBudget(Long id, BudgetUpdateRequest updateRequest, User user) {
        Budget existingBudget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));

        if (updateRequest.getName() != null && !existingBudget.getName().equals(updateRequest.getName())) {
            budgetRepository.findByNameAndUser(updateRequest.getName(), user)
                    .ifPresent(b -> {
                        if (!b.getId().equals(existingBudget.getId())) {
                            throw new OperationNotAllowedException(
                                    "Another budget with name '" + updateRequest.getName() + "' already exists.");
                        }
                    });
        }

        Set<Category> newCategories = null;
        if (updateRequest.getCategoryIds() != null) {
            newCategories = new HashSet<>();
            if (!updateRequest.getCategoryIds().isEmpty()) {
                newCategories = updateRequest.getCategoryIds().stream()
                        .map(categoryId -> {
                            Category cat = categoryRepository.findByIdAndUser(categoryId, user)
                                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                            categoryId + " for user " + user.getId()));
                            if (Boolean.TRUE.equals(cat.getIsArchived())) {
                                throw new OperationNotAllowedException(
                                        "Cannot assign an archived category (" + cat.getName() + ") to a budget.");
                            }
                            return cat;
                        })
                        .collect(Collectors.toSet());
            }
        }

        BudgetMapper.updateEntityFromRequest(updateRequest, existingBudget, newCategories);
        Budget updatedBudget = budgetRepository.save(existingBudget);
        return mapBudgetToDto(updatedBudget);
    }

    @Override
    @Transactional
    public void deleteBudget(Long id, User user) {
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        budgetRepository.delete(budget);
    }

    private Budget getBudgetEntityByIdAndUser(Long id, User user) {
        return budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
    }

    @Override
    @Transactional
    public BudgetDto addCategoryToBudget(Long budgetId, Long categoryId, User user) {
        Budget budget = getBudgetEntityByIdAndUser(budgetId, user);
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                        categoryId + " for user " + user.getId()));

        if (Boolean.TRUE.equals(category.getIsArchived())) {
            throw new OperationNotAllowedException(
                    "Cannot add an archived category ('" + category.getName() + "') to a budget.");
        }
        budget.getCategories().add(category);
        Budget savedBudget = budgetRepository.save(budget);
        return mapBudgetToDto(savedBudget);
    }

    @Override
    @Transactional
    public BudgetDto removeCategoryFromBudget(Long budgetId, Long categoryId, User user) {
        Budget budget = getBudgetEntityByIdAndUser(budgetId, user);
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                        categoryId + " for user " + user.getId()));

        budget.getCategories().remove(category);
        Budget savedBudget = budgetRepository.save(budget);
        return mapBudgetToDto(savedBudget);
    }

    @Override
    @Transactional
    public BudgetDto toggleBudgetStatus(Long budgetId, boolean isActive, User user) {
        Budget budget = getBudgetEntityByIdAndUser(budgetId, user);
        budget.setIsActive(isActive);
        Budget savedBudget = budgetRepository.save(budget);
        return mapBudgetToDto(savedBudget);
    }
}