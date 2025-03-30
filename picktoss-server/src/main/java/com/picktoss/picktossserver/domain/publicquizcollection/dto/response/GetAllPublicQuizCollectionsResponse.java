package com.picktoss.picktossserver.domain.publicquizcollection.dto.response;

import com.picktoss.picktossserver.global.enums.document.PublicQuizCollectionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllPublicQuizCollectionsResponse {

    private List<PublicQuizCollectionsDto> publicQuizCollections;

    @Getter
    @Builder
    public static class PublicQuizCollectionsDto {
        private Long id;
        private String title;
        private String explanation;
        private String emoji;
        private PublicQuizCollectionCategory publicQuizCollectionCategory;
        private int quizCount;
        private int tryCount;
        private boolean isBookmarked;
    }
}
