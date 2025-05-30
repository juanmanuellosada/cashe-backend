package com.cashe.backend.service;

import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.CardTaxesReminder;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.CardRepository;
import com.cashe.backend.repository.CardTaxesReminderRepository;
import com.cashe.backend.service.dto.CardTaxesReminderCreateRequest;
import com.cashe.backend.service.dto.CardTaxesReminderDto;
import com.cashe.backend.service.dto.CardTaxesReminderUpdateRequest;
import com.cashe.backend.service.mapper.CardTaxesReminderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardTaxesReminderServiceImpl implements CardTaxesReminderService {

    private final CardTaxesReminderRepository reminderRepository;
    private final CardRepository cardRepository;
    private final UserService userService; // Para getAllRemindersByCurrentUser

    @Override
    @Transactional
    public CardTaxesReminderDto createReminder(Long cardId, CardTaxesReminderCreateRequest createRequest, User user) {
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId + " for user " + user.getId()));

        CardTaxesReminder reminder = CardTaxesReminderMapper.toEntity(createRequest, card, user);
        CardTaxesReminder savedReminder = reminderRepository.save(reminder);
        return CardTaxesReminderMapper.toDto(savedReminder);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CardTaxesReminderDto> getReminderByIdAndUser(Long reminderId, User user) {
        return reminderRepository.findByIdAndUser(reminderId, user)
                .map(CardTaxesReminderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardTaxesReminderDto> getAllRemindersByCurrentUser(Pageable pageable) {
        User currentUser = userService.getCurrentAuthenticatedUserEntity();
        Page<CardTaxesReminder> reminders = reminderRepository.findByUser_Id(currentUser.getId(), pageable);
        return reminders.map(CardTaxesReminderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardTaxesReminderDto> getActiveRemindersByCardAndUser(Long cardId, User user) {
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
        return CardTaxesReminderMapper.toDtoList(reminderRepository.findByCardAndUserAndIsActiveTrue(card, user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardTaxesReminderDto> getAllRemindersByCardAndUser(Long cardId, User user) {
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
        return CardTaxesReminderMapper.toDtoList(reminderRepository.findByCardAndUser(card, user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardTaxesReminderDto> getAllRemindersByUser(User user) {
        return CardTaxesReminderMapper.toDtoList(reminderRepository.findByUser(user));
    }

    @Override
    @Transactional
    public CardTaxesReminderDto updateReminder(Long reminderId, CardTaxesReminderUpdateRequest updateRequest,
            User user) {
        CardTaxesReminder existingReminder = reminderRepository.findByIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("CardTaxesReminder", "id", reminderId));

        // La Card asociada no se cambia en este método de actualización.
        CardTaxesReminderMapper.updateEntityFromRequest(updateRequest, existingReminder);
        CardTaxesReminder updatedReminder = reminderRepository.save(existingReminder);
        return CardTaxesReminderMapper.toDto(updatedReminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Long reminderId, User user) {
        CardTaxesReminder reminder = reminderRepository.findByIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("CardTaxesReminder", "id", reminderId));
        reminderRepository.delete(reminder);
    }

    @Override
    @Transactional
    public CardTaxesReminderDto toggleReminderStatus(Long reminderId, User user) {
        CardTaxesReminder reminder = reminderRepository.findByIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("CardTaxesReminder", "id", reminderId));
        reminder.setIsActive(!reminder.getIsActive());
        CardTaxesReminder updatedReminder = reminderRepository.save(reminder);
        return CardTaxesReminderMapper.toDto(updatedReminder);
    }
}