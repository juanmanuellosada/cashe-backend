package com.cashe.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String email;
    private String fullName;
    private String googleId; // Podría ser útil para el frontend saber si está vinculado
    private boolean isActive;
    private String defaultCurrencyCode;
    private String preferredLanguage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // No incluir passwordHash ni otra información sensible
    // Podríamos añadir profileImageUrl si lo implementamos
    // private String profileImageUrl;
}