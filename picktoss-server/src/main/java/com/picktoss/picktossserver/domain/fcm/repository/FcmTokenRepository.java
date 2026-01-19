package com.picktoss.picktossserver.domain.fcm.repository;

import com.picktoss.picktossserver.domain.fcm.entity.FcmToken;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    @Query("SELECT n FROM Notification n " +
            "WHERE n.notificationStatus = :notificationStatus " +
            "AND n.isActive = true")
    List<Notification> findAllByNotificationStatusAndIsActiveTrue(
            @Param("notificationStatus") NotificationStatus notificationStatus
    );

    @Query("SELECT ft FROM FcmToken ft " +
            "WHERE ft.member.id = :memberId")
    List<FcmToken> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT ft FROM FcmToken ft " +
            "WHERE ft.member.id = :memberId")
    Optional<FcmToken> findByMemberId(
            @Param("memberId") Long memberId
    );

    boolean existsByMemberId(Long memberId);
}
