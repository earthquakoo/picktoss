package com.picktoss.picktossserver.domain.publicquizcollection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "public_quiz_collection_bookmark")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PublicQuizCollectionBookmark extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_quiz_collection_id", nullable = false)
    private PublicQuizCollection publicQuizCollection;

    public static PublicQuizCollectionBookmark createPublicQuizCollectionBookmark(Member member, PublicQuizCollection publicQuizCollection) {
        return PublicQuizCollectionBookmark.builder()
                .member(member)
                .publicQuizCollection(publicQuizCollection)
                .build();
    }
}
