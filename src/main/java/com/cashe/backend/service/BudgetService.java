package com.cashe.backend.service;

import com.cashe.backend.domain.Budget;
import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.BudgetCreateRequest;
import com.cashe.backend.service.dto.BudgetDto;
import com.cashe.backend.service.dto.BudgetUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface BudgetService {

    /**
     * Crea un nuevo presupuesto para el usuario especificado.
     *
     * @param createRequest DTO con la información para crear el presupuesto.
     * @param user          El usuario para el cual se crea el presupuesto.
     * @return El DTO del presupuesto creado.
     */
    BudgetDto createBudget(BudgetCreateRequest createRequest, User user);

    /**
     * Obtiene un presupuesto por su ID y usuario.
     *
     * @param id   El ID del presupuesto.
     * @param user El usuario propietario del presupuesto.
     * @return Un Optional con el DTO del presupuesto si se encuentra, o vacío si
     *         no.
     */
    Optional<BudgetDto> getBudgetByIdAndUser(Long id, User user);

    /**
     * Busca y pagina presupuestos para un usuario según un Specification.
     * El Specification opera sobre la entidad Budget.
     *
     * @param user     El usuario propietario de los presupuestos.
     * @param spec     La especificación de JPA para filtrar.
     * @param pageable La información de paginación.
     * @return Una página de DTOs de presupuestos.
     */
    Page<BudgetDto> findBudgets(User user, Specification<Budget> spec, Pageable pageable);

    /**
     * Obtiene todos los presupuestos activos para un usuario.
     *
     * @param user El usuario.
     * @return Una lista de DTOs de presupuestos activos.
     */
    List<BudgetDto> getActiveBudgetsByUser(User user);

    /**
     * Actualiza un presupuesto existente.
     *
     * @param id            El ID del presupuesto a actualizar.
     * @param updateRequest DTO con la información para actualizar.
     * @param user          El usuario propietario del presupuesto.
     * @return El DTO del presupuesto actualizado.
     */
    BudgetDto updateBudget(Long id, BudgetUpdateRequest updateRequest, User user);

    /**
     * Elimina un presupuesto.
     *
     * @param id   El ID del presupuesto a eliminar.
     * @param user El usuario propietario del presupuesto.
     */
    void deleteBudget(Long id, User user);

    /**
     * Añade una categoría a un presupuesto.
     *
     * @param budgetId   El ID del presupuesto.
     * @param categoryId El ID de la categoría a añadir.
     * @param user       El usuario propietario.
     * @return El DTO del presupuesto actualizado.
     */
    BudgetDto addCategoryToBudget(Long budgetId, Long categoryId, User user);

    /**
     * Elimina una categoría de un presupuesto.
     *
     * @param budgetId   El ID del presupuesto.
     * @param categoryId El ID de la categoría a eliminar.
     * @param user       El usuario propietario.
     * @return El DTO del presupuesto actualizado.
     */
    BudgetDto removeCategoryFromBudget(Long budgetId, Long categoryId, User user);

    /**
     * Cambia el estado (activo/inactivo) de un presupuesto.
     *
     * @param budgetId El ID del presupuesto.
     * @param isActive El nuevo estado.
     * @param user     El usuario propietario.
     * @return El DTO del presupuesto actualizado.
     */
    BudgetDto toggleBudgetStatus(Long budgetId, boolean isActive, User user);

}