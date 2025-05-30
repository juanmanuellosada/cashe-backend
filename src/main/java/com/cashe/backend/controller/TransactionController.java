package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.domain.enums.TransactionEntryType;
import com.cashe.backend.domain.enums.TransactionStatus;
import com.cashe.backend.service.AttachmentService;
import com.cashe.backend.service.TransactionService;
import com.cashe.backend.service.dto.*;
import com.cashe.backend.repository.dto.CategorySummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TransactionController {

        private final TransactionService transactionService;
        private final AttachmentService attachmentService;

        // --- Endpoints de Transacciones ---

        @PostMapping
        public ResponseEntity<TransactionDto> createTransaction(
                        @Valid @RequestBody TransactionCreateRequest createRequest,
                        @AuthenticationPrincipal User currentUser) {
                TransactionDto createdTransaction = transactionService.createTransaction(createRequest, currentUser);
                return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
        }

        @GetMapping("/{id}")
        public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {
                return transactionService.getTransactionByIdAndUser(id, currentUser)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping
        public ResponseEntity<Page<TransactionDto>> getAllTransactions(
                        Pageable pageable,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
                        @RequestParam(required = false) TransactionEntryType entryType,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Long accountId,
                        @RequestParam(required = false) Long cardId,
                        @RequestParam(required = false) TransactionStatus status,
                        @RequestParam(required = false) String descriptionLike,
                        @AuthenticationPrincipal User currentUser) {
                Page<TransactionDto> transactionsPage = transactionService.getAllTransactions(currentUser, pageable,
                                startDate,
                                endDate, entryType, categoryId, accountId, cardId, status, descriptionLike);
                return ResponseEntity.ok(transactionsPage);
        }

        @PutMapping("/{id}")
        public ResponseEntity<TransactionDto> updateTransaction(@PathVariable Long id,
                        @Valid @RequestBody TransactionUpdateRequest updateRequest,
                        @AuthenticationPrincipal User currentUser) {
                TransactionDto updatedTransaction = transactionService.updateTransaction(id, updateRequest,
                                currentUser);
                return ResponseEntity.ok(updatedTransaction);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteTransaction(@PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {
                transactionService.deleteTransaction(id, currentUser);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/approve")
        public ResponseEntity<TransactionDto> approveTransaction(@PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {
                TransactionDto transactionDto = transactionService.approveTransaction(id, currentUser);
                return ResponseEntity.ok(transactionDto);
        }

        @PatchMapping("/{id}/reject")
        public ResponseEntity<TransactionDto> rejectTransaction(@PathVariable Long id,
                        @AuthenticationPrincipal User currentUser) {
                TransactionDto transactionDto = transactionService.rejectTransaction(id, currentUser);
                return ResponseEntity.ok(transactionDto);
        }

        // --- Endpoints de Adjuntos (Attachments) ---

        @PostMapping("/{transactionId}/attachments")
        public ResponseEntity<AttachmentDto> uploadAttachment(
                        @PathVariable Long transactionId,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "description", required = false) String description,
                        @AuthenticationPrincipal User currentUser) throws IOException {
                transactionService.getTransactionByIdAndUser(transactionId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Transaction", "id", transactionId));
                AttachmentDto attachmentDto = attachmentService.storeAttachment(file, transactionId, currentUser,
                                description);
                return new ResponseEntity<>(attachmentDto, HttpStatus.CREATED);
        }

        @GetMapping("/{transactionId}/attachments")
        public ResponseEntity<List<AttachmentDto>> getAttachmentsForTransaction(@PathVariable Long transactionId,
                        @AuthenticationPrincipal User currentUser) {
                transactionService.getTransactionByIdAndUser(transactionId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Transaction", "id", transactionId));
                List<AttachmentDto> attachments = attachmentService.getAttachmentsMetadataByTransactionAndUser(
                                transactionId,
                                currentUser);
                return ResponseEntity.ok(attachments);
        }

        @GetMapping("/{transactionId}/attachments/{attachmentId}")
        public ResponseEntity<AttachmentDto> getAttachmentMetadata(
                        @PathVariable Long transactionId,
                        @PathVariable Long attachmentId,
                        @AuthenticationPrincipal User currentUser) {
                transactionService.getTransactionByIdAndUser(transactionId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Transaction", "id", transactionId));
                return attachmentService.getAttachmentMetadataByIdAndUser(attachmentId, currentUser)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/{transactionId}/attachments/{attachmentId}/download")
        public ResponseEntity<Resource> downloadAttachment(
                        @PathVariable Long transactionId,
                        @PathVariable Long attachmentId,
                        @AuthenticationPrincipal User currentUser) {
                transactionService.getTransactionByIdAndUser(transactionId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Transaction", "id", transactionId));
                AttachmentDto metadata = attachmentService.getAttachmentMetadataByIdAndUser(attachmentId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Attachment not found or access denied"));

                Resource resource = attachmentService.loadAttachmentAsResource(attachmentId, currentUser);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + metadata.getFileName() + "\"")
                                .body(resource);
        }

        @DeleteMapping("/{transactionId}/attachments/{attachmentId}")
        public ResponseEntity<Void> deleteAttachment(
                        @PathVariable Long transactionId,
                        @PathVariable Long attachmentId,
                        @AuthenticationPrincipal User currentUser) {
                transactionService.getTransactionByIdAndUser(transactionId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Transaction", "id", transactionId));
                attachmentService.deleteAttachment(attachmentId, currentUser);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{transactionId}/attachments/{attachmentId}")
        public ResponseEntity<AttachmentDto> updateAttachmentDescription(
                        @PathVariable Long transactionId,
                        @PathVariable Long attachmentId,
                        @Valid @RequestBody UpdateDescriptionRequest request,
                        @AuthenticationPrincipal User currentUser) {
                transactionService.getTransactionByIdAndUser(transactionId, currentUser)
                                .orElseThrow(() -> new com.cashe.backend.common.exception.ResourceNotFoundException(
                                                "Transaction", "id", transactionId));
                AttachmentDto updatedAttachment = attachmentService.updateAttachmentDescription(attachmentId,
                                request.getDescription(), currentUser);
                return ResponseEntity.ok(updatedAttachment);
        }

        // --- Endpoints de Agregación/Reportes ---

        @GetMapping("/summary/categories")
        public ResponseEntity<List<CategorySummary>> getCategorySummaries(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
                        @RequestParam(required = false) String entryType,
                        @AuthenticationPrincipal User currentUser) {
                List<CategorySummary> summaries = transactionService.getCategorySummaries(currentUser, startDate,
                                endDate, entryType);
                return ResponseEntity.ok(summaries);
        }

        @GetMapping("/summary/over-time")
        public ResponseEntity<Map<String, List<Map<String, Object>>>> getSummaryOverTime(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
                        @RequestParam String granularity, // e.g., "day", "month", "year"
                        @RequestParam(required = false) String entryType,
                        @AuthenticationPrincipal User currentUser) {
                Map<String, List<Map<String, Object>>> summary = transactionService.getSummaryOverTime(currentUser,
                                startDate, endDate, granularity, entryType);
                return ResponseEntity.ok(summary);
        }

        @GetMapping("/summary/cash-flow")
        public ResponseEntity<Map<String, Map<String, BigDecimal>>> getCashFlow(
                        @RequestParam int year,
                        @RequestParam(required = false) Integer month, // Usar Integer para que sea opcional
                        @AuthenticationPrincipal User currentUser) {
                Map<String, Map<String, BigDecimal>> cashFlow = transactionService.getCashFlow(currentUser, year,
                                Optional.ofNullable(month));
                return ResponseEntity.ok(cashFlow);
        }

        @GetMapping("/summary/financial-statistics")
        public ResponseEntity<Map<String, BigDecimal>> getFinancialStatistics(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
                        @AuthenticationPrincipal User currentUser) {
                Map<String, BigDecimal> statistics = transactionService.getFinancialStatistics(currentUser, startDate,
                                endDate);
                return ResponseEntity.ok(statistics);
        }
}