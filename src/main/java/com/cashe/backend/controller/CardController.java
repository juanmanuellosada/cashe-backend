package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.CardService;
import com.cashe.backend.service.CardTaxesReminderService;
import com.cashe.backend.service.dto.CardCreateRequest;
import com.cashe.backend.service.dto.CardDto;
import com.cashe.backend.service.dto.CardUpdateRequest;
import com.cashe.backend.service.dto.CardTaxesReminderCreateRequest;
import com.cashe.backend.service.dto.CardTaxesReminderDto;
import com.cashe.backend.service.dto.CardTaxesReminderUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CardController {

    private final CardService cardService;
    private final CardTaxesReminderService reminderService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardCreateRequest createRequest,
            @AuthenticationPrincipal User currentUser) {
        CardDto createdCard = cardService.createCard(createRequest, currentUser);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return cardService.getCardByIdAndUser(id, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getAllActiveCards(@AuthenticationPrincipal User currentUser) {
        List<CardDto> cards = cardService.getActiveCardsByUser(currentUser);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardDto>> getAllCardsIncludingArchived(@AuthenticationPrincipal User currentUser) {
        List<CardDto> cards = cardService.getAllCardsByUser(currentUser);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id,
            @Valid @RequestBody CardUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        CardDto updatedCard = cardService.updateCard(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedCard);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<CardDto> archiveCard(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        CardDto archivedCard = cardService.archiveCard(id, currentUser);
        return ResponseEntity.ok(archivedCard);
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<CardDto> unarchiveCard(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        CardDto unarchivedCard = cardService.unarchiveCard(id, currentUser);
        return ResponseEntity.ok(unarchivedCard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        cardService.deleteCard(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints de Recordatorios de Impuestos de Tarjeta ---

    @PostMapping("/{cardId}/taxes-reminders")
    public ResponseEntity<CardTaxesReminderDto> createReminderForCard(
            @PathVariable Long cardId,
            @Valid @RequestBody CardTaxesReminderCreateRequest createRequest,
            @AuthenticationPrincipal User currentUser) {
        CardTaxesReminderDto createdReminder = reminderService.createReminder(cardId, createRequest, currentUser);
        return new ResponseEntity<>(createdReminder, HttpStatus.CREATED);
    }

    @GetMapping("/{cardId}/taxes-reminders")
    public ResponseEntity<List<CardTaxesReminderDto>> getRemindersForCard(@PathVariable Long cardId,
            @AuthenticationPrincipal User currentUser) {
        List<CardTaxesReminderDto> reminders = reminderService.getAllRemindersByCardAndUser(cardId, currentUser);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/{cardId}/taxes-reminders/{reminderId}")
    public ResponseEntity<CardTaxesReminderDto> getReminderById(
            @PathVariable Long cardId,
            @PathVariable Long reminderId,
            @AuthenticationPrincipal User currentUser) {
        return reminderService.getReminderByIdAndUser(reminderId, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{cardId}/taxes-reminders/{reminderId}")
    public ResponseEntity<CardTaxesReminderDto> updateReminder(
            @PathVariable Long cardId,
            @PathVariable Long reminderId,
            @Valid @RequestBody CardTaxesReminderUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        CardTaxesReminderDto updatedReminder = reminderService.updateReminder(reminderId, updateRequest, currentUser);
        return ResponseEntity.ok(updatedReminder);
    }

    @DeleteMapping("/{cardId}/taxes-reminders/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long cardId,
            @PathVariable Long reminderId,
            @AuthenticationPrincipal User currentUser) {
        reminderService.deleteReminder(reminderId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardId}/taxes-reminders/{reminderId}/toggle-status")
    public ResponseEntity<CardTaxesReminderDto> toggleReminderStatus(
            @PathVariable Long cardId,
            @PathVariable Long reminderId,
            @AuthenticationPrincipal User currentUser) {
        CardTaxesReminderDto updatedReminder = reminderService.toggleReminderStatus(reminderId, currentUser);
        return ResponseEntity.ok(updatedReminder);
    }

    // Podríamos añadir un endpoint para obtener las transacciones de una tarjeta,
    // similar a AccountController
    // GET /api/cards/{cardId}/transactions
}