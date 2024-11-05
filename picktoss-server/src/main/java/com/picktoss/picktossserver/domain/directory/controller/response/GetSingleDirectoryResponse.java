package com.picktoss.picktossserver.domain.directory.controller.response;

import com.picktoss.picktossserver.global.enums.directory.DirectoryTag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetSingleDirectoryResponse {
    private Long id;
    private String name;
    private String emoji;
    private DirectoryTag tag;
}
