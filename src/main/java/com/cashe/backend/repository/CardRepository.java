package com.cashe.backend.repository;

import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserAndIsArchivedFalseOrderByNameAsc(User user);

    Optional<Card> findByIdAndUser(Long id, User user);

    Optional<Card> findByNameAndUser(String name, User user);

    List<Card> findByUser(User user);

    List<Card> findByUserOrderByNameAsc(User user);

    // Ejemplo para calcular la deuda actual de una tarjeta de crédito
    @Query("SELECT COALESCE(SUM(CASE WHEN t.entryType = 'DEBIT' THEN t.amount ELSE -t.amount END), 0) " +
            "FROM Card c LEFT JOIN Transaction t ON t.card = c AND t.status = 'APPROVED' " +
            "WHERE c.id = :cardId GROUP BY c.id")
    Optional<BigDecimal> getCurrentDebt(@Param("cardId") Long cardId);

}