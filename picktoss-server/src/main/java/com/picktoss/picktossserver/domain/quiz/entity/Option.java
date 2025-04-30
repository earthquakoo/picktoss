package com.picktoss.picktossserver.domain.quiz.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Option extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "options", nullable = false)
    private String option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;


    public void updateOptionContent(String option) {
        this.option = option;
    }
}
