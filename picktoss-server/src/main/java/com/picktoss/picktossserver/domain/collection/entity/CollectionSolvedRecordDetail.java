package com.picktoss.picktossserver.domain.collection.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "collection_solved_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionSolvedRecordDetail extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "elapsed_time_ms")
    private Integer elapsedTime;

    @Column(name = "is_answer")
    private Boolean isAnswer;

    @Column(name = "chose_answer")
    private String choseAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_solved_record_id", nullable = false)
    private CollectionSolvedRecord collectionSolvedRecord;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "quiz_id", nullable = false)
//    private Quiz quiz;
}
