package com.picktoss.picktossserver.domain.quiz.controller.mapper;

import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;

import java.util.ArrayList;
import java.util.List;

public class QuizMapper {

    public static QuizResponseDto quizzesToQuizResponseDto(List<Quiz> quizzes) {

        List<QuizResponseDto.QuizDto> quizDtos = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            Document document = quiz.getDocument();
            Directory directory = document.getDirectory();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                List<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            QuizResponseDto.DocumentDto documentDto = QuizResponseDto.DocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            QuizResponseDto.CategoryDto categoryDto = QuizResponseDto.CategoryDto.builder()
                    .id(directory.getId())
                    .name(directory.getName())
                    .build();

            QuizResponseDto.QuizDto quizDto = QuizResponseDto.QuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .document(documentDto)
                    .category(categoryDto)
                    .build();

            quizDtos.add(quizDto);
        }

        return new QuizResponseDto(quizDtos);
    }
}
