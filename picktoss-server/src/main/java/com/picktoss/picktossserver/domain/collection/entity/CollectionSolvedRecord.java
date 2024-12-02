package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Table(name = "collection_solved_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionSolvedRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
