package com.picktoss.picktossserver.domain.directory.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.controller.response.GetAllDirectoriesResponse;
import com.picktoss.picktossserver.domain.directory.controller.response.GetSingleDirectoryResponse;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DIRECTORY_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.UNAUTHORIZED_OPERATION_EXCEPTION;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryService {

    private final DirectoryRepository directoryRepository;

    public List<GetAllDirectoriesResponse.GetAllDirectoriesDirectoryDto> findAllDirectories(Long memberId) {
        List<Directory> directories = directoryRepository.findAllByMemberId(memberId);
        List<GetAllDirectoriesResponse.GetAllDirectoriesDirectoryDto> directoryDtos = new ArrayList<>();

        for (Directory directory : directories) {
            GetAllDirectoriesResponse.GetAllDirectoriesDirectoryDto directoryDto = GetAllDirectoriesResponse.GetAllDirectoriesDirectoryDto.builder()
                    .id(directory.getId())
                    .name(directory.getName())
                    .tag(directory.getTag())
                    .emoji(directory.getEmoji())
                    .documentCount(directory.getDocuments().size())
                    .build();

            directoryDtos.add(directoryDto);
        }
        return directoryDtos;
    }

    public GetSingleDirectoryResponse findSingleDirectory(Long directoryId, Long memberId) {
        Directory directory = directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));

        return GetSingleDirectoryResponse.builder()
                .id(directory.getId())
                .name(directory.getName())
                .tag(directory.getTag())
                .emoji(directory.getEmoji())
                .build();
    }

    @Transactional
    public Long createDirectory(String name, Member member, String emoji) {
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

    @Transactional
    public void deleteDirectory(Long memberId, Long directoryId) {
        Directory directory = directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));

        if (!Objects.equals(directory.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        directoryRepository.delete(directory);
    }

    @Transactional
    public void updateDirectoryInfo(Long memberId, Long directoryId, String name, String emoji) {
        Directory directory = directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));

        if (!Objects.equals(directory.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        directory.updateDirectoryEmoji(emoji);
        directory.updateDirectoryName(name);
    }

    public Directory findByDirectoryIdAndMemberId(Long directoryId, Long memberId) {
        return directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));
    }

    public Directory findDirectoryWithMemberAndStarAndStarHistoryByDirectoryIdAndMemberId(Long directoryId, Long memberId) {
        return directoryRepository.findDirectoryWithMemberAndStarAndStarHistoryByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));
    }
}
