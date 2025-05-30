package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private Long userId; // El ID del usuario al que pertenece
    private String name;
    private CategoryType type;
    private String icon;
    private String color;
    private Boolean isArchived;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // private Boolean isGlobal; // Si se implementa la lógica de categorías
    // globales no asociadas a usuario
}