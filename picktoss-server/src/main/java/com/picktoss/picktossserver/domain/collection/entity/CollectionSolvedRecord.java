package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "collection_solved_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionSolvedRecord extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "collectionSolvedRecord", orphanRemoval = true)
    private Set<CollectionSolvedRecordDetail> collectionSolvedRecordDetails = new HashSet<>();

    public static CollectionSolvedRecord createCollectionSolvedRecord(Collection collection, Member member) {
        return CollectionSolvedRecord.builder()
                .collection(collection)
                .member(member)
                .build();
    }

}
