package com.cashe.backend.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyCreateRequest {

    @NotBlank(message = "El código de moneda no puede estar vacío.")
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código de moneda debe ser un código ISO 4217 de 3 letras mayúsculas.")
    private String code;

    @NotBlank(message = "El nombre de la moneda no puede estar vacío.")
    @Size(min = 1, max = 50, message = "El nombre de la moneda debe tener entre 1 y 50 caracteres.")
    private String name;

    @NotBlank(message = "El símbolo de la moneda no puede estar vacío.")
    @Size(min = 1, max = 5, message = "El símbolo de la moneda debe tener entre 1 y 5 caracteres.")
    private String symbol;

    // Tasa de cambio con respecto a la moneda base. Si esta es la moneda base, la
    // tasa es 1.
    // Opcional al crear, se puede ajustar después o si isBaseCurrency es true.
    private BigDecimal exchangeRate;

    // Por defecto, una nueva moneda no es la base.
    private Boolean isBaseCurrency = false;
}