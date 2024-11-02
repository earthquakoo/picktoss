package com.picktoss.picktossserver.core.event.event.sqs;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SQSMessageEvent {

    private Long memberId;
    private String s3Key;
    private Long documentId;
    private QuizType quizType;
    private Integer quizCount;
}
