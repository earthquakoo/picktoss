package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q WHERE q.bookmark = true")
    List<Quiz> findByBookmark();

    @Query("SELECT q FROM Quiz q WHERE q.document.id = :documentId ORDER BY q.deliveredCount ASC")
    List<Quiz> findByDocumentId(@Param("documentId") Long documentId);
}
