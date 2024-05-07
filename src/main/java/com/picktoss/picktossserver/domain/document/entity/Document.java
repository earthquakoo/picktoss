package com.picktoss.picktossserver.domain.document.entity;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "orders")
    private int order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "quiz_generation_status", nullable = false)
    private boolean quizGenerationStatus;

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
    public static Document createDocument(String name, String s3Key, int order, DocumentStatus status, boolean quizGenerationStatus, Category category) {
        Document document = Document.builder()
                .name(name)
                .s3Key(s3Key)
                .order(order)
                .status(status)
                .quizGenerationStatus(quizGenerationStatus)
                .category(category)
                .build();

        document.setCategory(category);
        return document;
    }

    // 연관관계 메서드
    public void setCategory(Category category) {
        this.category = category;
        category.getDocuments().add(this);
    }

    // Business Logics
    public void changeDocumentOrder(int order) {
        this.order = order;
    }

    public void moveDocumentToCategory(Category category) {
        this.category = category;
    }
}
