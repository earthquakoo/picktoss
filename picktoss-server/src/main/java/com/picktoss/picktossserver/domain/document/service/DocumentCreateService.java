package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.eventlistener.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.sqs.SQSEventMessagePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.messagesource.MessageService;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import com.picktoss.picktossserver.domain.outbox.repository.OutboxRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import com.picktoss.picktossserver.global.enums.subscription.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
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
    private final MemberRepository memberRepository;

    private final MessageService messageService;

    @Transactional
    public Long createDocument(MultipartFile file, DocumentType documentType, Boolean isPublic, Integer starCount, Long memberId, String language) {
        SubscriptionPlanType subscriptionPlanType = findMemberSubscriptionPlanType(memberId);

        List<Directory> directories = directoryRepository.findAllByMemberId(memberId);
        Directory directory = directories.getFirst();

        Star star = directory.getMember().getStar();
        withdrawalStarByCreateDocument(star, starCount, subscriptionPlanType);

        String s3Key = UUID.randomUUID().toString();
        Document document = Document.createDocument(s3Key, isPublic, UNPROCESSED, documentType, directory, language);
        documentRepository.save(document);

        createOutboxByCreateDocument(starCount, document);

        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(memberId, s3Key, document.getId(), starCount));

        return document.getId();
    }

    // 퀴즈 추가로 생성하기
    @Transactional
    public void createAdditionalQuizzes(Long documentId, Long memberId, Integer starCount) {
        SubscriptionPlanType subscriptionPlanType = findMemberSubscriptionPlanType(memberId);

        Document document = updateDocumentStatusProcessingByGenerateQuizzes(documentId, memberId);

        Star star = document.getDirectory().getMember().getStar();
        withdrawalStarByCreateDocument(star, starCount, subscriptionPlanType);

        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(memberId, document.getS3Key(), document.getId(), starCount));

        createOutboxByCreateDocument(starCount, document);
    }

    private SubscriptionPlanType findMemberSubscriptionPlanType(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Subscription subscription = member.getSubscriptions().getFirst();
        return subscription.getSubscriptionPlanType();
    }

    @Transactional
    private void withdrawalStarByCreateDocument(Star star, int starCount, SubscriptionPlanType subscriptionPlanType) {
        String description = messageService.getMessage("star.history.quiz_generation");
        StarHistory starHistory = star.withdrawalStarByCreateDocument(star, starCount, subscriptionPlanType, description);
        starHistoryRepository.save(starHistory);
    }

    @Transactional
    private void createOutboxByCreateDocument(Integer starCount, Document document) {
        Outbox outbox = Outbox.createOutbox(OutboxStatus.WAITING, starCount, document);
        outboxRepository.save(outbox);
    }

    @Transactional
    private Document updateDocumentStatusProcessingByGenerateQuizzes(Long documentId, Long memberId) {
        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentStatusProcessingByGenerateQuizzes();

        return document;
    }
}
