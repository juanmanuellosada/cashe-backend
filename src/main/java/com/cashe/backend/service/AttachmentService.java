package com.cashe.backend.service;

import com.cashe.backend.domain.User;
import com.cashe.backend.service.dto.AttachmentDto;
import org.springframework.core.io.Resource; // Para la descarga de archivos
import org.springframework.web.multipart.MultipartFile; // Para la subida de archivos

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AttachmentService {

    AttachmentDto storeAttachment(MultipartFile file, Long transactionId, User user, String description)
            throws IOException;

    Optional<AttachmentDto> getAttachmentMetadataByIdAndUser(Long attachmentId, User user);

    List<AttachmentDto> getAttachmentsMetadataByTransactionAndUser(Long transactionId, User user);

    Resource loadAttachmentAsResource(Long attachmentId, User user);

    void deleteAttachment(Long attachmentId, User user);

    // Podríamos necesitar un método para actualizar la descripción de un adjunto
    // existente.
    AttachmentDto updateAttachmentDescription(Long attachmentId, String newDescription, User user);

}