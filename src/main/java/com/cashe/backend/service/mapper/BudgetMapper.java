package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Budget;
import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.BudgetCreateRequest;
import com.cashe.backend.service.dto.BudgetDto;
import com.cashe.backend.service.dto.BudgetUpdateRequest;
import com.cashe.backend.service.dto.CategoryDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BudgetMapper {

    // Necesitaremos una forma de pasar el currentSpending calculado al método
    // toDto,
    // o calcularlo dentro si el mapper tiene acceso a los servicios/repositorios
    // necesarios.
    // Por simplicidad en el mapper, asumiremos que currentSpending se pasa como
    // argumento.
    public static BudgetDto toDto(Budget budget, BigDecimal currentSpending, Set<CategoryDto> categoryDtos) {
        if (budget == null) {
            return null;
        }
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        if (budget.getUser() != null) {
            dto.setUserId(budget.getUser().getId());
        }
        dto.setName(budget.getName());
        dto.setAmountLimit(budget.getAmountLimit());
        dto.setCurrentSpending(currentSpending != null ? currentSpending : BigDecimal.ZERO);
        if (budget.getCurrency() != null) {
            dto.setCurrency(CurrencyMapper.toDto(budget.getCurrency()));
        }
        dto.setPeriodType(budget.getPeriodType());
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setIsActive(budget.getIsActive());
        dto.setNotes(budget.getNotes());
        dto.setCategories(categoryDtos != null ? categoryDtos : Collections.emptySet());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());
        return dto;
    }

    public static Budget toEntity(BudgetCreateRequest createRequest, User user, Currency currency,
            Set<Category> categories) {
        if (createRequest == null) {
            return null;
        }
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setName(createRequest.getName());
        budget.setAmountLimit(createRequest.getAmountLimit());
        budget.setCurrency(currency);
        budget.setPeriodType(createRequest.getPeriodType());
        budget.setStartDate(createRequest.getStartDate());
        budget.setEndDate(createRequest.getEndDate());
        budget.setIsActive(createRequest.getIsActive() != null ? createRequest.getIsActive() : true);
        budget.setNotes(createRequest.getNotes());
        budget.setCategories(categories != null ? categories : new HashSet<>());
        // createdAt y updatedAt son manejados por @PrePersist/@PreUpdate en la entidad
        // Budget
        return budget;
    }

    public static void updateEntityFromRequest(BudgetUpdateRequest updateRequest, Budget budget,
            Set<Category> newCategories) {
        if (updateRequest == null || budget == null) {
            return;
        }

        if (updateRequest.getName() != null) {
            budget.setName(updateRequest.getName());
        }
        if (updateRequest.getAmountLimit() != null) {
            budget.setAmountLimit(updateRequest.getAmountLimit());
        }
        if (updateRequest.getPeriodType() != null) {
            budget.setPeriodType(updateRequest.getPeriodType());
        }
        if (updateRequest.getStartDate() != null) {
            budget.setStartDate(updateRequest.getStartDate());
        }
        // endDate puede ser null para quitarla
        budget.setEndDate(updateRequest.getEndDate());

        if (updateRequest.getIsActive() != null) {
            budget.setIsActive(updateRequest.getIsActive());
        }
        if (updateRequest.getNotes() != null) {
            budget.setNotes(updateRequest.getNotes());
        }
        // Si se proporcionan nuevas categorías, se reemplazan las existentes.
        // Si newCategories es null, no se tocan (esto depende de la lógica del
        // servicio).
        // Aquí asumimos que si se pasan, se actualizan.
        if (newCategories != null) {
            budget.setCategories(newCategories);
        }
        // updatedAt será manejado por @PreUpdate en la entidad Budget
    }
}