package com.picktoss.picktossserver.domain.document.dto.request;

import com.picktoss.picktossserver.global.enums.document.DocumentType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class CreateDocumentRequest {

    private String star;
    private String emoji;
    private Long categoryId;
    private Boolean isPublic;
    private QuizType quizType;
    private MultipartFile file;
    private DocumentType documentType;
}
