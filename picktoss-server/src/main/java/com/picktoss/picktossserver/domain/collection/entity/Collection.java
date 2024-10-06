package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.CollectionDomain;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "tag")
    private String tag;

    @Column(name = "solved_count")
    private Integer solvedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_domain")
    private CollectionDomain collectionDomain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "collection", orphanRemoval = true)
    private List<CollectionQuiz> collectionQuizzes = new ArrayList<>();

    @OneToMany(mappedBy = "collection", orphanRemoval = true)
    private List<CollectionBookmark> collectionBookmarks = new ArrayList<>();

    // Constructor methods
    public static Collection createCollection(
            String name, String emoji, String description, String tag, CollectionDomain collectionDomain, Member member
    ) {
        return Collection.builder()
                .name(name)
                .emoji(emoji)
                .description(description)
                .tag(tag)
                .solvedCount(0)
                .collectionDomain(collectionDomain)
                .member(member)
                .build();
    }

    public void updateCollectionByUpdateCollectionInfo(String name, String tag, String description, String emoji, CollectionDomain collectionDomain) {
        if (name != null) {
            this.name = name;
        }

        if (tag != null) {
            this.tag = tag;
        }

        if (description != null) {
            this.description = description;
        }

        if (emoji != null) {
            this.emoji = emoji;
        }

        if (collectionDomain != null) {
            this.collectionDomain = collectionDomain;
        }
    }

}
