package com.picktoss.picktossserver.domain.notification.repository;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
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
}
