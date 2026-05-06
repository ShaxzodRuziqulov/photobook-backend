package com.example.photobook.service;

import com.example.photobook.dto.UploadDto;
import com.example.photobook.dto.UploadResponseDto;
import com.example.photobook.entity.Upload;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.repository.ExpenseRepository;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.UploadRepository;
import com.example.photobook.repository.UserRepository;
import org.springframework.util.unit.DataSize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UploadService {
    private final UploadRepository repository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;
    private final Path root;
    private final long maxFileSizeBytes;

    public UploadService(
            UploadRepository repository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            ExpenseRepository expenseRepository,
            @Value("${app.upload.dir:uploads-storage}") String uploadDir,
            @Value("${spring.servlet.multipart.max-file-size:20MB}") DataSize maxFileSize) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.expenseRepository = expenseRepository;
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSize.toBytes();
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
        if (file.getSize() > maxFileSizeBytes) {
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

    /**
     * Accepts either persisted upload id (UUID, as returned by POST /uploads) or storage key
     * (e.g. {@code uuid-filename.png}).
     */
    public void delete(String idOrKey) {
        if (idOrKey == null || idOrKey.isBlank()) {
            throw new IllegalArgumentException("key is required");
        }

        Optional<Upload> byId = parseUuid(idOrKey).flatMap(repository::findById);
        Upload upload = byId.or(() -> repository.findByKey(idOrKey))
                .orElseThrow(() -> new IllegalArgumentException("key not found"));
        deleteUpload(upload);
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public void deleteOwnedUpload(OwnerType ownerType, UUID ownerId, UUID keepUploadId) {
        if (ownerType == null || ownerId == null) {
            return;
        }

        repository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .filter(upload -> !upload.getId().equals(keepUploadId))
                .ifPresent(this::deleteUpload);
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
        if (upload.getOwnerType() != null && upload.getOwnerId() != null
                && (upload.getOwnerType() != ownerType || !upload.getOwnerId().equals(ownerId))) {
            clearOwnerReference(upload.getOwnerType(), upload.getOwnerId(), upload.getId());
        }

        deleteOwnedUpload(ownerType, ownerId, uploadId);
        upload.setOwnerType(ownerType);
        upload.setOwnerId(ownerId);
        return repository.save(upload);
    }

    public String buildFileUrl(Upload upload) {
        return upload == null ? null : "/uploads-storage/" + upload.getKey();
    }

    private void deleteUpload(Upload upload) {
        try {
            clearOwnerReference(upload.getOwnerType(), upload.getOwnerId(), upload.getId());
            Files.deleteIfExists(root.resolve(upload.getKey()));
            repository.delete(upload);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to delete file");
        }
    }

    public Upload findUploadById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("upload not found"));
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

    private void clearOwnerReference(OwnerType ownerType, UUID ownerId, UUID uploadId) {
        if (ownerType == null || ownerId == null || uploadId == null) {
            return;
        }

        switch (ownerType) {
            case USER -> userRepository.findById(ownerId)
                    .filter(user -> hasUpload(user.getUpload(), uploadId))
                    .ifPresent(user -> {
                        user.setUpload(null);
                        user.setAvatarUrl(null);
                        userRepository.save(user);
                    });
            case ORDER -> orderRepository.findById(ownerId)
                    .filter(order -> hasUpload(order.getUpload(), uploadId))
                    .ifPresent(order -> {
                        order.setUpload(null);
                        order.setImageUrl(null);
                        orderRepository.save(order);
                    });
            case EXPENSE -> expenseRepository.findById(ownerId)
                    .filter(expense -> hasUpload(expense.getUpload(), uploadId))
                    .ifPresent(expense -> {
                        expense.setUpload(null);
                        expense.setReceiptImageUrl(null);
                        expenseRepository.save(expense);
                    });
        }
    }

    private boolean hasUpload(Upload upload, UUID uploadId) {
        return upload != null && uploadId.equals(upload.getId());
    }
}
