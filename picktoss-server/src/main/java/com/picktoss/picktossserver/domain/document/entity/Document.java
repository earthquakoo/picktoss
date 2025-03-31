package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus.*;

@Entity
@Getter
@Table(name = "document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Document extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

//    @Column(name = "emoji", nullable = false)
//    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_generation_status", nullable = false)
    private QuizGenerationStatus quizGenerationStatus;

    @Column(name = "is_today_quiz_included", nullable = false)
    private boolean isTodayQuizIncluded;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id", nullable = false)
    private Directory directory;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quiz> quizzes = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PublicQuizCollection> publicQuizCollections = new HashSet<>();

    // Constructor methods
    public static Document createDocument(String name, String s3Key, QuizGenerationStatus quizGenerationStatus, DocumentType documentType, Directory directory) {
        return Document.builder()
                .name(name)
                .s3Key(s3Key)
                .quizGenerationStatus(quizGenerationStatus)
                .isTodayQuizIncluded(true)
                .documentType(documentType)
                .directory(directory)
                .build();
    }

    // 연관관계 메서드
    public void setDirectory(Directory directory) {
        this.directory = directory;
        directory.getDocuments().add(this);
    }

    // Business Logics
    public void moveDocumentToDirectory(Directory directory) {
        this.directory = directory;
    }

    public void updateDocumentS3KeyByUpdatedContent(String s3Key) {
        this.s3Key = s3Key;
    }

    public void updateDocumentName(String name) {
        this.name = name;
    }

    public void updateDocumentStatusProcessingByGenerateAiPick() {
        this.quizGenerationStatus = QuizGenerationStatus.PROCESSING;
    }

    public void updateDocumentStatusProcessingByGenerateQuizzes() {
        this.quizGenerationStatus = QuizGenerationStatus.PROCESSING;
    }


    public void updateDocumentIsTodayQuizIncluded(Boolean isTodayQuizIncluded) {
        this.isTodayQuizIncluded = isTodayQuizIncluded;
    }

    public QuizGenerationStatus updateDocumentStatusClientResponse(QuizGenerationStatus quizGenerationStatus) {
        if (quizGenerationStatus == PARTIAL_SUCCESS ||
                quizGenerationStatus == PROCESSED ||
                quizGenerationStatus == COMPLETELY_FAILED) {
            quizGenerationStatus = PROCESSED;
        } else {
            return quizGenerationStatus;
        }
        return quizGenerationStatus;
    }
}
