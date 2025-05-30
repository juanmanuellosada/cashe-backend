package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.CardTaxesReminder;
import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.CardTaxesReminderCreateRequest;
import com.cashe.backend.service.dto.CardTaxesReminderDto;
import com.cashe.backend.service.dto.CardTaxesReminderUpdateRequest;

import java.util.List;
import java.util.stream.Collectors;

public class CardTaxesReminderMapper {

    public static CardTaxesReminderDto toDto(CardTaxesReminder reminder) {
        if (reminder == null) {
            return null;
        }
        return new CardTaxesReminderDto(
                reminder.getId(),
                reminder.getCard() != null ? reminder.getCard().getId() : null,
                reminder.getDescription(),
                reminder.getEstimatedAmount(),
                reminder.getReminderDayOffset(),
                reminder.getBasedOnDayType(),
                reminder.getIsActive(),
                reminder.getNotes(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt());
    }

    public static List<CardTaxesReminderDto> toDtoList(List<CardTaxesReminder> reminders) {
        return reminders.stream().map(CardTaxesReminderMapper::toDto).collect(Collectors.toList());
    }

    public static CardTaxesReminder toEntity(CardTaxesReminderCreateRequest createRequest, Card card, User user) {
        if (createRequest == null) {
            return null;
        }
        CardTaxesReminder reminder = new CardTaxesReminder();
        reminder.setCard(card);
        reminder.setUser(user);
        reminder.setDescription(createRequest.getDescription());
        reminder.setEstimatedAmount(createRequest.getEstimatedAmount());
        reminder.setReminderDayOffset(createRequest.getReminderDayOffset());
        reminder.setBasedOnDayType(createRequest.getBasedOnDayType());
        reminder.setIsActive(createRequest.getIsActive() != null ? createRequest.getIsActive() : true);
        reminder.setNotes(createRequest.getNotes());
        return reminder;
    }

    public static void updateEntityFromRequest(CardTaxesReminderUpdateRequest updateRequest,
            CardTaxesReminder reminder) {
        if (updateRequest == null || reminder == null) {
            return;
        }
        if (updateRequest.getDescription() != null) {
            reminder.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEstimatedAmount() != null) {
            reminder.setEstimatedAmount(updateRequest.getEstimatedAmount());
        }
        if (updateRequest.getReminderDayOffset() != null) {
            reminder.setReminderDayOffset(updateRequest.getReminderDayOffset());
        }
        if (updateRequest.getBasedOnDayType() != null) {
            reminder.setBasedOnDayType(updateRequest.getBasedOnDayType());
        }
        if (updateRequest.getIsActive() != null) {
            reminder.setIsActive(updateRequest.getIsActive());
        }
        if (updateRequest.getNotes() != null) {
            reminder.setNotes(updateRequest.getNotes());
        }
    }
}