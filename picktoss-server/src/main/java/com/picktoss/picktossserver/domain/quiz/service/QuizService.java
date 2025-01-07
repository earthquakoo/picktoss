package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.eventlistener.event.email.EmailSenderEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.email.EmailSenderPublisher;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final JdbcTemplate jdbcTemplate;
    private final EmailSenderPublisher emailSenderPublisher;

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
                        ps.setObject(4, quizSet.getQuizSetType());
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
}
