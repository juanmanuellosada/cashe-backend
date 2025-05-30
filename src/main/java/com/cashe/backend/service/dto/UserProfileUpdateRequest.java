package com.cashe.backend.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @Size(max = 255, message = "El nombre completo no puede exceder los 255 caracteres.")
    private String fullName;

    @Size(max = 3, message = "El código de moneda debe tener 3 caracteres.") // Asumiendo códigos ISO 4217
    private String defaultCurrencyCode;

    @Size(max = 10, message = "El código de idioma no puede exceder los 10 caracteres.") // Ej. "es", "en-US"
    private String preferredLanguage;

}