package com.cashe.backend.repository;

import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.User;
import com.cashe.backend.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    List<Account> findByUserAndIsArchivedFalseOrderByNameAsc(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    Optional<Account> findByNameAndUser(String name, User user);

    List<Account> findByUser(User user);

    List<Account> findByUserOrderByNameAsc(User user);

    // Ejemplo de cómo podríamos calcular el saldo, aunque esto usualmente se hace
    // en el servicio
    // o se mantiene actualizado. Para lecturas directas de saldo puede ser útil.
    @Query("SELECT (a.initialBalance + COALESCE(SUM(CASE WHEN t.entryType = 'CREDIT' THEN t.amount ELSE -t.amount END), 0)) "
            +
            "FROM Account a LEFT JOIN Transaction t ON t.account = a AND t.status = 'APPROVED' " +
            "WHERE a.id = :accountId GROUP BY a.id, a.initialBalance")
    Optional<BigDecimal> getCurrentBalance(@Param("accountId") Long accountId);

    long countByAccountType(AccountType accountType);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    boolean existsByCurrencyCode(String currencyCode);
}