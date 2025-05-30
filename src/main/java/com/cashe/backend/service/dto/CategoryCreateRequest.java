package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private CategoryType type;

    @Size(max = 50)
    private String icon; // Opcional

    @Size(max = 7)
    private String color; // Opcional, ej. "#FF5733"
}