package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.CategoryType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    @Size(max = 100)
    private String name; // Opcional, si se envía, se actualiza

    private CategoryType type; // Opcional

    @Size(max = 50)
    private String icon; // Opcional

    @Size(max = 7)
    private String color; // Opcional

    // isArchived se manejará con endpoints dedicados (archive/unarchive)
}