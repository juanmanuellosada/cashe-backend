package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.service.dto.CurrencyCreateRequest;
import com.cashe.backend.service.dto.CurrencyDto;
import com.cashe.backend.service.dto.CurrencyUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface CurrencyService {

    /**
     * Crea una nueva moneda.
     *
     * @param createRequest DTO con la información para crear la moneda.
     * @return El DTO de la moneda creada.
     */
    CurrencyDto createCurrency(CurrencyCreateRequest createRequest);

    /**
     * Crea una nueva moneda solo si no existe una con el mismo código.
     * Si la moneda ya existe, devuelve la existente sin modificarla.
     *
     * @param createRequest DTO con la información para crear la moneda.
     * @return El DTO de la moneda creada o la existente.
     */
    CurrencyDto createIfNotExists(CurrencyCreateRequest createRequest);

    /**
     * Obtiene una moneda por su código ISO (ej. "USD", "EUR").
     *
     * @param code El código ISO de la moneda.
     * @return Un Optional con el DTO de la moneda si se encuentra, o vacío si no.
     */
    Optional<CurrencyDto> getCurrencyByCode(String code);

    /**
     * Obtiene todas las monedas.
     *
     * @return Una lista de DTOs de todas las monedas.
     */
    List<CurrencyDto> getAllCurrencies();

    /**
     * Actualiza una moneda existente.
     *
     * @param code          El código ISO de la moneda a actualizar.
     * @param updateRequest DTO con la información para actualizar.
     * @return El DTO de la moneda actualizada.
     * @throws ResourceNotFoundException si la moneda no se encuentra.
     */
    CurrencyDto updateCurrency(String code, CurrencyUpdateRequest updateRequest);

    /**
     * Elimina una moneda por su código ISO.
     *
     * @param code El código ISO de la moneda a eliminar.
     * @throws ResourceNotFoundException    si la moneda no se encuentra.
     * @throws OperationNotAllowedException si se intenta eliminar la moneda base
     *                                      o si existen cuentas asociadas a esta
     *                                      moneda.
     */
    void deleteCurrency(String code);

    /**
     * Establece una moneda como la moneda base del sistema.
     * Solo puede haber una moneda base activa.
     *
     * @param code El código ISO de la moneda a establecer como base.
     * @return El DTO de la moneda que ahora es la base.
     * @throws ResourceNotFoundException si la moneda no se encuentra.
     */
    CurrencyDto setBaseCurrency(String code);

    /**
     * Obtiene la moneda base actual del sistema.
     *
     * @return Un Optional con el DTO de la moneda base si está configurada, o vacío
     *         si no.
     */
    Optional<CurrencyDto> getBaseCurrency();
}