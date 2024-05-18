package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q WHERE q.bookmark = true")
    List<Quiz> findByBookmark();

    @Query("SELECT q FROM Quiz q WHERE q.document.id = :documentId AND q.quizType = :quizType ORDER BY q.deliveredCount ASC")
    List<Quiz> findByDocumentIdAndQuizType(
            @Param("documentId") Long documentId,
            @Param("quizType") QuizType quizType
    );

    @Query("SELECT q FROM Quiz q WHERE q.document.id = :documentId AND q.latest = true")
    List<Quiz> findByDocumentIdAndLatestIs(@Param("documentId") Long documentId);
}
