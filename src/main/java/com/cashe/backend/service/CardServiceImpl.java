package com.cashe.backend.service;

import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.CardRepository;
import com.cashe.backend.repository.CardTaxesReminderRepository;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.repository.TransactionRepository;
import com.cashe.backend.service.dto.CardCreateRequest;
import com.cashe.backend.service.dto.CardDto;
import com.cashe.backend.service.dto.CardUpdateRequest;
import com.cashe.backend.service.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final CardTaxesReminderRepository cardTaxesReminderRepository;
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);

    @Override
    @Transactional
    public CardDto createCard(CardCreateRequest createRequest, User user) {
        cardRepository.findByNameAndUser(createRequest.getName(), user)
                .ifPresent(c -> {
                    throw new RuntimeException(
                            "Card with name '" + createRequest.getName() + "' already exists for this user.");
                });

        Currency currency = currencyRepository.findById(createRequest.getCurrencyCode())
                .orElseThrow(() -> new RuntimeException("Invalid Currency code: " + createRequest.getCurrencyCode()));

        Account linkedPaymentAccount = null;
        if (createRequest.getLinkedPaymentAccountId() != null) {
            linkedPaymentAccount = accountService
                    .getAccountEntityByIdAndUser(createRequest.getLinkedPaymentAccountId(), user)
                    .orElseThrow(() -> new RuntimeException("Invalid linked payment Account ID specified: "
                            + createRequest.getLinkedPaymentAccountId()));
        }

        Card card = CardMapper.toEntity(createRequest, currency, linkedPaymentAccount);
        card.setUser(user);
        // isArchived ya se setea en el mapper.

        Card savedCard = cardRepository.save(card);
        CardDto cardDto = CardMapper.toDto(savedCard);
        if (cardDto != null) {
            BigDecimal currentDebt = getCurrentDebt(savedCard.getId(), user).orElse(BigDecimal.ZERO);
            cardDto.setCurrentDebt(currentDebt);
        }
        return cardDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CardDto> getCardByIdAndUser(Long id, User user) {
        return cardRepository.findByIdAndUser(id, user).map(card -> {
            CardDto dto = CardMapper.toDto(card);
            if (dto != null) {
                BigDecimal currentDebt = getCurrentDebt(card.getId(), user).orElse(BigDecimal.ZERO);
                dto.setCurrentDebt(currentDebt);
            }
            return dto;
        });
    }

    @Override
    public Optional<Card> getCardEntityByIdAndUser(Long id, User user) {
        return cardRepository.findByIdAndUser(id, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getActiveCardsByUser(User user) {
        return cardRepository.findByUserAndIsArchivedFalseOrderByNameAsc(user).stream()
                .map(card -> {
                    CardDto dto = CardMapper.toDto(card);
                    if (dto != null) {
                        BigDecimal currentDebt = getCurrentDebt(card.getId(), user).orElse(BigDecimal.ZERO);
                        dto.setCurrentDebt(currentDebt);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getAllCardsByUser(User user) {
        return cardRepository.findByUserOrderByNameAsc(user).stream() // Assuming findByUserOrderByNameAsc exists
                .map(card -> {
                    CardDto dto = CardMapper.toDto(card);
                    if (dto != null) {
                        BigDecimal currentDebt = getCurrentDebt(card.getId(), user).orElse(BigDecimal.ZERO);
                        dto.setCurrentDebt(currentDebt);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardDto updateCard(Long id, CardUpdateRequest updateRequest, User user) {
        Card existingCard = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id + " for this user."));

        if (updateRequest.getName() != null && !existingCard.getName().equals(updateRequest.getName())) {
            cardRepository.findByNameAndUser(updateRequest.getName(), user)
                    .ifPresent(c -> {
                        if (!c.getId().equals(existingCard.getId())) {
                            throw new RuntimeException("Another card with name '" + updateRequest.getName()
                                    + "' already exists for this user.");
                        }
                    });
        }

        CardMapper.updateEntityFromRequest(updateRequest, existingCard);

        // Currency no se actualiza aquí.

        if (updateRequest.getLinkedPaymentAccountId() != null) {
            if (existingCard.getLinkedPaymentAccount() == null ||
                    !existingCard.getLinkedPaymentAccount().getId().equals(updateRequest.getLinkedPaymentAccountId())) {
                Account newPaymentAccount = accountService
                        .getAccountEntityByIdAndUser(updateRequest.getLinkedPaymentAccountId(), user)
                        .orElseThrow(() -> new RuntimeException(
                                "Invalid new linked payment Account ID: " + updateRequest.getLinkedPaymentAccountId()));
                existingCard.setLinkedPaymentAccount(newPaymentAccount);
            }
        } else if (updateRequest.getLinkedPaymentAccountId() == null
                && existingCard.getLinkedPaymentAccount() != null) {
            // Si el request explícitamente quiere desvincular la cuenta.
            // Se podría requerir un campo booleano específico en el DTO para esto para más
            // claridad, ej. "unlinkPaymentAccount = true"
            // Por ahora, si el ID es null en el request, y existía una cuenta, la
            // desvinculamos.
            // PERO, updateRequest.getLinkedPaymentAccountId() es Long, no un Optional o
            // algo que distinga "no enviado" de "enviado como null".
            // Para desvincular, se necesitaría una lógica más explícita. Asumimos por ahora
            // que si no viene, no se toca,
            // y si viene un ID, se intenta actualizar.
            // Para desvincular explícitamente, se podría tener un endpoint PATCH o un campo
            // específico en el DTO.
            // La lógica actual en el mapper no setea linkedPaymentAccount si no viene en el
            // DTO, lo cual es razonable.
            // Aquí lo que se quiere es si el DTO trae un ID, se actualiza. Si el DTO *no*
            // trae un ID (campo nulo en el JSON),
            // ¿debería desvincularse? Esto es una decisión de diseño. Por ahora, no
            // desvincula si no viene en el DTO.
        }

        Card savedCard = cardRepository.save(existingCard);
        CardDto cardDto = CardMapper.toDto(savedCard);
        if (cardDto != null) {
            BigDecimal currentDebt = getCurrentDebt(savedCard.getId(), user).orElse(BigDecimal.ZERO);
            cardDto.setCurrentDebt(currentDebt);
        }
        return cardDto;
    }

    @Override
    @Transactional
    public CardDto archiveCard(Long id, User user) {
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id + " for this user."));
        card.setIsArchived(true);
        Card savedCard = cardRepository.save(card);
        CardDto cardDto = CardMapper.toDto(savedCard);
        if (cardDto != null) {
            BigDecimal currentDebt = getCurrentDebt(savedCard.getId(), user).orElse(BigDecimal.ZERO);
            cardDto.setCurrentDebt(currentDebt);
        }
        return cardDto;
    }

    @Override
    @Transactional
    public CardDto unarchiveCard(Long id, User user) {
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id + " for this user."));
        card.setIsArchived(false);
        Card savedCard = cardRepository.save(card);
        CardDto cardDto = CardMapper.toDto(savedCard);
        if (cardDto != null) {
            BigDecimal currentDebt = getCurrentDebt(savedCard.getId(), user).orElse(BigDecimal.ZERO);
            cardDto.setCurrentDebt(currentDebt);
        }
        return cardDto;
    }

    @Override
    @Transactional
    public void deleteCard(Long id, User user) {
        Card cardToDelete = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id + " for this user."));

        long debitTransactionCount = transactionRepository.countByCardAndUserAndEntryType(cardToDelete, user,
                com.cashe.backend.domain.enums.TransactionEntryType.DEBIT);
        if (debitTransactionCount > 0) {
            throw new RuntimeException("Cannot delete card: it has " + debitTransactionCount
                    + " associated debit transactions (expenses).");
        }

        if (!Boolean.TRUE.equals(cardToDelete.getIsArchived())) {
            throw new RuntimeException(
                    "Card must be archived before deletion. Also ensure it has no transactions and zero debt.");
        }

        if (cardTaxesReminderRepository.existsByCard(cardToDelete)) {
            throw new RuntimeException("Cannot delete card: it is used in tax reminders.");
        }

        cardRepository.delete(cardToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getCurrentDebt(Long cardId, User user) {
        // Validar que la tarjeta existe y pertenece al usuario
        getCardEntityByIdAndUser(cardId, user);
        return cardRepository.getCurrentDebt(cardId);
    }

    @Override
    @Transactional
    public void updateBalance(Long cardId, BigDecimal amountChange) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
        // amountChange positivo incrementa el balance (pago a tarjeta o reversión de
        // gasto)
        // amountChange negativo decrementa el balance (gasto con tarjeta o reversión de
        // pago)
        card.setCurrentBalance(card.getCurrentBalance().add(amountChange));
        cardRepository.save(card);
        logger.info("Updated balance for card ID {}: change={}, new balance={}",
                cardId, amountChange, card.getCurrentBalance());
    }
}