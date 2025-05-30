package com.cashe.backend.service.dto;

import com.cashe.backend.domain.enums.TransactionEntryType;
import com.cashe.backend.domain.enums.TransactionStatus;
import com.cashe.backend.validation.constraint.EitherAccountIdOrCardId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EitherAccountIdOrCardId
public class TransactionCreateRequest {

    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;

    @NotNull
    private OffsetDateTime transactionDate;

    private TransactionStatus status = TransactionStatus.APPROVED;

    @Size(max = 1000)
    private String notes;

    @NotNull
    private Long categoryId;

    @NotNull
    private TransactionEntryType entryType;

    private Long accountId;

    private Long cardId;

    // Lista de adjuntos podría ir aquí en el futuro
    // private List<AttachmentCreateRequest> attachments;
}