package com.cashe.backend.repository;

import com.cashe.backend.domain.AccountType;
import com.cashe.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {

    List<AccountType> findByUserOrIsPredefinedTrue(User user);

    Optional<AccountType> findByIdAndUser(Long id, User user);

    Optional<AccountType> findByNameAndUser(String name, User user);

    List<AccountType> findByIsPredefinedTrue();

    Optional<AccountType> findByIdAndUserAndIsPredefinedFalse(Long id, User user);

    List<AccountType> findByUserAndIsPredefinedFalseOrderByNameAsc(User user);

    List<AccountType> findByIsPredefinedTrueOrderByNameAsc();
}