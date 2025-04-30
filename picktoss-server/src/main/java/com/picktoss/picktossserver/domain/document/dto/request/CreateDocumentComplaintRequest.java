package com.picktoss.picktossserver.domain.document.dto.request;

import com.picktoss.picktossserver.global.enums.document.ComplaintReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateDocumentComplaintRequest {
    private List<MultipartFile> files;
    private String content;
    private ComplaintReason complaintReason;
}
