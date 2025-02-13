package com.picktoss.picktossserver.domain.admin.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class DeleteNotificationRequest {
    private List<Long> notificationIds;
}
