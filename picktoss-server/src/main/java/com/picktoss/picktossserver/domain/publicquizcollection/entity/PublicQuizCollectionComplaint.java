package com.picktoss.picktossserver.domain.publicquizcollection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "public_quiz_collection_complaint")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PublicQuizCollectionComplaint extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_quiz_collection_id", nullable = false)
    private PublicQuizCollection publicQuizCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "publicQuizCollectionComplaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PublicQuizCollectionComplaintFile> publicQuizCollectionComplaintFiles = new HashSet<>();

    public static PublicQuizCollectionComplaint createPublicQuizCollectionComplaint(String content, PublicQuizCollection publicQuizCollection, Member member) {
        return PublicQuizCollectionComplaint.builder()
                .content(content)
                .publicQuizCollection(publicQuizCollection)
                .member(member)
                .build();

    }
}
