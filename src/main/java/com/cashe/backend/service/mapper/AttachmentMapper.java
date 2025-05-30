package com.cashe.backend.service.mapper;

import com.cashe.backend.domain.Attachment;
import com.cashe.backend.service.dto.AttachmentDto;

import java.util.List;
import java.util.stream.Collectors;

public class AttachmentMapper {

    public static AttachmentDto toDto(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new AttachmentDto(
                attachment.getId(),
                attachment.getTransaction() != null ? attachment.getTransaction().getId() : null,
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getDescription(),
                attachment.getUploadedAt());
    }

    public static List<AttachmentDto> toDtoList(List<Attachment> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream()
                .map(AttachmentMapper::toDto)
                .collect(Collectors.toList());
    }
}