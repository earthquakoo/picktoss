package com.picktoss.picktossserver.domain.document.dto.request;

import com.picktoss.picktossserver.global.enums.document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class CreateDocumentRequest {

    private String star;
    private Boolean isPublic;
    private MultipartFile file;
    private DocumentType documentType;
}
