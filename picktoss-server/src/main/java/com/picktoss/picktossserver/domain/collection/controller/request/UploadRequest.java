package com.picktoss.picktossserver.domain.collection.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class UploadRequest {
    private MultipartFile file;
}
