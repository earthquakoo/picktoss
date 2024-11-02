package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.document.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static com.picktoss.picktossserver.global.enums.document.DocumentStatus.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "is_today_quiz_included", nullable = false)
    private boolean isTodayQuizIncluded;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL,  orphanRemoval = true)
    private Set<Quiz> quizzes = new HashSet<>();

    // Constructor methods
    public static Document createDocument(String name, String s3Key, DocumentStatus documentStatus, boolean isTodayQuizIncluded, Category category) {
        return Document.builder()
                .name(name)
                .s3Key(s3Key)
                .status(documentStatus)
                .isTodayQuizIncluded(isTodayQuizIncluded)
                .category(category)
                .build();
    }

    public static Document createDefaultDocument(String s3Key, Category category) {
        return Document.builder()
                .name("예시 문서")
                .s3Key(s3Key)
                .status(DocumentStatus.DEFAULT_DOCUMENT)
                .isTodayQuizIncluded(false)
                .category(category)
                .build();
    }

    // 연관관계 메서드
    public void setCategory(Category category) {
        this.category = category;
        category.getDocuments().add(this);
    }

    // Business Logics
    public void moveDocumentToCategory(Category category) {
        this.category = category;
    }

    public void updateDocumentS3KeyByUpdatedContent(String s3Key) {
        this.s3Key = s3Key;
    }

    public void updateDocumentName(String name) {
        this.name = name;
    }

    public void updateDocumentStatusProcessingByGenerateAiPick() {
        this.status = DocumentStatus.PROCESSING;
    }

    public void updateDocumentStatusKeyPointUpdatePossibleByUpdatedDocument() {
        this.status = DocumentStatus.KEYPOINT_UPDATE_POSSIBLE;
    }

    public void updateDocumentIsTodayQuizIncluded(Boolean isTodayQuizIncluded) {
        this.isTodayQuizIncluded = isTodayQuizIncluded;
    }

    public DocumentStatus updateDocumentStatusClientResponse(DocumentStatus documentStatus) {
        if (documentStatus == PARTIAL_SUCCESS ||
                documentStatus == PROCESSED ||
                documentStatus == COMPLETELY_FAILED) {
            documentStatus = PROCESSED;
        } else {
            return documentStatus;
        }
        return documentStatus;
    }
}
