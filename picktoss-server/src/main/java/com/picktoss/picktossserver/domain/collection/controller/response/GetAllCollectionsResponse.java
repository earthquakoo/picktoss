package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.CollectionDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllCollectionsResponse {

    private List<GetAllCollectionsDto> collections;

    @Getter
    @Builder
    public static class GetAllCollectionsDto {
        private Long id;
        private String name;
        private String emoji;
        private int bookmarkCount;
        private CollectionDomain collectionDomain;
        private String memberName;
        private int quizCount;
    }
}
