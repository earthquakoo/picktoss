package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.eventlistener.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.sqs.SQSEventMessagePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import com.picktoss.picktossserver.domain.outbox.repository.OutboxRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.MAX_POSSESS_DOCUMENT_COUNT;
import static com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus.UNPROCESSED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentCreateService {


    private final DocumentRepository documentRepository;
    private final DirectoryRepository directoryRepository;
    private final StarHistoryRepository starHistoryRepository;
    private final SQSEventMessagePublisher sqsEventMessagePublisher;
    private final S3UploadPublisher s3UploadPublisher;
    private final OutboxRepository outboxRepository;

    @Value("${picktoss.default_document_s3_key}")
    private String defaultDocumentS3Key;

    @Transactional
    public Long createDocument(String documentName, MultipartFile file, DocumentType documentType, QuizType quizType, Integer starCount, Long directoryId, Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();

        if (possessDocumentCount >= MAX_POSSESS_DOCUMENT_COUNT) {
            throw new CustomException(DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
        }

        Directory directory = directoryRepository.findDirectoryWithMemberAndStarAndStarHistoryByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));
        Star star = directory.getMember().getStar();
        StarHistory starHistory = star.withdrawalStarByCreateDocument(star, starCount);
        starHistoryRepository.save(starHistory);

        String s3Key = UUID.randomUUID().toString();
        Document document = Document.createDocument(documentName, s3Key, UNPROCESSED, documentType, directory);

        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(memberId, s3Key, document.getId(), quizType, starCount));

        documentRepository.save(document);
        Outbox outbox = Outbox.createOutbox(OutboxStatus.WAITING, quizType, starCount, document);
        outboxRepository.save(outbox);

        return document.getId();
    }

    // 퀴즈 추가로 생성하기
    @Transactional
    public void createAdditionalQuizzes(Long documentId, Long memberId, QuizType quizType, Integer starCount) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();
        if (possessDocumentCount >= MAX_POSSESS_DOCUMENT_COUNT) {
            throw new CustomException(DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
        }

        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentStatusProcessingByGenerateQuizzes();

        Star star = document.getDirectory().getMember().getStar();
        StarHistory starHistory = star.withdrawalStarByCreateDocument(star, starCount);
        starHistoryRepository.save(starHistory);

        Outbox.createOutbox(OutboxStatus.WAITING, quizType, starCount, document);
        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(memberId, document.getS3Key(), document.getId(), quizType, starCount));
    }

    @Transactional
    public Document createDefaultDocument(Directory directory) {
        Document document = Document.createDefaultDocument(defaultDocumentS3Key, directory);
        documentRepository.save(document);
        return document;
    }
}
