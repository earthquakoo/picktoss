package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import com.picktoss.picktossserver.global.enums.document.PublicQuizCollectionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionCreateService {

    private final DocumentRepository documentRepository;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;

    @Transactional
    public void createPublicQuizCollection(Long memberId, Long documentId, String explanation, PublicQuizCollectionCategory publicQuizCollectionCategory) {
        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_NOT_FOUND));

        PublicQuizCollection publicQuizCollection = PublicQuizCollection.createPublicQuizCollection(explanation, publicQuizCollectionCategory, document);

        publicQuizCollectionRepository.save(publicQuizCollection);
    }
}
