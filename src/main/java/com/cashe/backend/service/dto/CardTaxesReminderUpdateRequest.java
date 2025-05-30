package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.BasedOnDayType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@Getter
public class CardTaxesReminderUpdateRequest {
    @Size(max = 255)
    private String description;

    @PositiveOrZero(message = "Estimated amount must be positive or zero")
    private BigDecimal estimatedAmount;

    private Integer reminderDayOffset;

    private BasedOnDayType basedOnDayType;

    private Boolean isActive;

    @Size(max = 1000)
    private String notes;
}