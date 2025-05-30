package com.cashe.backend.service;

import com.cashe.backend.common.exception.ResourceNotFoundException;
import com.cashe.backend.domain.Attachment;
import com.cashe.backend.domain.Transaction;
import com.cashe.backend.domain.User;
import com.cashe.backend.repository.AttachmentRepository;
import com.cashe.backend.repository.TransactionRepository;
import com.cashe.backend.service.dto.AttachmentDto;
import com.cashe.backend.service.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private final AttachmentRepository attachmentRepository;
    private final TransactionRepository transactionRepository;
    private final @Qualifier("fileSystemStorageService") FileStorageService fileStorageService;

    @Override
    @Transactional
    public AttachmentDto storeAttachment(MultipartFile file, Long transactionId, User user, String description)
            throws IOException {
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id",
                        transactionId + " for user " + user.getId()));

        String subPath = "user_" + user.getId() + "/tx_" + transactionId;
        String storedFilenameWithSubPath = fileStorageService.store(file, subPath);

        Attachment attachment = new Attachment();
        attachment.setTransaction(transaction);
        attachment.setUser(user);
        attachment.setFileName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        attachment.setFilePath(storedFilenameWithSubPath);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setDescription(description);

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return AttachmentMapper.toDto(savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttachmentDto> getAttachmentMetadataByIdAndUser(Long attachmentId, User user) {
        return attachmentRepository.findByIdAndUser(attachmentId, user).map(AttachmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentDto> getAttachmentsMetadataByTransactionAndUser(Long transactionId, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id",
                        transactionId + " for user " + user.getId()));
        return AttachmentMapper.toDtoList(attachmentRepository.findByTransactionAndUser(transaction, user));
    }

    @Override
    public Resource loadAttachmentAsResource(Long attachmentId, User user) {
        Attachment attachment = attachmentRepository.findByIdAndUser(attachmentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id",
                        attachmentId + " not found or access denied."));
        return fileStorageService.loadAsResource(attachment.getFilePath());
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, User user) {
        Attachment attachment = attachmentRepository.findByIdAndUser(attachmentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id",
                        attachmentId + " not found or access denied."));
        try {
            fileStorageService.delete(attachment.getFilePath());
        } catch (IOException e) {
            logger.error("Failed to delete physical file: {}", attachment.getFilePath(), e);
        }
        attachmentRepository.delete(attachment);
    }

    @Override
    @Transactional
    public AttachmentDto updateAttachmentDescription(Long attachmentId, String newDescription, User user) {
        Attachment attachment = attachmentRepository.findByIdAndUser(attachmentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id",
                        attachmentId + " not found or access denied."));
        attachment.setDescription(newDescription);
        Attachment savedAttachment = attachmentRepository.save(attachment);
        return AttachmentMapper.toDto(savedAttachment);
    }
}