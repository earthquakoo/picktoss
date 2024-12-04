package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.MAX_POSSESS_DOCUMENT_COUNT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Long createMember(Member member) {
        memberRepository.save(member);
        return member.getId();
    }

    @Transactional
    public Member createGoogleMember(String name, String clientId, String email) {
        Member member = Member.createGoogleMember(name, clientId, email);
        memberRepository.save(member);
        return member;
    }

    @Transactional
    public Member createKakaoMember(String name, String clientId) {
        Member member = Member.createKakaoMember(name, clientId);
        memberRepository.save(member);
        return member;
    }

    public GetMemberInfoResponse findMemberInfo(
            Member member,
            int possessDocumentCount,
            int star
    ) {

        GetMemberInfoResponse.GetMemberInfoDocumentDto documentDto = GetMemberInfoResponse.GetMemberInfoDocumentDto.builder()
                .possessDocumentCount(possessDocumentCount)
                .maxPossessDocumentCount(MAX_POSSESS_DOCUMENT_COUNT)
                .build();


        String email = Optional.ofNullable(member.getEmail()).orElse("");

        return GetMemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(email)
                .socialPlatform(member.getSocialPlatform())
                .interestField(member.getInterestCollectionFields())
                .role(member.getRole())
                .documentUsage(documentDto)
                .star(star)
                .isQuizNotificationEnabled(member.isQuizNotificationEnabled())
                .build();
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    public Optional<Member> findMemberByGoogleClientId(String googleClientId) {
        return memberRepository.findByClientId(googleClientId);
    }

    public Optional<Member> findMemberByClientId(String clientId) {
        return memberRepository.findByClientId(clientId);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }

    @Transactional
    public void updateMemberName(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateMemberName(name);
    }

    @Transactional
    public void updateQuizNotification(Long memberId, boolean isQuizNotification) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateQuizNotification(isQuizNotification);
    }

    @Transactional
    public void updateInterestCollectionFields(Long memberId, List<String> interestCollectionFields) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateInterestCollectionFields(interestCollectionFields);
    }

    @Transactional
    public void updateTodayQuizCount(Long memberId, Integer todayQuizCount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateTodayQuizCount(todayQuizCount);
    }

    @Transactional
    public void createMemberForTest() {
        String nickname = "test Member";
        String clientId = "test client id";

        List<Member> members = new ArrayList<>();

        for (int i = 0; i < 999; i++) {
            Member member = Member.builder()
                    .name(nickname)
                    .clientId(clientId)
                    .socialPlatform(SocialPlatform.GOOGLE)
                    .isQuizNotificationEnabled(false)
                    .role(MemberRole.ROLE_USER)
                    .build();

            members.add(member);
        }

        String insertQuizSetQuizzesSql = "INSERT INTO member (name, client_id, social_platform, is_quiz_notification_enabled, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                insertQuizSetQuizzesSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, nickname);
                        ps.setObject(2, clientId);
                        ps.setObject(3, "GOOGLE");
                        ps.setObject(4, false);
                        ps.setObject(5, "ROLE_USER");
                        ps.setObject(6, LocalDateTime.now());
                        ps.setObject(7, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return members.size();
                    }
                }
        );
    }

    public Tuple findMinIdAndMaxIdAndIsQuizNotificationEnabled() {
        return memberRepository.findMinIdAndMaxIdAndIsQuizNotificationEnabled();
    }
}
