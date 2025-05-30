package com.cashe.backend.service;

import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.*;
import com.cashe.backend.repository.*;
import com.cashe.backend.service.dto.TransferCreateRequest;
import com.cashe.backend.service.dto.TransferDto;
import com.cashe.backend.service.dto.TransferUpdateRequest;
import com.cashe.backend.service.mapper.TransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy; // Para dependencias circulares si surgen
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CurrencyRepository currencyRepository;
    private final @Lazy AccountService accountService;
    private final @Lazy CardService cardService;

    @Override
    @Transactional
    public TransferDto createTransfer(TransferCreateRequest createRequest, User user) {
        Currency currency = currencyRepository.findById(createRequest.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", createRequest.getCurrencyCode()));

        Account fromAccount = accountService.getAccountEntityByIdAndUser(createRequest.getFromAccountId(), user)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Source Account", "id", createRequest.getFromAccountId()));

        Account toAccount = null;
        if (createRequest.getToAccountId() != null) {
            toAccount = accountService.getAccountEntityByIdAndUser(createRequest.getToAccountId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Destination Account", "id",
                            createRequest.getToAccountId()));
        }

        Card toCard = null;
        if (createRequest.getToCardId() != null) {
            toCard = cardService.getCardEntityByIdAndUser(createRequest.getToCardId(), user)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Destination Card", "id", createRequest.getToCardId()));
        }

        if (toAccount == null && toCard == null) {
            throw new OperationNotAllowedException("Transfer must have a destination account or card.");
        }
        if (toAccount != null && toCard != null) {
            throw new OperationNotAllowedException(
                    "Transfer cannot have both a destination account and a destination card.");
        }
        if (fromAccount.getId().equals(toAccount != null ? toAccount.getId() : null)) {
            throw new OperationNotAllowedException("Source and destination accounts cannot be the same.");
        }

        // Ajustar saldos ANTES de guardar la transferencia
        BigDecimal amount = createRequest.getAmount();
        accountService.updateBalance(fromAccount.getId(), amount.negate()); // Restar del origen
        if (toAccount != null) {
            accountService.updateBalance(toAccount.getId(), amount); // Sumar al destino (cuenta)
        }
        if (toCard != null) {
            // Asumiendo que una transferencia a tarjeta reduce su deuda (aumenta el saldo
            // disponible/reduce balance negativo)
            cardService.updateBalance(toCard.getId(), amount); // Sumar al "saldo" de la tarjeta
        }

        Transfer transfer = TransferMapper.toEntity(createRequest, user, currency, fromAccount, toAccount, toCard);
        Transfer savedTransfer = transferRepository.save(transfer);
        return TransferMapper.toDto(savedTransfer);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferDto getTransferByIdAndUser(Long id, User user) {
        return transferRepository.findByIdAndUser(id, user)
                .map(TransferMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getAllTransfersByUser(User user, Pageable pageable) {
        return transferRepository.findByUserOrderByTransferDateDesc(user, pageable).map(TransferMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> findTransfers(User user, Specification<Transfer> spec, Pageable pageable) {
        Specification<Transfer> userSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"),
                user);
        Specification<Transfer> finalSpec = spec == null ? userSpec : userSpec.and(spec);
        return transferRepository.findAll(finalSpec, pageable).map(TransferMapper::toDto);
    }

    @Override
    @Transactional
    public TransferDto updateTransfer(Long id, TransferUpdateRequest updateRequest, User user) {
        Transfer existingTransfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        // Verificar propiedad
        if (!existingTransfer.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("User not authorized to update this transfer.");
        }

        Account fromAccount = existingTransfer.getFromAccount();
        Account toAccount = existingTransfer.getToAccount();
        Card toCard = existingTransfer.getToCard();
        BigDecimal oldAmount = existingTransfer.getAmount();

        // 1. Revertir el efecto del saldo original
        accountService.updateBalance(fromAccount.getId(), oldAmount); // Sumar de nuevo al origen
        if (toAccount != null) {
            accountService.updateBalance(toAccount.getId(), oldAmount.negate()); // Restar del destino (cuenta)
        }
        if (toCard != null) {
            cardService.updateBalance(toCard.getId(), oldAmount.negate()); // Restar del "saldo" de la tarjeta
        }

        // 2. Actualizar la entidad Transfer
        Currency newCurrency = existingTransfer.getCurrency();
        if (updateRequest.getCurrencyCode() != null
                && !updateRequest.getCurrencyCode().equals(existingTransfer.getCurrency().getCode())) {
            newCurrency = currencyRepository.findById(updateRequest.getCurrencyCode())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Currency", "code", updateRequest.getCurrencyCode()));
            // Nota: Si la moneda cambia, la lógica de ajuste de saldos debería considerar
            // conversiones.
            // Por simplicidad aquí, asumimos que los saldos se manejan en la moneda de la
            // cuenta/tarjeta.
            // O que la actualización de moneda no es común / requiere manejo especial.
        }
        TransferMapper.updateEntityFromRequest(updateRequest, existingTransfer, newCurrency);
        Transfer savedTransfer = transferRepository.save(existingTransfer);

        // 3. Aplicar el efecto del nuevo saldo
        BigDecimal newAmount = savedTransfer.getAmount();
        accountService.updateBalance(fromAccount.getId(), newAmount.negate()); // Restar nuevo monto del origen
        if (toAccount != null) {
            accountService.updateBalance(toAccount.getId(), newAmount); // Sumar nuevo monto al destino (cuenta)
        }
        if (toCard != null) {
            cardService.updateBalance(toCard.getId(), newAmount); // Sumar nuevo monto al "saldo" de la tarjeta
        }

        return TransferMapper.toDto(savedTransfer);
    }

    @Override
    @Transactional
    public void deleteTransfer(Long id, User user) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        // Verificar propiedad
        if (!transfer.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("User not authorized to delete this transfer.");
        }

        Account fromAccount = transfer.getFromAccount();
        Account toAccount = transfer.getToAccount();
        Card toCard = transfer.getToCard();
        BigDecimal amount = transfer.getAmount();

        // Revertir el efecto del saldo
        accountService.updateBalance(fromAccount.getId(), amount); // Sumar de nuevo al origen
        if (toAccount != null) {
            accountService.updateBalance(toAccount.getId(), amount.negate()); // Restar del destino (cuenta)
        }
        if (toCard != null) {
            cardService.updateBalance(toCard.getId(), amount.negate()); // Restar del "saldo" de la tarjeta
        }

        transferRepository.delete(transfer);
    }
}