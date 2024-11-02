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

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FeedbackType type;

//    @Column(name = "email")
//    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Feedback createFeedback(String title, String content, FeedbackType type, Member member) {
        return Feedback.builder()
                .title(title)
                .content(content)
                .type(type)
                .member(member)
                .build();
    }
}
