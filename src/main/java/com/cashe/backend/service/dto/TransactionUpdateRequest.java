package com.cashe.backend.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionUpdateRequest {

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres.")
    private String description;

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero.")
    private BigDecimal amount;

    private LocalDate transactionDate;

    private Long categoryId; // Para cambiar la categoría

    @Size(max = 1000, message = "Las notas no pueden exceder los 1000 caracteres.")
    private String notes;

    // accountId, cardId y currencyCode no se incluyen para la actualización.
    // entryType tampoco debería cambiar después de la creación.
}