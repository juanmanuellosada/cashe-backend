package com.cashe.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @ToString.Exclude // Evitar recursión en toString con Transaction
    @EqualsAndHashCode.Exclude // Evitar recursión con Transaction
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Para control de acceso/propiedad y auditoría

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName; // Nombre original del archivo

    @NotBlank
    @Size(max = 1024)
    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath; // Ruta en el sistema de almacenamiento (ej. S3, servidor local)

    @Size(max = 100)
    @Column(name = "file_type", length = 100)
    private String fileType; // MIME type (ej. 'application/pdf', 'image/jpeg')

    @Column(name = "file_size_bytes")
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String description; // Descripción opcional del adjunto

    @NotNull
    @Column(name = "uploaded_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = OffsetDateTime.now();
    }
}