package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.repository.AccountRepository;
import com.cashe.backend.repository.CurrencyRepository;
import com.cashe.backend.service.dto.CurrencyCreateRequest;
import com.cashe.backend.service.dto.CurrencyDto;
import com.cashe.backend.service.dto.CurrencyUpdateRequest;
import com.cashe.backend.service.mapper.CurrencyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository; // Para validación en delete

    @Override
    @Transactional
    public CurrencyDto createCurrency(CurrencyCreateRequest createRequest) {
        // Validar que el código no exista ya
        if (currencyRepository.findById(createRequest.getCode().toUpperCase()).isPresent()) {
            throw new OperationNotAllowedException("Currency code already exists: " + createRequest.getCode());
        }

        Currency currency = CurrencyMapper.toEntity(createRequest);
        currency.setIsActive(true);

        // Si se marca como moneda base al crear
        if (Boolean.TRUE.equals(currency.getIsBaseCurrency())) {
            // Asegurar que solo haya una moneda base
            currencyRepository.findByIsBaseCurrencyTrue().ifPresent(oldBase -> {
                if (!oldBase.getCode().equals(currency.getCode())) {
                    oldBase.setIsBaseCurrency(false);
                    currencyRepository.save(oldBase);
                }
            });
            currency.setExchangeRate(BigDecimal.ONE); // La tasa de la moneda base es siempre 1
        } else if (currency.getExchangeRate() == null) {
            // Si no es base y no se especificó tasa, requiere buscar la base para
            // calcularla? No, mejor lanzar error o poner 0/null?
            // Por ahora, si no es base, la tasa debe ser provista o será nula.
            // Lanzar error si no se provee y no es base?
            throw new IllegalArgumentException("Exchange rate must be provided for non-base currencies.");
            // currency.setExchangeRate(BigDecimal.ZERO); // O poner CERO?
        }

        // Marcar la hora de actualización de la tasa
        currency.setLastUpdatedRate(OffsetDateTime.now());

        Currency savedCurrency = currencyRepository.save(currency);
        return CurrencyMapper.toDto(savedCurrency);
    }

    @Override
    @Transactional
    public CurrencyDto createIfNotExists(CurrencyCreateRequest createRequest) {
        // Verificar si la moneda ya existe
        String code = createRequest.getCode().toUpperCase();
        Optional<Currency> existingCurrency = currencyRepository.findById(code);

        // Si ya existe, devolver la moneda existente
        if (existingCurrency.isPresent()) {
            return CurrencyMapper.toDto(existingCurrency.get());
        }

        // Si no existe, crear la moneda utilizando la lógica existente
        Currency currency = CurrencyMapper.toEntity(createRequest);
        currency.setIsActive(true);

        // Si se marca como moneda base al crear
        if (Boolean.TRUE.equals(currency.getIsBaseCurrency())) {
            // Asegurar que solo haya una moneda base
            currencyRepository.findByIsBaseCurrencyTrue().ifPresent(oldBase -> {
                if (!oldBase.getCode().equals(currency.getCode())) {
                    oldBase.setIsBaseCurrency(false);
                    currencyRepository.save(oldBase);
                }
            });
            currency.setExchangeRate(BigDecimal.ONE); // La tasa de la moneda base es siempre 1
        } else if (currency.getExchangeRate() == null) {
            // Si no es base y no se especificó tasa, requerimos la tasa
            throw new IllegalArgumentException("Exchange rate must be provided for non-base currencies.");
        }

        // Marcar la hora de actualización de la tasa
        currency.setLastUpdatedRate(OffsetDateTime.now());

        Currency savedCurrency = currencyRepository.save(currency);
        return CurrencyMapper.toDto(savedCurrency);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CurrencyDto> getCurrencyByCode(String code) {
        return currencyRepository.findById(code.toUpperCase())
                .map(CurrencyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyDto> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(CurrencyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CurrencyDto updateCurrency(String code, CurrencyUpdateRequest updateRequest) {
        Currency currency = currencyRepository.findById(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", code));

        // No permitir cambiar la tasa de cambio de la moneda base directamente
        if (Boolean.TRUE.equals(currency.getIsBaseCurrency()) && updateRequest.getExchangeRate() != null) {
            throw new OperationNotAllowedException(
                    "No se puede cambiar la tasa de cambio de la moneda base directamente. Use la funcionalidad de establecer moneda base.");
        }

        CurrencyMapper.updateEntityFromRequest(updateRequest, currency);
        Currency updatedCurrency = currencyRepository.save(currency);
        return CurrencyMapper.toDto(updatedCurrency);
    }

    @Override
    @Transactional
    public void deleteCurrency(String code) {
        String upperCaseCode = code.toUpperCase();
        Currency currency = currencyRepository.findById(upperCaseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", upperCaseCode));

        if (Boolean.TRUE.equals(currency.getIsBaseCurrency())) {
            throw new OperationNotAllowedException("No se puede eliminar la moneda base.");
        }

        // Validar que no existan cuentas asociadas a esta moneda
        if (accountRepository.existsByCurrencyCode(upperCaseCode)) {
            throw new OperationNotAllowedException(
                    "No se puede eliminar la moneda porque existen cuentas asociadas a ella.");
        }

        // Validar que no existan tarjetas asociadas a esta moneda (si aplica)
        // if (cardRepository.existsByCurrencyCode(upperCaseCode)) {
        // throw new OperationNotAllowedException("No se puede eliminar la moneda porque
        // existen tarjetas asociadas a ella.");
        // }

        currencyRepository.delete(currency);
    }

    @Override
    @Transactional
    public CurrencyDto setBaseCurrency(String code) {
        String upperCaseCode = code.toUpperCase();
        Currency newBaseCurrency = currencyRepository.findById(upperCaseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", upperCaseCode));

        Optional<Currency> currentBaseOpt = currencyRepository.findByIsBaseCurrencyTrue();
        if (currentBaseOpt.isPresent()) {
            Currency oldBase = currentBaseOpt.get();
            if (oldBase.getCode().equals(upperCaseCode)) {
                // Ya es la moneda base, no hacer nada o solo devolverla
                return CurrencyMapper.toDto(newBaseCurrency);
            }
            oldBase.setIsBaseCurrency(false);
            // Aquí deberíamos recalcular todas las demás tasas de cambio si la antigua base
            // no era 1
            // Por simplicidad, asumimos que la antigua base tenía tasa 1 y las otras se
            // ajustan manualmente por el usuario.
            // Una implementación más robusta requeriría una estrategia de conversión de
            // tasas.
            currencyRepository.save(oldBase);
        }

        newBaseCurrency.setIsBaseCurrency(true);
        newBaseCurrency.setExchangeRate(BigDecimal.ONE); // La moneda base siempre tiene tasa 1
        Currency savedNewBase = currencyRepository.save(newBaseCurrency);

        // Opcional: ¿Recalcular tasas de otras monedas en relación a la nueva base?
        // Esto puede ser complejo y depender de cómo se quieran gestionar las
        // conversiones.
        // Por ahora, las tasas de otras monedas permanecerán como están y el usuario
        // deberá ajustarlas si es necesario.

        return CurrencyMapper.toDto(savedNewBase);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CurrencyDto> getBaseCurrency() {
        return currencyRepository.findByIsBaseCurrencyTrue()
                .map(CurrencyMapper::toDto);
    }
}