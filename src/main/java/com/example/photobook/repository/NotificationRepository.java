package com.example.photobook.repository;

import com.example.photobook.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtAsc(UUID userId);
    void deleteByOrderId(UUID orderId);
    void deleteByOrderIdAndUserIdIn(UUID orderId, Collection<UUID> userIds);
}
