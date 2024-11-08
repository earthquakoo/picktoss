package com.picktoss.picktossserver.domain.feedback.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Feedback {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FeedbackType type;

    @Column(name = "email", nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Feedback createFeedback(String title, String content, String s3Key, FeedbackType type, String email, Member member) {
        return Feedback.builder()
                .title(title)
                .content(content)
                .s3Key(s3Key)
                .type(type)
                .email(email)
                .member(member)
                .build();
    }
}
