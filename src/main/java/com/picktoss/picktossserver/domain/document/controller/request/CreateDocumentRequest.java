package com.picktoss.picktossserver.domain.document.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class CreateDocumentRequest {

    private MultipartFile file;
    private String userDocumentName;
    private String categoryId;
}
