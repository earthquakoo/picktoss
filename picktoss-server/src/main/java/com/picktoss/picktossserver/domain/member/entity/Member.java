package com.picktoss.picktossserver.domain.member.entity;


import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.payment.entity.Payment;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.MemberRole;
import com.picktoss.picktossserver.global.enums.SocialPlatform;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "social_platform", nullable = false)
    private SocialPlatform socialPlatform;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "is_quiz_notification_enabled", nullable = false)
    private boolean isQuizNotificationEnabled;

    @Column(name = "ai_pick_count", nullable = false)
    private int aiPickCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Collection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<CollectionBookmark> collectionBookmarks = new ArrayList<>();

    public void useAiPick() {
        this.aiPickCount -= 1;
    }
    
    public void updateMemberName(String name) {
        this.name = name;
    }

    public void updateMemberEmail(String email) {
        this.email = email;
    }

    public void updateQuizNotification(boolean isQuizNotificationEnabled) {
        this.isQuizNotificationEnabled = isQuizNotificationEnabled;
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    public void changeAiPickCountForTest(int aiPickCount) {
        this.aiPickCount = aiPickCount;
    }
}
