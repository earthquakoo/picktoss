package com.picktoss.picktossserver.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "feedback_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedbackFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    public static FeedbackFile createFeedbackFile(String s3Key, Feedback feedback) {
        return FeedbackFile.builder()
                .s3Key(s3Key)
                .feedback(feedback)
                .build();
    }
}
