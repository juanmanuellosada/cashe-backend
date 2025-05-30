package com.cashe.backend.repository;

import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.User;
import com.cashe.backend.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    List<Category> findByUserAndIsArchivedFalseOrderByTypeAscNameAsc(User user);

    List<Category> findByUserAndTypeAndIsArchivedFalseOrderByNameAsc(User user, CategoryType type);

    Optional<Category> findByIdAndUser(Long id, User user);

    Optional<Category> findByNameAndUserAndType(String name, User user, CategoryType type);

    // Para validaciones o búsquedas específicas
    List<Category> findByUser(User user);

    Optional<Category> findByIdAndUserIsNull(Long id);

    List<Category> findByUserIsNullOrUserOrderByNameAsc(User user);

    Optional<Category> findByNameAndUser(String name, User user);

    // Para validación de duplicados globales (sin usuario)
    Optional<Category> findByNameAndUserIsNull(String name);

    List<Category> findByUserIsNullOrderByNameAsc();
}