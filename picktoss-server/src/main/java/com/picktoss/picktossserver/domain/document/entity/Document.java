package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
import com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus;
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

    @Column(name = "name")
    private String name;

    @Column(name = "emoji")
    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_generation_status", nullable = false)
    private QuizGenerationStatus quizGenerationStatus;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "try_count", nullable = false)
    private Integer tryCount;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id", nullable = false)
    private Directory directory;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quiz> quizzes = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DocumentBookmark> documentBookmarks = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DocumentComplaint> documentComplaints = new HashSet<>();

    // Constructor methods
    public static Document createDocument(
            String s3Key,
            Boolean isPublic,
            QuizGenerationStatus quizGenerationStatus,
            DocumentType documentType,
            Directory directory
    ) {
        return Document.builder()
                .s3Key(s3Key)
                .isPublic(isPublic)
                .tryCount(0)
                .quizGenerationStatus(quizGenerationStatus)
                .documentType(documentType)
                .directory(directory)
                .build();
    }

    // Business Logics
    public void updateDocumentS3KeyByUpdatedContent(String s3Key) {
        this.s3Key = s3Key;
    }

    public void updateDocumentName(String name) {
        this.name = name;
    }

    public void updateDocumentCategory(Category category) {
        this.category = category;
    }

    public void updateDocumentEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void updateDocumentStatusProcessingByGenerateAiPick() {
        this.quizGenerationStatus = QuizGenerationStatus.PROCESSING;
    }

    public void updateDocumentStatusProcessingByGenerateQuizzes() {
        this.quizGenerationStatus = QuizGenerationStatus.PROCESSING;
    }

    public void updateDocumentIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void updateDocumentTryCountBySolvedQuizSet() {
        this.tryCount += 1;
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