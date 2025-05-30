package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.BasedOnDayType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardTaxesReminderDto {
    private Long id;
    private Long cardId;
    private String description;
    private BigDecimal estimatedAmount;
    private Integer reminderDayOffset;
    private BasedOnDayType basedOnDayType;
    private Boolean isActive;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}