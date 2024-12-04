package com.picktoss.picktossserver.domain.member.entity;


import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.member.constant.MemberConstant;
import com.picktoss.picktossserver.domain.payment.entity.TossPayment;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import com.picktoss.picktossserver.global.utils.StringListConvert;
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

    @Column(name = "today_quiz_count", nullable = false)
    private Integer todayQuizCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Convert(converter = StringListConvert.class)
    @Column(name = "interest_collection_fields")
    private List<String> interestCollectionFields;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Star star;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Directory> directories = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSet> quizSets = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Collection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<CollectionBookmark> collectionBookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<CollectionSolvedRecord> collectionSolvedRecords = new ArrayList<>();

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<TossPayment> tossPayments = new ArrayList<>();

    public static Member createGoogleMember(String name, String clientId, String email) {
        return Member.builder()
                .name(name)
                .clientId(clientId)
                .socialPlatform(SocialPlatform.GOOGLE)
                .email(email)
                .isQuizNotificationEnabled(true)
                .todayQuizCount(MemberConstant.DEFAULT_TODAY_QUIZ_COUNT)
                .role(MemberRole.ROLE_USER)
                .build();
    }

    public static Member createKakaoMember(String name, String clientId) {
        return Member.builder()
                .name(name)
                .clientId(clientId)
                .socialPlatform(SocialPlatform.KAKAO)
                .isQuizNotificationEnabled(false)
                .todayQuizCount(MemberConstant.DEFAULT_TODAY_QUIZ_COUNT)
                .role(MemberRole.ROLE_USER)
                .build();
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

    public void updateInterestCollectionFields(List<String> interestCollectionFields) {
        this.interestCollectionFields = interestCollectionFields;
    }

    public void updateTodayQuizCount(Integer todayQuizCount) {
        this.todayQuizCount = todayQuizCount;
    }
}
