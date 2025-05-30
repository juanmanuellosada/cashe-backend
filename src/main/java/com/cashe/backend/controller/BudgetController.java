package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.BudgetService;
import com.cashe.backend.service.dto.BudgetCreateRequest;
import com.cashe.backend.service.dto.BudgetDto;
import com.cashe.backend.service.dto.BudgetUpdateRequest;
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
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BudgetController {

    private final BudgetService budgetService;
    // private final UserService userService; // Eliminado

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(@Valid @RequestBody BudgetCreateRequest createRequest,
            @AuthenticationPrincipal User currentUser) {
        BudgetDto createdBudget = budgetService.createBudget(createRequest, currentUser);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudgetById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return budgetService.getBudgetByIdAndUser(id, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<BudgetDto>> findBudgets(Pageable pageable, @AuthenticationPrincipal User currentUser) {
        Page<BudgetDto> budgets = budgetService.findBudgets(currentUser, null, pageable); // null para Specification
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BudgetDto>> getActiveBudgets(@AuthenticationPrincipal User currentUser) {
        List<BudgetDto> activeBudgets = budgetService.getActiveBudgetsByUser(currentUser);
        return ResponseEntity.ok(activeBudgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(@PathVariable Long id,
            @Valid @RequestBody BudgetUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        BudgetDto updatedBudget = budgetService.updateBudget(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        budgetService.deleteBudget(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{budgetId}/categories/{categoryId}")
    public ResponseEntity<BudgetDto> addCategoryToBudget(@PathVariable Long budgetId, @PathVariable Long categoryId,
            @AuthenticationPrincipal User currentUser) {
        BudgetDto updatedBudget = budgetService.addCategoryToBudget(budgetId, categoryId, currentUser);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{budgetId}/categories/{categoryId}")
    public ResponseEntity<BudgetDto> removeCategoryFromBudget(@PathVariable Long budgetId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal User currentUser) {
        BudgetDto updatedBudget = budgetService.removeCategoryFromBudget(budgetId, categoryId, currentUser);
        return ResponseEntity.ok(updatedBudget);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<BudgetDto> toggleBudgetStatus(@PathVariable Long id, @RequestParam boolean isActive,
            @AuthenticationPrincipal User currentUser) {
        BudgetDto updatedBudget = budgetService.toggleBudgetStatus(id, isActive, currentUser);
        return ResponseEntity.ok(updatedBudget);
    }
}