package com.cashe.backend.service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateDescriptionRequest {
    @Size(max = 1000)
    private String description;
}