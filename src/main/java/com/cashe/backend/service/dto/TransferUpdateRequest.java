package com.cashe.backend.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferUpdateRequest {

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres.")
    private String description;

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero.")
    private BigDecimal amount;

    private LocalDate transferDate;

    @Size(min = 3, max = 3, message = "El código de moneda debe tener 3 caracteres.")
    private String currencyCode;

    // No se incluyen fromAccountId, fromCardId, toAccountId, toCardId
    // para simplificar la lógica de actualización de saldos.
    // Cambiar estos requeriría una lógica más compleja, similar a borrar y crear de
    // nuevo.
}