package com.cashe.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AttachmentDto {
    private Long id;
    private Long transactionId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String description;
    private OffsetDateTime uploadedAt;
    // Considerar añadir un campo para una URL de descarga si se genera una
    // private String downloadUrl;
}