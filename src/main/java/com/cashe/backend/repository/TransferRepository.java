package com.cashe.backend.repository;

import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.Transfer;
import com.cashe.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {

    Optional<Transfer> findByIdAndUser(Long id, User user);

    Page<Transfer> findByUserOrderByTransferDateDesc(User user, Pageable pageable);

    // Ejemplos de búsquedas más específicas que podrían ser útiles
    List<Transfer> findByUserAndTransferDateBetween(User user, OffsetDateTime startDate, OffsetDateTime endDate);

    List<Transfer> findByUserAndFromAccount(User user, Account fromAccount);

    List<Transfer> findByUserAndToAccount(User user, Account toAccount);

    List<Transfer> findByUserAndToCard(User user, Card toCard);

    boolean existsByFromAccountOrToAccount(Account fromAccount, Account toAccount);

    // Métodos para conteo específico por origen o destino
    long countByFromAccount(Account fromAccount);

    long countByToAccount(Account toAccount);

    // Métodos para suma específica por origen o destino (considerando
    // transferencias completadas)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.toAccount = :account AND t.status = com.cashe.backend.domain.enums.TransferStatus.COMPLETED")
    BigDecimal sumAmountByDestinationAccount(@Param("account") Account account);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.fromAccount = :account AND t.status = com.cashe.backend.domain.enums.TransferStatus.COMPLETED")
    BigDecimal sumAmountByOriginAccount(@Param("account") Account account);
}