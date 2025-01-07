package com.picktoss.picktossserver.domain.collection.dto.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class CreateCollectionComplaintRequest {
    private List<MultipartFile> files;
    private String content;
}
