package com.cashe.backend.service;

import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.User;
// import com.cashe.backend.domain.Transaction; // Si se listan transacciones desde aquí
import com.cashe.backend.service.dto.CardCreateRequest;
import com.cashe.backend.service.dto.CardDto;
import com.cashe.backend.service.dto.CardUpdateRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;

public interface CardService {

    CardDto createCard(CardCreateRequest createRequest, User user);

    Optional<CardDto> getCardByIdAndUser(Long id, User user);

    Optional<Card> getCardEntityByIdAndUser(Long id, User user);

    List<CardDto> getActiveCardsByUser(User user);

    List<CardDto> getAllCardsByUser(User user); // Incluye archivadas

    CardDto updateCard(Long id, CardUpdateRequest updateRequest, User user);

    CardDto archiveCard(Long id, User user);

    CardDto unarchiveCard(Long id, User user);

    void deleteCard(Long id, User user); // Con validación de no estar en uso y deuda cero

    void updateBalance(Long cardId, BigDecimal amountChange); // Nuevo método

    Optional<BigDecimal> getCurrentDebt(Long cardId, User user);

    // Para listar transacciones de una tarjeta (podría estar en TransactionService
    // también)
    // Page<Transaction> getTransactionsByCard(Long cardId, User user, Pageable
    // pageable); // Placeholder
}