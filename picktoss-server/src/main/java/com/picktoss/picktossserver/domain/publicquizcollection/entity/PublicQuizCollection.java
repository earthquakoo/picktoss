package com.picktoss.picktossserver.domain.publicquizcollection.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.document.PublicQuizCollectionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "public_quiz_collection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PublicQuizCollection extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "explanation", nullable = false)
    private String explanation;

    @Column(name = "try_count", nullable = false)
    private int tryCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "public_quiz_collection_category")
    private PublicQuizCollectionCategory publicQuizCollectionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @OneToMany(mappedBy = "publicQuizCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PublicQuizCollectionBookmark> publicQuizCollectionBookmarks = new HashSet<>();


    public static PublicQuizCollection createPublicQuizCollection(String explanation, PublicQuizCollectionCategory publicQuizCollectionCategory, Document document) {
        return PublicQuizCollection.builder()
                .explanation(explanation)
                .tryCount(0)
                .publicQuizCollectionCategory(publicQuizCollectionCategory)
                .document(document)
                .build();
    }

    public void updateTryCountByPublicQuizCollectionSet() {
        this.tryCount += 1;
    }
}
