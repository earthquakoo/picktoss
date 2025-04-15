package com.picktoss.picktossserver.domain.member.entity;


import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.entity.DocumentComplaint;
import com.picktoss.picktossserver.domain.feedback.entity.Feedback;
import com.picktoss.picktossserver.domain.payment.entity.Payment;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Star star;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Directory> directories = new HashSet<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSet> quizSets = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<DocumentBookmark> documentBookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<DocumentComplaint> documentComplaints = new ArrayList<>();


    public static Member createGoogleMember(String name, String clientId, String email) {
        return Member.builder()
                .name(name)
                .clientId(clientId)
                .socialPlatform(SocialPlatform.GOOGLE)
                .email(email)
                .isQuizNotificationEnabled(true)
                .role(MemberRole.ROLE_USER)
                .build();
    }

    public static Member createKakaoMember(String name, String clientId) {
        return Member.builder()
                .name(name)
                .clientId(clientId)
                .socialPlatform(SocialPlatform.KAKAO)
                .isQuizNotificationEnabled(false)
                .role(MemberRole.ROLE_USER)
                .build();
    }

    public void updateMemberName(String name) {
        this.name = name;
    }

    public void updateMemberEmail(String email) {
        this.email = email;
    }

    public void updateMemberCategory(Category category) {
        this.category = category;
    }

    public void updateQuizNotification(boolean isQuizNotificationEnabled) {
        this.isQuizNotificationEnabled = isQuizNotificationEnabled;
    }
}
