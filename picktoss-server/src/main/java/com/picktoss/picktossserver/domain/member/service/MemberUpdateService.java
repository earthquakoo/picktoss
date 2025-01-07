package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberUpdateService {

    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;
    private final RedisUtil redisUtil;

    @Transactional
    public void updateTodayQuizCount(Long memberId, Integer todayQuizCount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateTodayQuizCount(todayQuizCount);
    }

    @Transactional
    public void updateInterestCollectionCategories(Long memberId, List<String> interestCollectionCategories) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateInterestCollectionCategories(interestCollectionCategories);
    }

    @Transactional
    public void updateQuizNotification(Long memberId, boolean isQuizNotification) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateQuizNotification(isQuizNotification);
    }

    @Transactional
    public void updateMemberName(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateMemberName(name);
    }
}
