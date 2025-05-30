package com.cashe.backend.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre completo no puede estar vacío.")
    @Size(max = 255, message = "El nombre completo no puede exceder los 255 caracteres.")
    private String fullName;

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El email debe tener un formato válido.")
    @Size(max = 255, message = "El email no puede exceder los 255 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    // Podríamos añadir validaciones de complejidad de contraseña aquí si es
    // necesario (ej. @Pattern)
    private String password;

    // Opcional: Incluir al momento del registro o permitir al usuario configurarlo
    // después.
    // private String defaultCurrencyCode;
    // private String preferredLanguage;
}