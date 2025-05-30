package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.CardTaxesReminderCreateRequest;
import com.cashe.backend.service.dto.CardTaxesReminderDto;
import com.cashe.backend.service.dto.CardTaxesReminderUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CardTaxesReminderService {

    CardTaxesReminderDto createReminder(Long cardId, CardTaxesReminderCreateRequest createRequest, User user);

    Optional<CardTaxesReminderDto> getReminderByIdAndUser(Long reminderId, User user);

    Page<CardTaxesReminderDto> getAllRemindersByCurrentUser(Pageable pageable);

    List<CardTaxesReminderDto> getActiveRemindersByCardAndUser(Long cardId, User user);

    List<CardTaxesReminderDto> getAllRemindersByCardAndUser(Long cardId, User user);

    List<CardTaxesReminderDto> getAllRemindersByUser(User user);

    CardTaxesReminderDto updateReminder(Long reminderId, CardTaxesReminderUpdateRequest updateRequest, User user);

    void deleteReminder(Long reminderId, User user);

    CardTaxesReminderDto toggleReminderStatus(Long reminderId, User user);

}