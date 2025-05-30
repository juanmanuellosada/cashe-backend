package com.cashe.backend.controller;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.TransferService;
import com.cashe.backend.service.dto.TransferCreateRequest;
import com.cashe.backend.service.dto.TransferDto;
import com.cashe.backend.service.dto.TransferUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransferDto> createTransfer(
            @Valid @RequestBody TransferCreateRequest transferCreateRequest,
            @AuthenticationPrincipal User currentUser) {
        TransferDto createdTransfer = transferService.createTransfer(transferCreateRequest, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransfer);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransferDto> getTransferById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        TransferDto transfer = transferService.getTransferByIdAndUser(id, currentUser);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TransferDto>> listTransfers(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable) {
        Page<TransferDto> transfers = transferService.getAllTransfersByUser(currentUser, pageable);
        return ResponseEntity.ok(transfers);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransferDto> updateTransfer(@PathVariable Long id,
            @Valid @RequestBody TransferUpdateRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {
        TransferDto updatedTransfer = transferService.updateTransfer(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedTransfer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        transferService.deleteTransfer(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}