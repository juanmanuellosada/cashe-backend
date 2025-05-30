package com.cashe.backend.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// TODO: Añadir validación a nivel de clase para asegurar que toAccountId O toCardId estén presentes (y no ambos, ni ninguno).
// Se puede hacer con una anotación de validación personalizada.
@Data
public class TransferCreateRequest {
    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters long")
    private String currencyCode;

    @NotNull
    private OffsetDateTime transferDate;

    @NotNull(message = "Source account ID cannot be null")
    private Long fromAccountId;

    private Long toAccountId; // Opcional si toCardId está presente

    private Long toCardId; // Opcional si toAccountId está presente

    @Size(max = 1000)
    private String notes;
}