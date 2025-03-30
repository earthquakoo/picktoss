package com.picktoss.picktossserver.domain.directory.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryDeleteService {

    private final DirectoryRepository directoryRepository;

    @Transactional
    public void deleteDirectory(Long memberId, Long directoryId) {
        List<Directory> directories = directoryRepository.findAllByMemberId(memberId);

        if (directories.size() == 1) {
            throw new CustomException(DIRECTORY_DELETE_NOT_ALLOWED);
        }

        Directory directory = directoryRepository.findByDirectoryIdAndMemberId(directoryId, memberId)
                .orElseThrow(() -> new CustomException(DIRECTORY_NOT_FOUND));

        if (!Objects.equals(directory.getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        directoryRepository.delete(directory);
    }
}
