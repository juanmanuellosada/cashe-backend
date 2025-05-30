package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Transaction;
import com.cashe.backend.service.dto.TransactionDto;
import com.cashe.backend.domain.User;
import com.cashe.backend.domain.Currency;
import com.cashe.backend.domain.Category;
import com.cashe.backend.domain.Account;
import com.cashe.backend.domain.Card;
import com.cashe.backend.domain.enums.TransactionStatus;
import com.cashe.backend.service.dto.TransactionCreateRequest;
import com.cashe.backend.service.dto.TransactionUpdateRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TransactionMapper {

    public static TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setAmount(transaction.getAmount());

        if (transaction.getCurrency() != null) {
            dto.setCurrencyCode(transaction.getCurrency().getCode());
            dto.setCurrencySymbol(transaction.getCurrency().getSymbol());
        }

        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setStatus(transaction.getStatus());
        dto.setNotes(transaction.getNotes());

        if (transaction.getCategory() != null) {
            dto.setCategoryId(transaction.getCategory().getId());
            dto.setCategoryName(transaction.getCategory().getName());
        }

        if (transaction.getAccount() != null) {
            dto.setAccountId(transaction.getAccount().getId());
            dto.setAccountName(transaction.getAccount().getName());
        }

        if (transaction.getCard() != null) {
            dto.setCardId(transaction.getCard().getId());
            dto.setCardLastFourDigits(transaction.getCard().getName());
        }

        dto.setEntryType(transaction.getEntryType());

        if (transaction.getAttachments() != null) {
            dto.setAttachmentsCount(transaction.getAttachments().size());
        } else {
            dto.setAttachmentsCount(0);
        }

        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());

        return dto;
    }

    public static List<TransactionDto> toDtoList(List<Transaction> transactions) {
        if (transactions == null) {
            return null;
        }
        return transactions.stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    public static Transaction toEntity(TransactionCreateRequest request,
            User user,
            Currency currency,
            Category category,
            Account account,
            Card card) {
        if (request == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(currency);
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setStatus(request.getStatus() != null ? request.getStatus() : TransactionStatus.APPROVED);
        transaction.setNotes(request.getNotes());
        transaction.setCategory(category);
        transaction.setEntryType(request.getEntryType());

        if (request.getAccountId() != null && account != null) {
            transaction.setAccount(account);
            transaction.setCard(null);
        } else if (request.getCardId() != null && card != null) {
            transaction.setCard(card);
            transaction.setAccount(null);
        }

        return transaction;
    }

    public static void updateEntityFromRequest(Transaction transaction,
            TransactionUpdateRequest request,
            Category category) {
        if (request == null || transaction == null) {
            return;
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getTransactionDate() != null) {
            OffsetDateTime odt = request.getTransactionDate().atStartOfDay(ZoneOffset.systemDefault())
                    .toOffsetDateTime();
            transaction.setTransactionDate(odt);
        }
        if (request.getNotes() != null) {
            transaction.setNotes(request.getNotes());
        }
        transaction.setCategory(category);
    }
}