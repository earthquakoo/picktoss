package com.picktoss.picktossserver.core.eventlistener.event.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class S3UploadFeedbackImageEvent {
    private List<MultipartFile> files;
    private List<String> s3Keys;
}
