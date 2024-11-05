package com.picktoss.picktossserver.domain.outbox.entity;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Outbox {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Column(name = "try_count", nullable = false)
    private int tryCount;

    @Column(name = "created_quiz_type", nullable = false)
    private QuizType createdQuizType;

    @Column(name = "used_stars", nullable = false)
    private Integer usedStars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Constructor methods
    public static Outbox createOutbox(OutboxStatus status, Document document) {
        return Outbox.builder()
                .status(status)
                .tryCount(0)
                .document(document)
                .build();
    }

    public void addTryCountBySendMessage() {
        this.tryCount += 1;
    }

    public void updateOutboxStatusByBatchFailed() {
        this.status = OutboxStatus.FAILED;
    }
}