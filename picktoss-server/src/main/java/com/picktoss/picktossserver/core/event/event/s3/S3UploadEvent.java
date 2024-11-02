package com.picktoss.picktossserver.core.event.event.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class S3UploadEvent {

    private MultipartFile file;
    private String s3Key;
}
