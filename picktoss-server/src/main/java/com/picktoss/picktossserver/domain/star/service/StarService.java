package com.picktoss.picktossserver.domain.star.service;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.star.constant.StarConstant;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.domain.star.repository.StarRepository;
import com.picktoss.picktossserver.global.enums.star.Source;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StarService {

    private final StarRepository starRepository;
    private final StarHistoryRepository starHistoryRepository;

    @Transactional
    public void createStarBySignUp(Member member) {
        Star star = Star.createStar(StarConstant.SIGN_UP_STAR, member);
        StarHistory starHistory = StarHistory.createStarHistory("회원 가입", StarConstant.SIGN_UP_STAR, StarConstant.SIGN_UP_STAR, TransactionType.DEPOSIT, Source.SIGN_UP, star);

        starRepository.save(star);
        starHistoryRepository.save(starHistory);
    }

    @Transactional
    public void withdrawalStarByCreateDocument(Star star, Integer starCount) {
        star.withdrawalStarByCreateDocument(starCount);
        Integer curStarCount = star.getStar();
        Integer changeStarCount = curStarCount - starCount;

        StarHistory starHistory = StarHistory.createStarHistory("문서 생성으로 인한 지출", starCount, changeStarCount, TransactionType.WITHDRAWAL, Source.SERVICE, star);

        starHistoryRepository.save(starHistory);
    }

    @Transactional
    public void depositStarByTodayQuizSolvedReward(Star star, boolean isContinuousFiveDays) {
        StarHistory lastStarHistory = star.getStarHistories().getLast();
        Integer changeAmount = StarConstant.TODAY_QUIZ_REWARD_BY_ONE_DAY;
        if (isContinuousFiveDays) {
            changeAmount = StarConstant.TODAY_QUIZ_REWARD_BY_FIVE_DAY;
        }
        Integer balanceAfter = lastStarHistory.getBalanceAfter() + changeAmount;

        StarHistory starHistory = StarHistory.createStarHistory(
                "오늘의 퀴즈 보상", changeAmount, balanceAfter, TransactionType.DEPOSIT, Source.REWARD,  star);

        star.depositStarByTodayQuizSolvedReward(StarConstant.TODAY_QUIZ_REWARD_BY_ONE_DAY);
        starHistoryRepository.save(starHistory);
    }

    @Transactional
    public void depositStarByInviteFriend(Star star) {
        StarHistory lastStarHistory = star.getStarHistories().getLast();
        Integer changeAmount = StarConstant.INVITE_FRIEND_REWARD;
        Integer balanceAfter = lastStarHistory.getBalanceAfter() + changeAmount;

        StarHistory starHistory = StarHistory.createStarHistory(
                "친구 초대 보상", changeAmount, balanceAfter, TransactionType.DEPOSIT, Source.REWARD, star);

        star.depositStarByInviteFriendReward(StarConstant.INVITE_FRIEND_REWARD);
        starHistoryRepository.save(starHistory);
    }

    @Transactional
    public void depositStarByInvalidQuiz(Star star, String errorContent) {
        StarHistory lastStarHistory = star.getStarHistories().getLast();
        Integer changeAmount = StarConstant.INVALID_QUIZ_REWARD;
        Integer balanceAfter = lastStarHistory.getBalanceAfter() + changeAmount;

        StarHistory starHistory = StarHistory.createStarHistory(
                errorContent, changeAmount, balanceAfter, TransactionType.DEPOSIT, Source.REWARD, star);

        star.depositStarByInvalidQuizReward(StarConstant.INVALID_QUIZ_REWARD);
        starHistoryRepository.save(starHistory);
    }
}
