package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IntegratedSearchResponse {

    private List<IntegratedSearchDocumentDto> documents;
    private List<IntegratedSearchQuizDto> quizzes;
    private List<IntegratedSearchCollectionDto> collections;

    @Getter
    @Builder
    public static class IntegratedSearchDocumentDto {
        private Long documentId;
        private String documentName;
        private String content;
        private DocumentType documentType;
        private IntegratedSearchDirectoryDto directory;
    }

    @Getter
    @Builder
    public static class IntegratedSearchDirectoryDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class IntegratedSearchQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String documentName;
        private String directoryName;
    }

    @Getter
    @Builder
    public static class IntegratedSearchCollectionDto {
        private Long id;
        private String name;
        private String emoji;
        private int bookmarkCount;
        private CollectionCategory collectionCategory;
        private String memberName;
        private int quizCount;
    }
}
