package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.BasedOnDayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@Getter
public class CardTaxesReminderCreateRequest {
    // cardId se tomará del path en el endpoint o será un argumento del método de
    // servicio

    @NotBlank
    @Size(max = 255)
    private String description;

    @PositiveOrZero(message = "Estimated amount must be positive or zero")
    private BigDecimal estimatedAmount;

    @NotNull
    private Integer reminderDayOffset; // Ej. -2 para 2 días antes, 0 para el mismo día, 1 para 1 día después

    @NotNull
    private BasedOnDayType basedOnDayType;

    private Boolean isActive = true; // Por defecto true

    @Size(max = 1000)
    private String notes;
}