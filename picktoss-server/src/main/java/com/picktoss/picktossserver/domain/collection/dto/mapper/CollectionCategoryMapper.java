package com.picktoss.picktossserver.domain.collection.dto.mapper;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;

public class CollectionCategoryMapper {

    public static String mapCollectionCategoryName(CollectionCategory category) {
        switch (category) {
            case CollectionCategory.IT: return "IT·프로그래밍";
            case CollectionCategory.LAW: return "법학";
            case CollectionCategory.BUSINESS_ECONOMY: return "경영·경제";
            case CollectionCategory.HISTORY_PHILOSOPHY: return "역사·철학";
            case CollectionCategory.LANGUAGE: return "언어";
            case CollectionCategory.MEDICINE_PHARMACY: return "의학·약학";
            case CollectionCategory.SCIENCE_ENGINEERING: return "과학·공학";
            case CollectionCategory.ART: return "예술";
            case CollectionCategory.SOCIETY_POLITICS: return "사회·정치";
            default: return "기타";
        }
    }

    public static String mapCollectionCategoryEmoji(CollectionCategory category) {
        switch (category) {
            case CollectionCategory.IT: return "\uD83E\uDD16";
            case CollectionCategory.LAW: return "\uD83D\uDCD6";
            case CollectionCategory.BUSINESS_ECONOMY: return "\uD83D\uDCB0";
            case CollectionCategory.HISTORY_PHILOSOPHY: return "\uD83D\uDCDC";
            case CollectionCategory.LANGUAGE: return "\uD83D\uDCAC";
            case CollectionCategory.MEDICINE_PHARMACY: return "\uD83E\uDE7A";
            case CollectionCategory.SCIENCE_ENGINEERING: return "\uD83D\uDD2C";
            case CollectionCategory.ART: return "\uD83C\uDFA8";
            case CollectionCategory.SOCIETY_POLITICS: return "⚖\uFE0F";
            default: return "♾\uFE0F";
        }
    }
}
