package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.BudgetPeriodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BudgetCreateRequest {

    @NotBlank(message = "El nombre del presupuesto no puede estar vacío.")
    @Size(max = 100, message = "El nombre del presupuesto no debe exceder los 100 caracteres.")
    private String name;

    @NotNull(message = "El monto límite del presupuesto no puede ser nulo.")
    @DecimalMin(value = "0.01", message = "El monto límite debe ser mayor que cero.")
    private BigDecimal amountLimit;

    @NotBlank(message = "El código de moneda para el presupuesto no puede estar vacío.")
    private String currencyCode; // Código ISO de la moneda

    @NotNull(message = "El tipo de período del presupuesto no puede ser nulo.")
    private BudgetPeriodType periodType;

    @NotNull(message = "La fecha de inicio del presupuesto no puede ser nula.")
    private LocalDate startDate;

    private LocalDate endDate; // Opcional, depende del periodType

    private Boolean isActive = true; // Por defecto activo

    @Size(max = 500, message = "Las notas no deben exceder los 500 caracteres.")
    private String notes;

    private Set<Long> categoryIds; // IDs de las categorías a asociar

    // userId se obtendrá del contexto de seguridad, no se incluye aquí
}