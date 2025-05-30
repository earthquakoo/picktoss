package com.picktoss.picktossserver.domain.notification.entity;

import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import com.picktoss.picktossserver.global.utils.StringListConvert;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "memo")
    private String memo;

    @Column(name = "notification_key", nullable = false)
    private String notificationKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false)
    private NotificationStatus notificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_target", nullable = false)
    private NotificationTarget notificationTarget;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "notification_time", nullable = false)
    private LocalDateTime notificationTime;

    @Convert(converter = StringListConvert.class)
    @Column(name = "repeat_days")
    private List<String> repeatDays;

    public static Notification createNotification(String title, String content, String memo, String notificationKey, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<String> repeatDays) {
        return Notification.builder()
                .title(title)
                .content(content)
                .memo(memo)
                .notificationKey(notificationKey)
                .notificationType(notificationType)
                .notificationStatus(NotificationStatus.PENDING)
                .notificationTarget(notificationTarget)
                .isActive(isActive)
                .notificationTime(notificationTime)
                .repeatDays(repeatDays)
                .build();
    }

    public void updateNotificationStatusComplete() {
        this.notificationStatus = NotificationStatus.COMPLETE;
    }

    public void updateNotificationStatusPending() {
        this.notificationStatus = NotificationStatus.PENDING;
    }

    public void updateNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public void updateNotificationSendTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public void updateNotificationIsActiveFalse() {
        this.isActive = false;
    }

    public void updateNotificationInfo(String title, String content, String memo, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<String> repeatDays) {
        if (title != null) {
            this.title = title;
        }

        if (content != null) {
            this.content = content;
        }

        if (memo != null) {
            this.memo = memo;
        }

        if (notificationType != null) {
            this.notificationType = notificationType;
        }

        if (notificationTarget != null) {
            this.notificationTarget = notificationTarget;
        }

        if (isActive != null) {
            this.isActive = isActive;
        }

        if (notificationTime != null) {
            this.notificationTime = notificationTime;
        }

        if (repeatDays != null) {
            this.repeatDays = repeatDays;
        }
    }
}
