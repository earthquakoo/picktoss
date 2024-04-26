package com.picktoss.picktossserver.domain.keypoint.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "question_set")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KeyPointSet extends AuditBase {

    @Id
    @Column(name = "id", length = 300)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "keyPointSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyPointKeyPointSet> keyPointKeyPointSets = new ArrayList<>();
}
