package com.example.photobook.repository;

import com.example.photobook.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {
    Optional<Upload> findByKey(String key);
    Optional<Upload> findByOwnerTypeAndOwnerId(com.example.photobook.entity.enumirated.OwnerType ownerType, UUID ownerId);
}
