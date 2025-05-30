package com.cashe.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    void init() throws IOException;

    String store(MultipartFile file, String... subPath) throws IOException;

    Resource loadAsResource(String filename);

    void delete(String filename) throws IOException;

    Path load(String filename);

}