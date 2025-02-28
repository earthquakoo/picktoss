package com.picktoss.picktossserver.domain.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.auth.dto.response.CheckInviteCodeBySignUpResponse;
import com.picktoss.picktossserver.domain.auth.dto.response.GetInviteMemberResponse;
import com.picktoss.picktossserver.domain.auth.util.AuthUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.global.enums.auth.CheckInviteCodeResponseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthInviteLinkService {

    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;
    private final AuthUtil authUtil;

    public String createInviteLink(Long memberId) {
        String initLink = "https://pick-toss-next.vercel.app/invite/";

        String memberIdKey = memberId.toString();

        // 기존 초대 코드 조회
        Optional<Map> existingCode = redisUtil.getData(RedisConstant.REDIS_INVITE_MEMBER_PREFIX, memberIdKey, Map.class);
        if (existingCode.isPresent()) {
            Map map = existingCode.get();
            String inviteCode = map.get("inviteCode").toString();
            return initLink + inviteCode;
        }

        String uniqueCode = authUtil.generateUniqueCode();
        String inviteLink = initLink + uniqueCode;

        LocalDateTime createdAt = LocalDateTime.now();

        Map<String, Object> memberIdKeyData = Map.of(
                "inviteCode", uniqueCode,
                "createdAt", createdAt,
                "expiresAt", createdAt.plusDays(3)
        );

        Map<String, Object> inviteCodeKeyData = Map.of(
                "inviteMemberId", memberId,
                "isUsed", false,
                "createdAt", createdAt,
                "expiresAt", createdAt.plusDays(3)
        );

        redisUtil.setData(RedisConstant.REDIS_INVITE_MEMBER_PREFIX, memberIdKey, memberIdKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, uniqueCode, inviteCodeKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);

        return inviteLink;
    }

    // 초대 코드 유효성 검사
    public void verifyInviteCode(String inviteCode) {
        Optional<Map> optionalInviteCodeData = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, Map.class);

        if (optionalInviteCodeData.isEmpty()) {
            throw new CustomException(INVITE_LINK_EXPIRED_OR_NOT_FOUND);
        }

        Map inviteCodeKeyData = optionalInviteCodeData.get();
        Object isUsedObject = inviteCodeKeyData.get("isUsed");
        Boolean isUsed = new ObjectMapper().convertValue(isUsedObject, new TypeReference<Boolean>() {});

        if (isUsed) {
            throw new CustomException(ALREADY_USED_INVITED_CODE);
        }
    }

    // 초대 코드로 회원가입했는지 체크
    @Transactional
    public CheckInviteCodeBySignUpResponse checkInviteCodeBySignUp(Long memberId) {
        String memberIdKey = memberId.toString();

        Optional<Map> optionalMemberIdKey = redisUtil.getData(RedisConstant.REDIS_INVITE_MEMBER_PREFIX, memberIdKey, Map.class);
        if (optionalMemberIdKey.isEmpty()) {
            return new CheckInviteCodeBySignUpResponse(CheckInviteCodeResponseType.NONE);
        }
        Map memberIdKeyData = optionalMemberIdKey.get();
        Object inviteCodeObject = memberIdKeyData.get("inviteCode");

        String inviteCode = new ObjectMapper().convertValue(inviteCodeObject, new TypeReference<String>() {});
        Optional<Map> optionalInviteCodeKeyData = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, Map.class);
        if (optionalInviteCodeKeyData.isEmpty()) {
            return new CheckInviteCodeBySignUpResponse(CheckInviteCodeResponseType.NONE);
        }

        Map inviteCodeKeyData = optionalInviteCodeKeyData.get();
        Object isUsedObject = inviteCodeKeyData.get("isUsed");
        Boolean isUsed = new ObjectMapper().convertValue(isUsedObject, new TypeReference<Boolean>() {});

        if (isUsed) {
            redisUtil.deleteData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode);
            return new CheckInviteCodeBySignUpResponse(CheckInviteCodeResponseType.READY);
        }

        return new CheckInviteCodeBySignUpResponse(CheckInviteCodeResponseType.NONE);
    }

    public GetInviteMemberResponse findInviteMember(String inviteCode) {
        Optional<Map> inviteCodeData = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, Map.class);

        if (inviteCodeData.isEmpty()) {
            throw new CustomException(INVITE_LINK_EXPIRED_OR_NOT_FOUND);
        }

        Map inviteCodeKeyData = inviteCodeData.get();
        Object inviteCodeCreatorIdObject = inviteCodeKeyData.get("inviteMemberId");

        Long inviteCodeCreatorId = new ObjectMapper().convertValue(inviteCodeCreatorIdObject, new TypeReference<>() {});

        Member member = memberRepository.findById(inviteCodeCreatorId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        return new GetInviteMemberResponse(member.getName());
    }
}
