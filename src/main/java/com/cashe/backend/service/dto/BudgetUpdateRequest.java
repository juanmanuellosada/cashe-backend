package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.BudgetPeriodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUpdateRequest {

    @Size(max = 100, message = "El nombre del presupuesto no debe exceder los 100 caracteres.")
    private String name;

    @DecimalMin(value = "0.01", message = "El monto límite debe ser mayor que cero.")
    private BigDecimal amountLimit;

    // No permitimos cambiar la moneda de un presupuesto existente directamente
    // aquí.
    // Podría ser una operación más compleja que requiera recalcular gastos si hay
    // diferentes monedas.
    // private String currencyCode;

    private BudgetPeriodType periodType;

    private LocalDate startDate;

    private LocalDate endDate; // Permitir nulo para quitarla si aplica

    private Boolean isActive;

    @Size(max = 500, message = "Las notas no deben exceder los 500 caracteres.")
    private String notes;

    private Set<Long> categoryIds; // Se reemplazará el conjunto existente de categorías
}