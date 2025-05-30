package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Currency;
import com.cashe.backend.service.dto.CurrencyCreateRequest;
import com.cashe.backend.service.dto.CurrencyDto;
import com.cashe.backend.service.dto.CurrencyUpdateRequest;

import java.math.BigDecimal;

public class CurrencyMapper {

    public static CurrencyDto toDto(Currency currency) {
        if (currency == null) {
            return null;
        }
        return new CurrencyDto(
                currency.getCode(),
                currency.getName(),
                currency.getSymbol(),
                currency.getExchangeRate(),
                currency.getIsBaseCurrency(),
                currency.getCreatedAt(),
                currency.getUpdatedAt());
    }

    public static Currency toEntity(CurrencyCreateRequest createRequest) {
        if (createRequest == null) {
            return null;
        }
        Currency currency = new Currency();
        currency.setCode(createRequest.getCode().toUpperCase());
        currency.setName(createRequest.getName());
        currency.setSymbol(createRequest.getSymbol());
        // Si es la moneda base, la tasa de cambio es 1, sino, usar la proporcionada o
        // null (se puede validar en servicio)
        currency.setExchangeRate(Boolean.TRUE.equals(createRequest.getIsBaseCurrency()) ? BigDecimal.ONE
                : createRequest.getExchangeRate());
        currency.setIsBaseCurrency(Boolean.TRUE.equals(createRequest.getIsBaseCurrency()));
        // createdAt y updatedAt son manejados por @PrePersist/@PreUpdate en la entidad
        return currency;
    }

    public static void updateEntityFromRequest(CurrencyUpdateRequest updateRequest, Currency currency) {
        if (updateRequest == null || currency == null) {
            return;
        }

        if (updateRequest.getName() != null) {
            currency.setName(updateRequest.getName());
        }
        if (updateRequest.getSymbol() != null) {
            currency.setSymbol(updateRequest.getSymbol());
        }
        // La tasa de cambio solo se actualiza si no es la moneda base
        if (updateRequest.getExchangeRate() != null && !Boolean.TRUE.equals(currency.getIsBaseCurrency())) {
            currency.setExchangeRate(updateRequest.getExchangeRate());
        }
        // No se actualiza code ni isBaseCurrency directamente aquí
        // updatedAt será manejado por @PreUpdate en la entidad
    }
}