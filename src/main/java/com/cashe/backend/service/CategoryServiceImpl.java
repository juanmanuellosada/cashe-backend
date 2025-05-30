package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.User;
import com.cashe.backend.domain.enums.CategoryType;
import com.cashe.backend.repository.BudgetRepository;
import com.cashe.backend.repository.CategoryRepository;
import com.cashe.backend.repository.TransactionRepository;
import com.cashe.backend.service.dto.CategoryCreateRequest;
import com.cashe.backend.service.dto.CategoryDto;
import com.cashe.backend.service.dto.CategoryUpdateRequest;
import com.cashe.backend.service.mapper.CategoryMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    // No necesitamos CategoryMapper inyectado si todos sus métodos son estáticos

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryCreateRequest createRequest, User user) {
        categoryRepository.findByNameAndUserAndType(createRequest.getName(), user, createRequest.getType())
                .ifPresent(c -> {
                    throw new OperationNotAllowedException(
                            "Category with name '" + createRequest.getName() + "' and type '"
                                    + createRequest.getType() + "' already exists for this user.");
                });
        Category category = CategoryMapper.toEntity(createRequest, user);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user).map(CategoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> findCategoriesByUser(User user, CategoryType type, Boolean isArchived, Pageable pageable) {
        Specification<Category> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user"), user));
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (isArchived != null) {
                predicates.add(criteriaBuilder.equal(root.get("isArchived"), isArchived));
            }
            // Ordenar por nombre por defecto si no se especifica otro orden en pageable
            query.orderBy(criteriaBuilder.asc(root.get("name")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Category> categoriesPage = categoryRepository.findAll(spec, pageable);
        return categoriesPage.map(CategoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategoriesByUser(User user) {
        return categoryRepository.findByUserAndIsArchivedFalseOrderByTypeAscNameAsc(user).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategoriesByUserAndType(User user, CategoryType type) {
        return categoryRepository.findByUserAndTypeAndIsArchivedFalseOrderByNameAsc(user, type).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryUpdateRequest updateRequest, User user) {
        Category existingCategory = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id + " for user " + user.getId()));

        // Validar nombre duplicado si cambia el nombre o el tipo
        if ((updateRequest.getName() != null && !existingCategory.getName().equals(updateRequest.getName())) ||
                (updateRequest.getType() != null && existingCategory.getType() != updateRequest.getType())) {
            String nameToCheck = updateRequest.getName() != null ? updateRequest.getName() : existingCategory.getName();
            CategoryType typeToCheck = updateRequest.getType() != null ? updateRequest.getType()
                    : existingCategory.getType();

            categoryRepository.findByNameAndUserAndType(nameToCheck, user, typeToCheck)
                    .ifPresent(c -> {
                        if (!c.getId().equals(existingCategory.getId())) {
                            throw new OperationNotAllowedException("Another category with name '" + nameToCheck
                                    + "' and type '" + typeToCheck + "' already exists for this user.");
                        }
                    });
        }

        CategoryMapper.updateEntityFromRequest(updateRequest, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public CategoryDto toggleCategoryArchiveStatus(Long id, User user) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id + " for user " + user.getId()));
        category.setIsArchived(!category.getIsArchived());
        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, User user) {
        Category categoryToDelete = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id + " for user " + user.getId()));

        long transactionCount = transactionRepository.countByCategoryAndUser(categoryToDelete, user);
        if (transactionCount > 0) {
            throw new OperationNotAllowedException(
                    "Cannot delete category: it is currently in use by " + transactionCount + " transaction(s).");
        }

        if (budgetRepository.existsByCategoriesContains(categoryToDelete)) {
            throw new OperationNotAllowedException(
                    "Cannot delete category: it is currently in use by one or more budgets.");
        }

        // Eliminamos la restricción de que deba estar archivada para borrarla,
        // ya que las validaciones de uso son suficientes.
        // Si se desea mantener, descomentar la siguiente validación:
        // if (!Boolean.TRUE.equals(categoryToDelete.getIsArchived())) {
        // throw new OperationNotAllowedException(
        // "Category must be archived before deletion. For data integrity, ensure it's
        // not used in any transactions.");
        // }

        categoryRepository.delete(categoryToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoriesByUser(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getGlobalCategories() {
        return categoryRepository.findByUserIsNullOrderByNameAsc()
                .stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }
}