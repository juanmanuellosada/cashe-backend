package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.domain.enums.TransactionEntryType;
import com.cashe.backend.domain.enums.TransactionStatus;
import com.cashe.backend.repository.dto.CategorySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.cashe.backend.service.dto.TransactionCreateRequest;
import com.cashe.backend.service.dto.TransactionDto;
import com.cashe.backend.service.dto.TransactionUpdateRequest;

public interface TransactionService {

        TransactionDto createTransaction(TransactionCreateRequest createRequest, User user);

        Optional<TransactionDto> getTransactionByIdAndUser(Long id, User user);

        Page<TransactionDto> getAllTransactions(User user,
                        Pageable pageable,
                        OffsetDateTime startDate,
                        OffsetDateTime endDate,
                        TransactionEntryType entryType,
                        Long categoryId,
                        Long accountId,
                        Long cardId,
                        TransactionStatus status,
                        String descriptionLike);

        TransactionDto updateTransaction(Long id, TransactionUpdateRequest updateRequest, User user);

        void deleteTransaction(Long id, User user);

        TransactionDto approveTransaction(Long id, User user);

        TransactionDto rejectTransaction(Long id, User user);

        // Endpoints de agregación
        List<CategorySummary> getCategorySummaries(User user, OffsetDateTime startDate, OffsetDateTime endDate,
                        String entryTypeString);

        Map<String, List<Map<String, Object>>> getSummaryOverTime(User user, OffsetDateTime startDate,
                        OffsetDateTime endDate, String granularity, String entryTypeString);

        Map<String, Map<String, BigDecimal>> getCashFlow(User user, int year, Optional<Integer> month);

        Map<String, BigDecimal> getFinancialStatistics(User user, OffsetDateTime startDate, OffsetDateTime endDate);

        // Helper para cambiar estado (podría ser privado en la implementación o parte
        // de la interfaz si es útil)
        TransactionDto updateTransactionStatus(Long id, TransactionStatus newStatus, User user);

        Page<TransactionDto> getTransactionsByUser(User user, Pageable pageable);

        Page<TransactionDto> getTransactionsByCard(Long cardId, User user, Pageable pageable);
}