package com.picktoss.picktosssendemailbatch.core.config.dto;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ItemWriterDto {
    private List<QuizSetQuiz> quizSetQuizzes;
    private QuizSet quizSet;
    private Member member;
}
