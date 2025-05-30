package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.service.dto.CardCreateRequest;
import com.cashe.backend.service.dto.CardDto;
import com.cashe.backend.service.dto.CardUpdateRequest;

public class CardMapper {

    public static CardDto toDto(Card card) {
        if (card == null) {
            return null;
        }
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setName(card.getName());
        dto.setBankName(card.getBankName());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setBillingCycleDay(card.getBillingCycleDay());
        dto.setPaymentDueDay(card.getPaymentDueDay());

        if (card.getCurrency() != null) {
            dto.setCurrencyCode(card.getCurrency().getCode());
            dto.setCurrencySymbol(card.getCurrency().getSymbol());
        }

        if (card.getLinkedPaymentAccount() != null) {
            dto.setLinkedPaymentAccountId(card.getLinkedPaymentAccount().getId());
            dto.setLinkedPaymentAccountName(card.getLinkedPaymentAccount().getName());
        }

        dto.setIsArchived(card.getIsArchived());
        // currentDebt se calculará y asignará en el servicio
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        return dto;
    }

    public static Card toEntity(CardCreateRequest createRequest, Currency currency, Account linkedPaymentAccount) {
        if (createRequest == null) {
            return null;
        }
        Card card = new Card();
        card.setName(createRequest.getName());
        card.setBankName(createRequest.getBankName());
        card.setCreditLimit(createRequest.getCreditLimit());
        card.setBillingCycleDay(createRequest.getBillingCycleDay());
        card.setPaymentDueDay(createRequest.getPaymentDueDay());
        card.setCurrency(currency); // Asignar la entidad Currency obtenida por el servicio
        card.setLinkedPaymentAccount(linkedPaymentAccount); // Puede ser null
        card.setIsArchived(false); // Por defecto al crear
        return card;
    }

    public static void updateEntityFromRequest(CardUpdateRequest updateRequest, Card card) {
        if (updateRequest == null || card == null) {
            return;
        }
        if (updateRequest.getName() != null) {
            card.setName(updateRequest.getName());
        }
        if (updateRequest.getBankName() != null) {
            card.setBankName(updateRequest.getBankName());
        }
        // Usar Objects.requireNonNullElse para permitir setear null explícitamente si
        // se desea borrar el valor
        // o manejarlo de otra forma si null significa "no cambiar". Por ahora, si es
        // null, no cambia.
        if (updateRequest.getCreditLimit() != null) {
            card.setCreditLimit(updateRequest.getCreditLimit());
        }
        if (updateRequest.getBillingCycleDay() != null) {
            card.setBillingCycleDay(updateRequest.getBillingCycleDay());
        }
        if (updateRequest.getPaymentDueDay() != null) {
            card.setPaymentDueDay(updateRequest.getPaymentDueDay());
        }
        // El linkedPaymentAccountId se manejará en el servicio para cargar la entidad
        // Account
    }
}