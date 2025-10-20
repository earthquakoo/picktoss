package com.picktoss.picktossserver.global.enums.document;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.global.utils.DocumentFetchFunction;

import java.util.List;

public enum DocumentSortOption {
    CREATED_AT(DocumentRepository::findAllByMemberIdAndLanguageOrderByCreatedAtDesc),
    NAME(DocumentRepository::findAllByMemberIdAndLanguageOrderByNameAsc),
    QUIZ_COUNT(DocumentRepository::findAllByMemberIdAndLanguageOrderByQuizCountDesc),
    WRONG_ANSWER_COUNT(DocumentRepository::findAllByMemberIdAndLanguageWrongAnswerCount);

    private final DocumentFetchFunction fetchFunction;

    DocumentSortOption(DocumentFetchFunction fetchFunction) {
        this.fetchFunction = fetchFunction;
    }

    public List<Document> fetchDocuments(DocumentRepository repository, Long memberId, String language) {
        return this.fetchFunction.fetch(repository, memberId, language);
    }
}
