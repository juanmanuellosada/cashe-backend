package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.CategoryCreateRequest;
import com.cashe.backend.service.dto.CategoryDto;
import com.cashe.backend.service.dto.CategoryUpdateRequest;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(
                category.getId(),
                category.getUser() != null ? category.getUser().getId() : null,
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getIsArchived(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    public static Category toEntity(CategoryCreateRequest createRequest, User user) {
        if (createRequest == null) {
            return null;
        }
        Category category = new Category();
        category.setName(createRequest.getName());
        category.setType(createRequest.getType());
        category.setIcon(createRequest.getIcon());
        category.setColor(createRequest.getColor());
        category.setUser(user);
        category.setIsArchived(false); // Por defecto al crear
        return category;
    }

    public static void updateEntityFromRequest(CategoryUpdateRequest updateRequest, Category category) {
        if (updateRequest == null || category == null) {
            return;
        }
        if (updateRequest.getName() != null) {
            category.setName(updateRequest.getName());
        }
        if (updateRequest.getType() != null) {
            category.setType(updateRequest.getType());
        }
        // Icon y Color son opcionales en la actualización
        category.setIcon(updateRequest.getIcon());
        category.setColor(updateRequest.getColor());

        // isArchived se maneja con un endpoint dedicado (toggleArchiveStatus)
    }
}