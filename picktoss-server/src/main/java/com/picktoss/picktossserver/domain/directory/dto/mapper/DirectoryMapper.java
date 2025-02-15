package com.picktoss.picktossserver.domain.directory.dto.mapper;


import com.picktoss.picktossserver.domain.directory.entity.Directory;

import java.util.ArrayList;
import java.util.List;

public class DirectoryMapper {

    public static List<DirectoryResponseDto> mapDirectoriesToDirectoryDtos(List<Directory> directories) {

        if (directories.isEmpty())
            return new ArrayList<>();

        List<DirectoryResponseDto> directoryDtos = new ArrayList<>();

        for (Directory directory : directories) {
            DirectoryResponseDto directoryResponseDto = DirectoryResponseDto.builder()
                    .id(directory.getId())
                    .name(directory.getName())
                    .build();

            directoryDtos.add(directoryResponseDto);
        }

        return directoryDtos;
    }
}
