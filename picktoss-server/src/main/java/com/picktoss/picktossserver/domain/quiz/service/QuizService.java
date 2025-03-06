package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.eventlistener.event.email.EmailSenderEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.email.EmailSenderPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final JdbcTemplate jdbcTemplate;
    private final EmailSenderPublisher emailSenderPublisher;
    private final MemberRepository memberRepository;
    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;

    @Transactional
    public void quizChunkBatchInsert(
            List<Quiz> quizzes, List<QuizSet> quizSets, List<QuizSetQuiz> quizSetQuizzes, List<Member> members) {
        String insertQuizSetSql = "INSERT INTO quiz_set (id, name, solved, quiz_set_type, member_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSet quizSet = quizSets.get(i);
                        ps.setString(1, quizSet.getId());
                        ps.setString(2, quizSet.getName());
                        ps.setBoolean(3, quizSet.isSolved());
                        ps.setString(4, QuizSetType.TODAY_QUIZ_SET.name());
                        ps.setLong(5, quizSet.getMember().getId());
                        ps.setObject(6, LocalDateTime.now());
                        ps.setObject(7, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizSets.size();
                    }
                }
        );

        String insertQuizSetQuizzesSql = "INSERT INTO quiz_set_quiz (quiz_id, quiz_set_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetQuizzesSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSetQuiz quizSetQuiz = quizSetQuizzes.get(i);
                        ps.setObject(1, quizSetQuiz.getQuiz().getId());
                        ps.setObject(2, quizSetQuiz.getQuizSet().getId());
                        ps.setObject(3, LocalDateTime.now());
                        ps.setObject(4, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizSetQuizzes.size();
                    }
                }
        );

        String updateQuizSql = "UPDATE quiz SET delivered_count = delivered_count + 1 WHERE id = ?";
        jdbcTemplate.batchUpdate(
                updateQuizSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Quiz quiz = quizzes.get(i);
                        ps.setObject(1, quiz.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizzes.size();
                    }
                }
        );

        emailSenderPublisher.emailSenderPublisher(new EmailSenderEvent(members));
    }

    @Transactional
    public String createTodayQuizSetForTest(Long memberId) {
        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Integer todayQuizCount = member.getTodayQuizCount();

        List<Quiz> quizzesBySortedDeliveredCount = new ArrayList<>();
        Set<Directory> directories = member.getDirectories();
        for (Directory directory : directories) {
            if (directory.getDocuments() == null) {
                continue;
            }
            Set<Document> documents = directory.getDocuments();
            for (Document document : documents) {
                if (document.getQuizzes() == null) {
                    continue;
                }
                Set<Quiz> quizzes = document.getQuizzes();
                if (quizzes.isEmpty()) {
                    continue;
                }
                // quiz.deliveredCount 순으로 정렬 or List로 정렬
                List<Quiz> quizList = quizzes.stream().sorted((e1, e2) -> e1.getDeliveredCount()).limit(todayQuizCount).toList();
                quizzesBySortedDeliveredCount.addAll(quizList);
            }
        }
        String quizSetId = UUID.randomUUID().toString().replace("-", "");
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, "오늘의 퀴즈 세트", QuizSetType.TODAY_QUIZ_SET, member);

        quizzesBySortedDeliveredCount.stream().sorted((e1, e2) -> e1.getDeliveredCount());
        int quizCount = 0;

        for (Quiz quiz : quizzesBySortedDeliveredCount) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
            quizCount += 1;
            if (quizCount == todayQuizCount) {
                break;
            }
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);
        return quizSetId;
    }
}
