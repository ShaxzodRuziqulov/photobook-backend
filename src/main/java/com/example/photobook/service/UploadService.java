package com.example.photobook.service;

import com.example.photobook.dto.UploadDto;
import com.example.photobook.dto.UploadResponseDto;
import com.example.photobook.entity.Upload;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.repository.ExpenseRepository;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.UploadRepository;
import com.example.photobook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final UploadRepository repository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;
    private final Path root;

    public UploadService(
            UploadRepository repository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            ExpenseRepository expenseRepository,
            @Value("${app.upload.dir:uploads-storage}") String uploadDir) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.expenseRepository = expenseRepository;
        this.root = Paths.get(uploadDir);
    }

    private void validate(MultipartFile file, UploadDto dto) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        if (dto == null) {
            throw new IllegalArgumentException("upload metadata is required");
        }
        if ((dto.getOwnerType() == null) != (dto.getOwnerId() == null)) {
            throw new IllegalArgumentException("ownerType and ownerId must be provided together");
        }
        if (dto.getOwnerType() != null) {
            validateOwner(dto.getOwnerType(), dto.getOwnerId());
        }
        String mime = file.getContentType();

        if (mime == null || !mime.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large");
        }
    }

    public UploadResponseDto upload(MultipartFile file, UploadDto dto) {
        validate(file, dto);

        try {
            Files.createDirectories(root);

            String originalName = file.getOriginalFilename() == null
                    ? "file"
                    : file.getOriginalFilename();

            String key = UUID.randomUUID() + "-" +
                    originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

            Path target = root.resolve(key);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Upload upload = new Upload();
            upload.setKey(key);
            upload.setOwnerType(dto.getOwnerType());
            upload.setOwnerId(dto.getOwnerId());
            upload.setMimeType(file.getContentType());
            upload.setSize(file.getSize());

            Upload savedUpload = repository.save(upload);
            return toResponse(savedUpload);

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to upload file");
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key is required");
        }

        try {
            Upload upload = repository.findByKey(key).orElseThrow(() -> new IllegalArgumentException("key not found"));
            Files.deleteIfExists(root.resolve(key));
            repository.delete(upload);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to delete file");
        }
    }

    public Upload findUploadById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("upload not found"));
    }

    public Upload attachToOwner(UUID uploadId, OwnerType ownerType, UUID ownerId) {
        if (uploadId == null) {
            throw new IllegalArgumentException("uploadId is required");
        }
        if (ownerType == null) {
            throw new IllegalArgumentException("ownerType is required");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }

        validateOwner(ownerType, ownerId);

        Upload upload = findUploadById(uploadId);
        upload.setOwnerType(ownerType);
        upload.setOwnerId(ownerId);
        return repository.save(upload);
    }

    public String buildFileUrl(Upload upload) {
        return upload == null ? null : "/uploads-storage/" + upload.getKey();
    }

    private UploadResponseDto toResponse(Upload upload) {
        UploadResponseDto response = new UploadResponseDto();
        response.setId(upload.getId());
        response.setKey(upload.getKey());
        response.setUrl(buildFileUrl(upload));
        response.setMime(upload.getMimeType());
        response.setSize(upload.getSize());
        return response;
    }

    private void validateOwner(OwnerType ownerType, UUID ownerId) {
        boolean exists = switch (ownerType) {
            case USER -> userRepository.existsById(ownerId);
            case ORDER -> orderRepository.existsById(ownerId);
            case EXPENSE -> expenseRepository.existsById(ownerId);
        };

        if (!exists) {
            throw new IllegalArgumentException("owner not found");
        }
    }
}
