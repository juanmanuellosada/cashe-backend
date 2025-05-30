package com.cashe.backend.repository;

import com.cashe.backend.domain.Transaction;
import com.cashe.backend.domain.User;
import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.enums.TransactionEntryType;
import com.cashe.backend.domain.enums.TransactionStatus;
import com.cashe.backend.repository.dto.CategorySummary;
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
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

        Optional<Transaction> findByIdAndUser(Long id, User user);

        Page<Transaction> findByUserOrderByTransactionDateDesc(User user, Pageable pageable);

        // Para los endpoints de agregación, podríamos necesitar queries más complejas.
        // Estas son algunas básicas, las de agregación se construirán dinámicamente o
        // con queries nativas/JPQL específicas.

        List<Transaction> findByUserAndTransactionDateBetweenAndEntryTypeAndStatus(
                        User user, OffsetDateTime startDate, OffsetDateTime endDate, TransactionEntryType entryType,
                        TransactionStatus status);

        List<Transaction> findByAccountAndStatusOrderByTransactionDateDesc(Account account, TransactionStatus status);

        List<Transaction> findByCardAndStatusOrderByTransactionDateDesc(Card card, TransactionStatus status);

        long countByAccount(Account account);

        Page<Transaction> findByAccountIdAndUserId(Long accountId, Long userId, Pageable pageable);

        // Ejemplo para obtener sumas por categoría para un usuario en un rango de
        // fechas y tipo
        @Query("SELECT new com.cashe.backend.repository.dto.CategorySummary(t.category.id, t.category.name, SUM(t.amount), COUNT(t.id)) "
                        +
                        "FROM Transaction t " +
                        "WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate AND t.entryType = :entryType AND t.status = 'APPROVED' "
                        +
                        "AND t.category IS NOT NULL " +
                        "GROUP BY t.category.id, t.category.name")
        List<com.cashe.backend.repository.dto.CategorySummary> getCategorySummaries(
                        @Param("user") User user,
                        @Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate,
                        @Param("entryType") TransactionEntryType entryType);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                        "WHERE t.user = :user " +
                        "AND t.category IN :categories " +
                        "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
                        "AND t.entryType = :entryType " +
                        "AND t.status = com.cashe.backend.domain.enums.TransactionStatus.APPROVED")
        BigDecimal sumByUserAndCategoriesInAndTransactionDateBetweenAndEntryTypeAndStatus(
                        @Param("user") User user,
                        @Param("categories") Set<Category> categories,
                        @Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate,
                        @Param("entryType") TransactionEntryType entryType);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account AND t.entryType = :entryType AND t.status = com.cashe.backend.domain.enums.TransactionStatus.APPROVED")
        BigDecimal sumAmountByAccountAndEntryType(@Param("account") Account account,
                        @Param("entryType") TransactionEntryType entryType);

        long countByCardAndUserAndEntryType(Card card, User user, TransactionEntryType entryType);

        long countByCategoryAndUser(Category category, User user);

        Page<Transaction> findByAccountOrderByTransactionDateDesc(Account account, Pageable pageable);

        Page<Transaction> findByCardOrderByTransactionDateDesc(Card card, Pageable pageable);

        @Query("SELECT new com.cashe.backend.repository.dto.CategorySummary(c.id, c.name, SUM(t.amount), COUNT(t)) " +
                        "FROM Transaction t JOIN t.category c " +
                        "WHERE t.user = :user " +
                        "AND t.status = com.cashe.backend.domain.enums.TransactionStatus.APPROVED " +
                        "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
                        "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
                        "AND (:entryType IS NULL OR t.entryType = :entryType) " +
                        "GROUP BY c.id, c.name")
        List<CategorySummary> findCategorySummariesByUserAndDateRangeAndEntryType(
                        @Param("user") User user,
                        @Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate,
                        @Param("entryType") TransactionEntryType entryType);

        // Nota: La función DATE_TRUNC es específica de PostgreSQL.
        // Para portabilidad a otras bases de datos, se necesitaría una estrategia
        // diferente
        // o queries nativas separadas por dialecto.
        @Query(value = "SELECT CAST(DATE_TRUNC(:granularity, t.transaction_date) AS TEXT) AS periodLabel, " +
                        "t.entry_type AS entryTypeString, " +
                        "SUM(t.amount) AS totalAmount, " +
                        "COUNT(t.id) AS transactionCount " +
                        "FROM transactions t " +
                        "WHERE t.user_id = :userId " +
                        "AND t.status = 'APPROVED' " +
                        "AND (:startDate IS NULL OR t.transaction_date >= :startDate) " +
                        "AND (:endDate IS NULL OR t.transaction_date <= :endDate) " +
                        "AND (:entryTypeString IS NULL OR t.entry_type = :entryTypeString) " +
                        "GROUP BY periodLabel, t.entry_type " +
                        "ORDER BY periodLabel, t.entry_type", nativeQuery = true)
        List<Object[]> findSummaryOverTimeNative(
                        @Param("userId") Long userId,
                        @Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate,
                        @Param("granularity") String granularity, // 'day', 'month', 'year'
                        @Param("entryTypeString") String entryTypeString); // Cambiado a String

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                        "WHERE t.user = :user " +
                        "AND t.status = com.cashe.backend.domain.enums.TransactionStatus.APPROVED " +
                        "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
                        "AND (:entryType IS NULL OR t.entryType = :entryType)")
        BigDecimal sumAmountByUserAndDateRangeAndEntryType(
                        @Param("user") User user,
                        @Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate, // endDate es exclusivo
                        @Param("entryType") TransactionEntryType entryType);

}