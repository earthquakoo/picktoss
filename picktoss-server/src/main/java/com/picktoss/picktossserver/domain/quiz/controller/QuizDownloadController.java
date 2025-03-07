package com.picktoss.picktossserver.domain.quiz.controller;


import com.lowagie.text.DocumentException;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.pdfgenerator.PdfGenerator;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizDownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizDownloadController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PdfGenerator pdfGenerator;
    private final QuizDownloadService quizDownloadService;

    @Operation(summary = "퀴즈 다운로드")
    @GetMapping("/documents/{document_id}/download-quiz")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadQuizzes(@PathVariable("document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        try {
            List<Quiz> quizzes = quizDownloadService.findAllByDocumentIdAndMemberId(documentId, memberId);
            byte[] pdfBytes = pdfGenerator.generateQuizPdf(quizzes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "quizzes.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
