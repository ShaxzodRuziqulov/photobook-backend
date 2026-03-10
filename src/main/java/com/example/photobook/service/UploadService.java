package com.example.photobook.service;

import com.example.photobook.dto.UploadResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadService {
    private final Path root = Paths.get("uploads-storage");

    public UploadResponseDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        try {
            Files.createDirectories(root);
            String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String key = UUID.randomUUID() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = root.resolve(key);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            UploadResponseDto response = new UploadResponseDto();
            response.setKey(key);
            response.setUrl("/uploads-storage/" + key);
            response.setMime(file.getContentType());
            response.setSize(file.getSize());
            return response;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to upload file");
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key is required");
        }

        try {
            Files.deleteIfExists(root.resolve(key));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to delete file");
        }
    }
}
