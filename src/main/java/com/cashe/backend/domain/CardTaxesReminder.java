package com.cashe.backend.domain;

import com.cashe.backend.domain.enums.BasedOnDayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "card_taxes_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CardTaxesReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String description; // Ej. "Impuesto PAIS + Ganancias", "Seguro de Tarjeta"

    @Column(name = "estimated_amount", precision = 19, scale = 4)
    private BigDecimal estimatedAmount;

    @NotNull
    @Column(name = "reminder_day_offset", nullable = false)
    private Integer reminderDayOffset = -2; // Días ANTES del día base para recordar

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "based_on_day_type", nullable = false)
    private BasedOnDayType basedOnDayType = BasedOnDayType.BILLING_CYCLE_DAY;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}