package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3DeleteEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3DeletePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentDeleteService {

    private final DocumentRepository documentRepository;
    private final S3DeletePublisher s3DeletePublisher;

    @Transactional
    public void deleteDocument(Long memberId, List<Long> documentIds) {
        List<Document> documents = documentRepository.findByDocumentIdsInAndMemberId(documentIds, memberId);
        for (Document document : documents) {
            s3DeletePublisher.s3DeletePublisher(new S3DeleteEvent(document.getS3Key()));
        }

        if (documents.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        documentRepository.deleteAll(documents);
    }
}
