package com.picktoss.picktossserver.global.enums.document;

import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.global.utils.DocumentBookmarkFetchFunction;

import java.util.List;

public enum BookmarkedDocumentSortOption {
    CREATED_AT(DocumentBookmarkRepository::findAllByMemberIdAndIsPublicTrueOrderByCreatedAtDesc),
    QUIZ_COUNT(DocumentBookmarkRepository::findAllByMemberIdAndIsPublicTrueOrderByQuizCountDesc),
    NAME(DocumentBookmarkRepository::findAllByMemberIdAndIsPublicTrueOrderByNameDesc); // 'else'에 해당하던 기본값

    private final DocumentBookmarkFetchFunction fetchFunction;

    BookmarkedDocumentSortOption(DocumentBookmarkFetchFunction fetchFunction) {
        this.fetchFunction = fetchFunction;
    }

    public List<DocumentBookmark> fetchDocuments(DocumentBookmarkRepository repository, Long memberId) {
        return this.fetchFunction.fetch(repository, memberId);
    }
}
