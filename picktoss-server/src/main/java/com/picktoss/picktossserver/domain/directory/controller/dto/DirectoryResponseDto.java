package com.picktoss.picktossserver.domain.directory.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DirectoryResponseDto {

    private Long id;
    private String name;
}
