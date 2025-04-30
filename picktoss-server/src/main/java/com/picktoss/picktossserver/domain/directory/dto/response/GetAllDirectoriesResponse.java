package com.picktoss.picktossserver.domain.directory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllDirectoriesResponse {

    private List<GetAllDirectoriesDirectoryDto> directories;
    @Getter
    @Builder
    public static class GetAllDirectoriesDirectoryDto {
        private Long id;
        private String name;
        private String emoji;
        private int documentCount;
    }
}
