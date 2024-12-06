package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "collection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Collection extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "emoji")
    private String emoji;

    @Column(name = "description", length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_category")
    private CollectionCategory collectionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "collection", orphanRemoval = true)
    private Set<CollectionQuiz> collectionQuizzes = new HashSet<>();

    @OneToMany(mappedBy = "collection", orphanRemoval = true)
    private Set<CollectionBookmark> collectionBookmarks = new HashSet<>();

    @OneToMany(mappedBy = "collection", orphanRemoval = true)
    private Set<CollectionSolvedRecord> collectionSolvedRecords = new HashSet<>();

    // Constructor methods
    public static Collection createCollection(
            String name, String emoji, String description, CollectionCategory collectionCategory, Member member
    ) {
        return Collection.builder()
                .name(name)
                .emoji(emoji)
                .description(description)
                .collectionCategory(collectionCategory)
                .member(member)
                .build();
    }

    public void updateCollectionByUpdateCollectionInfo(String name, String description, String emoji, CollectionCategory collectionCategory) {
        if (name != null) {
            this.name = name;
        }

        if (description != null) {
            this.description = description;
        }

        if (emoji != null) {
            this.emoji = emoji;
        }

        if (collectionCategory != null) {
            this.collectionCategory = collectionCategory;
        }
    }
}
