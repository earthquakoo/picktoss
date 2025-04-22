package com.picktoss.picktossserver.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class UpdateMemberImageRequest {
    private MultipartFile file;
}
