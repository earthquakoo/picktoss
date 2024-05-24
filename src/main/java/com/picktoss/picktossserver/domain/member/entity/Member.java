package com.picktoss.picktossserver.domain.member.entity;


import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.payment.entity.Payment;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "continuous_solved_date_count")
    private int continuousQuizDatesCount;

    @Column(name = "is_quiz_notification_enabled", nullable = false)
    private boolean isQuizNotificationEnabled;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSet> quizSets = new ArrayList<>();

    public void updateContinuousQuizDatesCount(boolean isContinuous) {
        if (isContinuous) {
            this.continuousQuizDatesCount += 1;
        } else {
            this.continuousQuizDatesCount = 0;
        }
    }

    public void updateMemberName(String name) {
        this.name = name;
    }

    public void updateMemberEmail(String email) {
        this.email = email;
    }
}
