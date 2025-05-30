package com.cashe.backend.repository;

import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.CardTaxesReminder;
import com.cashe.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardTaxesReminderRepository
        extends JpaRepository<CardTaxesReminder, Long>, JpaSpecificationExecutor<CardTaxesReminder> {

    Optional<CardTaxesReminder> findByIdAndUser(Long id, User user);

    List<CardTaxesReminder> findByCardAndUserAndIsActiveTrue(Card card, User user);

    List<CardTaxesReminder> findByCardAndUser(Card card, User user);

    List<CardTaxesReminder> findByUser(User user);

    List<CardTaxesReminder> findByUserAndIsActiveTrueOrderByDescriptionAsc(User user);

    List<CardTaxesReminder> findByUserOrderByDescriptionAsc(User user);

    List<CardTaxesReminder> findByCardAndIsActiveTrue(Card card);

    List<CardTaxesReminder> findByCard(Card card);

    boolean existsByCard(Card card);

    // Métodos para buscar por ID de usuario
    Page<CardTaxesReminder> findByUser_Id(Long userId, Pageable pageable);

    Optional<CardTaxesReminder> findByIdAndUser_Id(Long id, Long userId);

}