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
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.global.enums.auth.CheckInviteCodeResponseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.INVITE_LINK_EXPIRED_OR_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthInviteLinkService {

    private final RedisUtil redisUtil;
    private final StarHistoryRepository starHistoryRepository;
    private final MemberRepository memberRepository;
    private final AuthUtil authUtil;

    public String createInviteLink(Long memberId) {
        String initLink = "https://www.picktoss.com/invite/";

        String memberIdKey = memberId.toString();

        // 기존 초대 코드 조회
        Optional<Map> existingCode = redisUtil.getData(RedisConstant.REDIS_INVITE_MEMBER_PREFIX, memberIdKey, Map.class);
        if (existingCode.isPresent()) {
            Map map = existingCode.get();
            String inviteCode = map.get("inviteCode").toString();
            return initLink + inviteCode;
        }

        List<Long> inviteMemberIdList = new ArrayList<>();

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
                "invitedMemberIdList", inviteMemberIdList,
                "createdAt", createdAt,
                "expiresAt", createdAt.plusDays(3)
        );

        redisUtil.setData(RedisConstant.REDIS_INVITE_MEMBER_PREFIX, memberIdKey, memberIdKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, uniqueCode, inviteCodeKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);

        return inviteLink;
    }

    // 초대 코드 인증
    public void verifyInviteCode(String inviteCode, Long memberId) {
        Optional<Map> inviteCodeData = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, Map.class);

        if (inviteCodeData.isEmpty()) {
            throw new CustomException(INVITE_LINK_EXPIRED_OR_NOT_FOUND);
        }

        Map inviteCodeKeyData = inviteCodeData.get();
        Object inviteMemberIdListObject = inviteCodeKeyData.get("invitedMemberIdList");

        List<Long> inviteMemberIdList = new ObjectMapper().convertValue(inviteMemberIdListObject, new TypeReference<List<Long>>() {});
        inviteMemberIdList.add(memberId);

        inviteCodeKeyData.put("invitedMemberIdList", inviteMemberIdList);
        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, inviteCodeKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
    }

    // 초대 코드로 회원가입했는지 체크
    @Transactional
    public CheckInviteCodeBySignUpResponse checkInviteCodeBySignUp(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Star star = member.getStar();
        StarHistory starHistory = star.depositStarByInviteFriendReward(star);
        starHistoryRepository.save(starHistory);

        String memberIdKey = memberId.toString();

        Optional<Map> memberIdKeyObject = redisUtil.getData(RedisConstant.REDIS_INVITE_MEMBER_PREFIX, memberIdKey, Map.class);
        if (memberIdKeyObject.isEmpty()) {
            return new CheckInviteCodeBySignUpResponse(CheckInviteCodeResponseType.NONE);
        }
        Map memberIdKeyData = memberIdKeyObject.get();
        Object inviteCodeObject = memberIdKeyData.get("inviteCode");

        String inviteCode = new ObjectMapper().convertValue(inviteCodeObject, new TypeReference<String>() {});
        Optional<Map> inviteCodeKeyDataObject = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, Map.class);
        Map inviteCodeKeyData = inviteCodeKeyDataObject.get();
        Object invitedMemberIdListObject = inviteCodeKeyData.get("invitedMemberIdList");

        List<Long> inviteMemberIdList = new ObjectMapper().convertValue(invitedMemberIdListObject, new TypeReference<List<Long>>() {});
        for (Long invitedMemberId : inviteMemberIdList) {
            if (invitedMemberId == memberId) {
                inviteMemberIdList = inviteMemberIdList.stream()
                        .filter(item -> !item.equals(memberId))
                        .collect(Collectors.toList());
            }
        }

        inviteCodeKeyData.put("invitedMemberIdList", inviteMemberIdList);
        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, inviteCodeKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
        return new CheckInviteCodeBySignUpResponse(CheckInviteCodeResponseType.READY);
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
