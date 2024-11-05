package com.picktoss.picktossserver.domain.directory.facade;

import com.picktoss.picktossserver.domain.directory.controller.response.GetAllDirectoriesResponse;
import com.picktoss.picktossserver.domain.directory.controller.response.GetSingleDirectoryResponse;
import com.picktoss.picktossserver.domain.directory.service.DirectoryService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DirectoryFacade {

    private final DirectoryService directoryService;
    private final MemberService memberService;


    public List<GetAllDirectoriesResponse.GetAllDirectoriesDirectoryDto> findAllDirectories(Long memberId) {
        return directoryService.findAllDirectories(memberId);
    }

    public GetSingleDirectoryResponse findSingleDirectory(Long directoryId, Long memberId) {
        return directoryService.findSingleDirectory(directoryId, memberId);
    }

    @Transactional
    public Long createDirectory(Long memberId, String name, String emoji) {
        Member member = memberService.findMemberById(memberId);
        return directoryService.createDirectory(name, memberId, member, emoji);
    }

    @Transactional
    public void deleteDirectory(Long memberId, Long directoryId) {
        directoryService.deleteDirectory(memberId, directoryId);
    }

    @Transactional
    public void updateDirectoryInfo(Long memberId, Long directoryId, String name, String emoji) {
        directoryService.updateDirectoryInfo(memberId, directoryId, name, emoji);
    }
}
