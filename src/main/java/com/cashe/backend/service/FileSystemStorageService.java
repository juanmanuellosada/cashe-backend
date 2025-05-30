package com.cashe.backend.service;

import com.cashe.backend.common.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service("fileSystemStorageService") // Darle un nombre al bean por si hay otras implementaciones
public class FileSystemStorageService implements FileStorageService {

    @Value("${file.upload-dir:./uploads}") // Inyectar la ruta base desde application.properties
    private String uploadDir;

    private Path rootLocation;

    @Override
    @PostConstruct // Para que se ejecute después de la inyección de dependencias
    public void init() throws IOException {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory: " + this.rootLocation, e);
        }
    }

    @Override
    public String store(MultipartFile file, String... subPathElements) throws IOException {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            // Esto es una comprobación de seguridad básica
            throw new FileStorageException(
                    "Cannot store file with relative path outside current directory " + originalFilename);
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");

        Path destinationDirectory = this.rootLocation;
        if (subPathElements != null && subPathElements.length > 0) {
            for (String element : subPathElements) {
                destinationDirectory = destinationDirectory.resolve(StringUtils.cleanPath(element));
            }
        }
        Files.createDirectories(destinationDirectory); // Asegurar que la subcarpeta exista

        Path destinationFile = destinationDirectory.resolve(storedFilename).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(destinationDirectory.toAbsolutePath())) {
            // Comprobación de seguridad adicional
            throw new FileStorageException("Cannot store file outside designated sub-directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
        // Devolver la ruta relativa al rootLocation o solo el nombre del archivo
        // almacenado.
        // Si usamos subPathElements, el nombre completo podría ser
        // subPath/storedFilename
        if (subPathElements != null && subPathElements.length > 0) {
            return Paths.get(String.join("/", subPathElements), storedFilename).toString();
        }
        return storedFilename;
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) throws IOException {
        Path file = load(filename);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + filename, e);
        }
    }
}