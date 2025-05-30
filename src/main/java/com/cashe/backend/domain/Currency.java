package com.cashe.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    @Id
    @Size(min = 3, max = 3)
    @Column(length = 3, nullable = false, updatable = false)
    private String code; // Ej. 'USD', 'EUR', 'ARS'

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name; // Ej. 'US Dollar', 'Euro'

    @Size(max = 5)
    @Column(length = 5)
    private String symbol; // Ej. '$', '€'

    @NotNull
    @Column(name = "exchange_rate", precision = 19, scale = 4)
    private BigDecimal exchangeRate; // Tasa de cambio con respecto a la moneda base

    @NotNull
    @Column(name = "is_base_currency", nullable = false)
    private Boolean isBaseCurrency = false;

    @Column(name = "last_updated_rate", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastUpdatedRate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (lastUpdatedRate == null) {
            lastUpdatedRate = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        lastUpdatedRate = OffsetDateTime.now();
    }
}