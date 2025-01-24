package com.picktoss.picktossserver.domain.notification.repository;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "WHERE n.notificationStatus = :notificationStatus")
    List<Notification> findAllByNotificationStatus(
            @Param("notificationStatus") NotificationStatus notificationStatus
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE n.id IN :notificationIds")
    List<Notification> findAllByNotificationIds(
            @Param("notificationIds") List<Long> notificationIds
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE n.notificationType = :notificationType")
    List<Notification> findAllByNotificationType(
            @Param("notificationType") NotificationType notificationType
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    List<Notification> findAllByTitleOrContent(
            @Param("keyword") String keyword
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE n.title LIKE %:keyword%")
    List<Notification> findAllByTitle(
            @Param("keyword") String keyword
    );

    @Query("SELECT n FROM Notification n " +
            "WHERE n.content LIKE %:keyword%")
    List<Notification> findAllByContent(
            @Param("keyword") String keyword
    );
}
