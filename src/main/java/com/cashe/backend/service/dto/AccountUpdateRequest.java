package com.cashe.backend.service.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    @Size(max = 100)
    private String name; // Permitir cambiar nombre

    private Long accountTypeId; // Permitir cambiar tipo

    // No permitimos cambiar la moneda de una cuenta existente fácilmente.
    // private String currencyCode;

    @Size(max = 100)
    private String bankName;

    // El saldo inicial generalmente no se actualiza, se ajusta con transacciones.
    // Para correcciones, se podría tener un endpoint/lógica especial.
    // private BigDecimal initialBalance;

    private Boolean includeInNetWorth;

    // isArchived se maneja con endpoints dedicados (archive/unarchive)
}