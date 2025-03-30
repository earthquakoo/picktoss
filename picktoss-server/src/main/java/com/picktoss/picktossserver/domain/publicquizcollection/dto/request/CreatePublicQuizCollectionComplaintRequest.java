package com.picktoss.picktossserver.domain.publicquizcollection.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreatePublicQuizCollectionComplaintRequest {
    private List<MultipartFile> files;
    private String content;
}
