package com.picktoss.picktossserver.domain.auth.service;

import com.picktoss.picktossserver.core.email.MailgunEmailSenderManager;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.auth.repository.EmailVerificationRepository;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.member.dto.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCreateService {

    private final MailgunEmailSenderManager mailgunEmailSenderManager;
    private final EmailVerificationRepository emailVerificationRepository;
    private final S3Provider s3Provider;
    private final RedisUtil redisUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final StarRepository starRepository;
    private final StarHistoryRepository starHistoryRepository;
    private final DirectoryRepository directoryRepository;

    @Transactional
    public JwtTokenDto createMember(MemberInfoDto memberInfoDto) {
        Optional<Member> optionalMember = memberRepository.findByClientId(memberInfoDto.getSub());

        if (optionalMember.isEmpty()) {
            Member member = memberInfoDto.toEntity();
            memberRepository.save(member);

            Star star = Star.createStar(StarConstant.SIGN_UP_STAR, member);
            StarHistory starHistory = StarHistory.createStarHistory("회원 가입", StarConstant.SIGN_UP_STAR, StarConstant.SIGN_UP_STAR, TransactionType.DEPOSIT, Source.SIGN_UP, star);

            starRepository.save(star);
            starHistoryRepository.save(starHistory);

            Directory directory = Directory.createDefaultDirectory(member);
            directoryRepository.save(directory);

            return jwtTokenProvider.generateToken(member);
        }
        Member member = optionalMember.get();
        return jwtTokenProvider.generateToken(member);
    }
}
