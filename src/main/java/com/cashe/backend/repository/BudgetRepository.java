package com.cashe.backend.repository;

import com.cashe.backend.domain.Budget;
import com.cashe.backend.domain.User;
import com.cashe.backend.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    List<Budget> findByUserAndIsActiveTrueOrderByStartDateDescNameAsc(User user);

    List<Budget> findByUserOrderByNameAsc(User user);

    Optional<Budget> findByIdAndUser(Long id, User user);

    Optional<Budget> findByNameAndUser(String name, User user);

    boolean existsByCategoriesContains(Category category);
}