package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.directory.entity.Directory;
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
    @JoinColumn(name = "directory_id", nullable = false)
    private Directory directory;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL,  orphanRemoval = true)
    private Set<Quiz> quizzes = new HashSet<>();

    // Constructor methods
    public static Document createDocument(String name, String s3Key, DocumentStatus documentStatus, boolean isTodayQuizIncluded, Directory directory) {
        return Document.builder()
                .name(name)
                .s3Key(s3Key)
                .status(documentStatus)
                .isTodayQuizIncluded(isTodayQuizIncluded)
                .directory(directory)
                .build();
    }

    public static Document createDefaultDocument(String s3Key, Directory directory) {
        return Document.builder()
                .name("예시 문서")
                .s3Key(s3Key)
                .status(DocumentStatus.DEFAULT_DOCUMENT)
                .isTodayQuizIncluded(false)
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
        this.status = DocumentStatus.PROCESSING;
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
