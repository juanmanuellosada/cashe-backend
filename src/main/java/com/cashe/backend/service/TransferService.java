package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.domain.Transfer; // Necesario para Specification
import com.cashe.backend.service.dto.TransferCreateRequest;
import com.cashe.backend.service.dto.TransferDto;
import com.cashe.backend.service.dto.TransferUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TransferService {

    TransferDto createTransfer(TransferCreateRequest createRequest, User user);

    TransferDto getTransferByIdAndUser(Long id, User user);

    Page<TransferDto> getAllTransfersByUser(User user, Pageable pageable);

    Page<TransferDto> findTransfers(User user, Specification<Transfer> spec, Pageable pageable);

    TransferDto updateTransfer(Long id, TransferUpdateRequest updateRequest, User user);

    void deleteTransfer(Long id, User user);

}