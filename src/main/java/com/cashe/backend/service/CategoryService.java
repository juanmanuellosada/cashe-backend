package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.domain.enums.CategoryType;
import com.cashe.backend.service.dto.CategoryCreateRequest;
import com.cashe.backend.service.dto.CategoryDto;
import com.cashe.backend.service.dto.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    CategoryDto createCategory(CategoryCreateRequest createRequest, User user);

    Optional<CategoryDto> getCategoryByIdAndUser(Long id, User user);

    Page<CategoryDto> findCategoriesByUser(User user, CategoryType type, Boolean isArchived, Pageable pageable);

    List<CategoryDto> getActiveCategoriesByUser(User user);

    List<CategoryDto> getActiveCategoriesByUserAndType(User user, CategoryType type);

    CategoryDto updateCategory(Long id, CategoryUpdateRequest updateRequest, User user);

    CategoryDto toggleCategoryArchiveStatus(Long id, User user);

    void deleteCategory(Long id, User user);

    List<CategoryDto> getAllCategoriesByUser(User user);

    List<CategoryDto> getGlobalCategories();
}