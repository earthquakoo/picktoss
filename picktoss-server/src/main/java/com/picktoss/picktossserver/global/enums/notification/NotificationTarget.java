package com.picktoss.picktossserver.global.enums.notification;

public enum NotificationTarget {
    ALL,
    // 오늘의 퀴즈
    QUIZ_INCOMPLETE_STATUS, // 퀴즈 미완료 상태
    QUIZ_INCOMPLETE_STATUS_FOUR_DAYS, // 퀴즈 미완료 상태 4일 지속
    // 컬렉션
    COLLECTION_NOT_GENERATE, // 컬렉션 미등록 상태
    IT, LAW, BUSINESS_ECONOMY, SOCIETY_POLITICS, LANGUAGE, MEDICINE_PHARMACY, ART, SCIENCE_ENGINEERING, HISTORY_PHILOSOPHY
}
