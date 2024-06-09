package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.global.enums.DocumentStatus.*;
import static com.picktoss.picktossserver.global.enums.DocumentStatus.PROCESSED;

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

    @Column(name = "orders", nullable = false)
    private int order;

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
    private List<KeyPoint> keyPoints = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    // Constructor methods
    public static Document createDocument(String name, String s3Key, int order, DocumentStatus documentStatus, boolean isTodayQuizIncluded, Category category) {
        return Document.builder()
                .name(name)
                .s3Key(s3Key)
                .order(order)
                .status(documentStatus)
                .isTodayQuizIncluded(isTodayQuizIncluded)
                .category(category)
                .build();
    }

    public static Document createDefaultDocument(String s3Key, Category category) {
        return Document.builder()
                .name("예시 문서")
                .s3Key(s3Key)
                .order(1)
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
    public void updateDocumentOrder(int order) {
        this.order = order;
    }

    public void addDocumentOrder() {
        this.order += 1;
    }

    public void minusDocumentOrder() {
        this.order -= 1;
    }

    public void moveDocumentToCategory(Category category) {
        this.category = category;
    }

    public void updateDocumentS3Key(String s3Key) {
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
