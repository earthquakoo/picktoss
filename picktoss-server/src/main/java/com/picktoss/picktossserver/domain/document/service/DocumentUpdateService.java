package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3DeleteEvent;
import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.eventlistener.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3DeletePublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.sqs.SQSEventMessagePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.MAX_POSSESS_DOCUMENT_COUNT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentUpdateService {

    private final DocumentRepository documentRepository;
    private final DirectoryRepository directoryRepository;
    private final StarHistoryRepository starHistoryRepository;
    private final SQSEventMessagePublisher sqsEventMessagePublisher;
    private final S3UploadPublisher s3UploadPublisher;
    private final S3DeletePublisher s3DeletePublisher;


    @Transactional
    public void moveDocumentToDirectory(List<Long> documentIds, Long memberId, Long directoryId) {
        Directory directory = directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));

        List<Document> documents = documentRepository.findByDocumentIdsInAndMemberId(documentIds, memberId);
        for (Document document : documents) {
            document.moveDocumentToDirectory(directory);
        }
    }

    @Transactional
    public void updateDocumentContent(MultipartFile file, Long documentId, Long memberId, String name) {
        String s3Key = UUID.randomUUID().toString();

        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentS3KeyByUpdatedContent(s3Key);
        document.updateDocumentName(name);

        s3DeletePublisher.s3DeletePublisher(new S3DeleteEvent(s3Key));
        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
    }

    @Transactional
    public void updateDocumentName(Long documentId, Long memberId, String documentName) {
        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentName(documentName);
    }

    @Transactional
    public void selectDocumentToNotGenerateByTodayQuiz(Map<Long, Boolean> documentIdTodayQuizMap, Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        for (Document document : documents) {
            if (documentIdTodayQuizMap.containsKey(document.getId())) {
                document.updateDocumentIsTodayQuizIncluded(documentIdTodayQuizMap.get(document.getId()));
            }
        }
    }


}
