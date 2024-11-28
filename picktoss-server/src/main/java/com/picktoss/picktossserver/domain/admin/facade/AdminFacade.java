package com.picktoss.picktossserver.domain.admin.facade;

import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.domain.star.service.StarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFacade {

    private final DocumentService documentService;
    private final QuizService quizService;
    private final MemberService memberService;
    private final StarService starService;
}
