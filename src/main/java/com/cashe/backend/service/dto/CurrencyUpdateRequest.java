package com.cashe.backend.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyUpdateRequest {

    @Size(min = 1, max = 50, message = "El nombre de la moneda debe tener entre 1 y 50 caracteres.")
    private String name; // Opcional: permitir cambiar nombre

    @Size(min = 1, max = 5, message = "El símbolo de la moneda debe tener entre 1 y 5 caracteres.")
    private String symbol; // Opcional: permitir cambiar símbolo

    // Opcional: permitir cambiar tasa de cambio. No se puede cambiar si es la
    // moneda base.
    private BigDecimal exchangeRate;

    // El código (ISO) de la moneda no se debe poder cambiar una vez creada.
    // El estado de isBaseCurrency se maneja con un endpoint dedicado
    // (setBaseCurrency).
}