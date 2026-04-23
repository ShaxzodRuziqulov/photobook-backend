package com.example.photobook.repository;

import com.example.photobook.entity.Notification;
import com.example.photobook.entity.enumirated.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtAsc(UUID userId);
    long countByUserIdAndReadAtIsNull(UUID userId);

    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.user.id = :userId
              AND (:search IS NULL OR :search = '' OR
                   LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(n.orderName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(n.employeeName, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:type IS NULL OR n.type = :type)
              AND (:actionRequired IS NULL OR n.actionRequired = :actionRequired)
              AND (:isRead IS NULL OR
                   (:isRead = true AND n.readAt IS NOT NULL) OR
                   (:isRead = false AND n.readAt IS NULL))
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findPage(@Param("userId") UUID userId,
                                @Param("search") String search,
                                @Param("type") NotificationType type,
                                @Param("isRead") Boolean isRead,
                                @Param("actionRequired") Boolean actionRequired,
                                Pageable pageable);

    void deleteByOrderId(UUID orderId);
    void deleteByOrderIdAndUserIdIn(UUID orderId, Collection<UUID> userIds);
}
