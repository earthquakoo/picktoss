package com.picktoss.picktossserver.domain.notification.repository;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

//    Page<Notification> findAllByPageable(Pageable pageable);

    @Query("SELECT n FROM Notification n " +
            "WHERE n.notificationStatus = :notificationStatus " +
            "AND n.isActive = true")
    List<Notification> findAllByNotificationStatusAndIsActiveTrue(
            @Param("notificationStatus") NotificationStatus notificationStatus
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE n.id IN :notificationIds")
    List<Notification> findAllByNotificationIds(
            @Param("notificationIds") List<Long> notificationIds
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE (:type IS NULL OR n.notificationType = :type) " +
            "AND (:isActive IS NULL OR n.isActive = :isActive)")
    Page<Notification> findAllByNotificationTypeOrIsActive(
            @Param("type") NotificationType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE (:keyword IS NULL OR n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
            "AND (:type IS NULL OR n.notificationType = :type) " +
            "AND (:isActive IS NULL OR n.isActive = :isActive)")
    Page<Notification> findAllByTitleOrContent(
            @Param("keyword") String keyword,
            @Param("type") NotificationType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE (:keyword IS NULL OR n.title LIKE %:keyword%) " +
            "AND (:type IS NULL OR n.notificationType = :type) " +
            "AND (:isActive IS NULL OR n.isActive = :isActive)")
    Page<Notification> findAllByTitle(
            @Param("keyword") String keyword,
            @Param("type") NotificationType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE (:keyword IS NULL OR n.content LIKE %:keyword%) " +
            "AND (:type IS NULL OR n.notificationType = :type) " +
            "AND (:isActive IS NULL OR n.isActive = :isActive)")
    Page<Notification> findAllByContent(
            @Param("keyword") String keyword,
            @Param("type") NotificationType type,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

//    @Query("SELECT n FROM Notification n " +
//            "WHERE (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
//    Page<Notification> findAllByTitleOrContent(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );
//
//    @Query("SELECT n FROM Notification n " +
//            "WHERE n.title LIKE %:keyword%")
//    Page<Notification> findAllByTitle(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );
//
//    @Query("SELECT n FROM Notification n " +
//            "WHERE n.content LIKE %:keyword%")
//    Page<Notification> findAllByContent(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );
}
