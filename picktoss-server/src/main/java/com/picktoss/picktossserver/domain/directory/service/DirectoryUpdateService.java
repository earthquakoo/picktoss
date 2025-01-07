package com.picktoss.picktossserver.domain.directory.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DIRECTORY_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.UNAUTHORIZED_OPERATION_EXCEPTION;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryUpdateService {

    private final DirectoryRepository directoryRepository;

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
}
