package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.*;
import com.cashe.backend.service.dto.TransferCreateRequest;
import com.cashe.backend.service.dto.TransferDto;
import com.cashe.backend.service.dto.TransferUpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TransferMapper {

    public static TransferDto toDto(Transfer transfer) {
        if (transfer == null) {
            return null;
        }
        return new TransferDto(
                transfer.getId(),
                transfer.getUser() != null ? transfer.getUser().getId() : null,
                transfer.getDescription(),
                transfer.getAmount(),
                transfer.getCurrency() != null ? transfer.getCurrency().getCode() : null,
                transfer.getTransferDate(),
                transfer.getFromAccount() != null ? transfer.getFromAccount().getId() : null,
                transfer.getFromAccount() != null ? transfer.getFromAccount().getName() : null,
                transfer.getToAccount() != null ? transfer.getToAccount().getId() : null,
                transfer.getToAccount() != null ? transfer.getToAccount().getName() : null,
                transfer.getToCard() != null ? transfer.getToCard().getId() : null,
                transfer.getToCard() != null ? transfer.getToCard().getName() : null,
                transfer.getNotes(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt());
    }

    public static List<TransferDto> toDto(List<Transfer> transfers) {
        if (transfers == null) {
            return new ArrayList<>();
        }
        return transfers.stream().map(TransferMapper::toDto).collect(Collectors.toList());
    }

    // El servicio se encargará de buscar y asignar las entidades referenciadas
    // (Currency, Accounts, Card)
    public static Transfer toEntity(TransferCreateRequest createRequest, User user, Currency currency,
            Account fromAccount, Account toAccount, Card toCard) {
        if (createRequest == null) {
            return null;
        }
        Transfer transfer = new Transfer();
        transfer.setUser(user);
        transfer.setDescription(createRequest.getDescription());
        transfer.setAmount(createRequest.getAmount());
        transfer.setCurrency(currency); // Asignada por el servicio
        transfer.setTransferDate(createRequest.getTransferDate());
        transfer.setFromAccount(fromAccount); // Asignada por el servicio
        transfer.setToAccount(toAccount); // Asignada por el servicio (puede ser null)
        transfer.setToCard(toCard); // Asignada por el servicio (puede ser null)
        transfer.setNotes(createRequest.getNotes());
        // createdAt y updatedAt son manejados por @PrePersist/@PreUpdate en la entidad
        return transfer;
    }

    public static void updateEntityFromRequest(TransferUpdateRequest updateRequest, Transfer transfer,
            Currency newCurrency) {
        if (updateRequest == null || transfer == null) {
            return;
        }
        if (updateRequest.getDescription() != null) {
            transfer.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getAmount() != null) {
            transfer.setAmount(updateRequest.getAmount());
        }
        if (newCurrency != null) { // La moneda se actualiza si se proporciona una nueva válida
            transfer.setCurrency(newCurrency);
        }
        if (updateRequest.getTransferDate() != null) {
            // Convertir LocalDate a OffsetDateTime al inicio del día en la zona horaria del
            // sistema
            OffsetDateTime odt = updateRequest.getTransferDate().atStartOfDay(ZoneOffset.systemDefault())
                    .toOffsetDateTime();
            transfer.setTransferDate(odt);
        }
        // Las cuentas/tarjetas no se actualizan desde aquí para mantener la simplicidad
        // y evitar lógica compleja de reversión/aplicación de saldos.
        // El servicio podría manejar esto si fuera un requisito.
    }
}