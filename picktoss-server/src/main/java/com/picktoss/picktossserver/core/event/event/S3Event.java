package com.picktoss.picktossserver.core.event.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@RequiredArgsConstructor
public class S3Event {

    private final MultipartFile file;
    private final String s3Key;
}
