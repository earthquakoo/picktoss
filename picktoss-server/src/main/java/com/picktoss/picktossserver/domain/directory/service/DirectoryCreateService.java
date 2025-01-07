package com.picktoss.picktossserver.domain.directory.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryCreateService {

    private final DirectoryRepository directoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createDirectory(String name, String emoji, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Directory directory = Directory.createDirectory(member, name, emoji);
        directoryRepository.save(directory);
        return directory.getId();
    }

    @Transactional
    public Directory createDefaultDirectory(Member member) {
        Directory directory = Directory.createDefaultDirectory(member);
        directoryRepository.save(directory);
        return directory;
    }
}
