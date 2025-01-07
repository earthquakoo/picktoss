package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetSingleQuizRecordByDateResponse {

    private int currentConsecutiveDays;
    private int maxConsecutiveDays;
    private List<GetQuizRecordsResponse.GetQuizRecordsDto> quizRecords;
}
