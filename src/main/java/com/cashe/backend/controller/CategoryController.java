package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.domain.enums.CategoryType;
import com.cashe.backend.service.CategoryService;
// import com.cashe.backend.service.UserService; // Eliminado
import com.cashe.backend.service.dto.CategoryCreateRequest;
import com.cashe.backend.service.dto.CategoryDto;
import com.cashe.backend.service.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Para getPredefinedAccountTypes (si se mantiene List en lugar de Page)

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // Aplicar a nivel de clase
public class CategoryController {

    private final CategoryService categoryService;
    // private final UserService userService; // Eliminado

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryCreateRequest createRequest,
            @AuthenticationPrincipal User currentUser) {
        CategoryDto createdCategory = categoryService.createCategory(createRequest, currentUser);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping // Cambiado de /user a la raíz, se filtra por usuario autenticado
    public ResponseEntity<Page<CategoryDto>> getUserCategories(
            Pageable pageable,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Boolean isArchived,
            @AuthenticationPrincipal User currentUser) {
        Page<CategoryDto> categories = categoryService.findCategoriesByUser(currentUser, type, isArchived, pageable);
        return ResponseEntity.ok(categories);
    }

    // Endpoint para categorías predefinidas, no necesita usuario autenticado en el
    // path
    // pero sí que el usuario esté autenticado para acceder a /api
    @GetMapping("/predefined")
    public ResponseEntity<List<CategoryDto>> getPredefinedCategories() {
        List<CategoryDto> predefinedCategories = categoryService.getGlobalCategories();
        return ResponseEntity.ok(predefinedCategories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return categoryService.getCategoryByIdAndUser(id, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        CategoryDto updatedCategory = categoryService.updateCategory(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        categoryService.deleteCategory(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-archive")
    public ResponseEntity<CategoryDto> toggleArchiveCategory(@PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        CategoryDto categoryDto = categoryService.toggleCategoryArchiveStatus(id, currentUser);
        return ResponseEntity.ok(categoryDto);
    }
}