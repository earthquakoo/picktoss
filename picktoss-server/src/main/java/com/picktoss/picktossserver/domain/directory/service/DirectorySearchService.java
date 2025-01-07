package com.picktoss.picktossserver.domain.directory.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.dto.response.GetAllDirectoriesResponse;
import com.picktoss.picktossserver.domain.directory.dto.response.GetSingleDirectoryResponse;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DIRECTORY_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorySearchService {

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
}
