package com.picktoss.picktossserver.domain.document.controller.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class CreateDocumentRequest {

    private MultipartFile file;
    private String directoryId;
    private String documentName;
    private String star;
    private QuizType quizType;
}
