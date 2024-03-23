package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE d.category.id = :categoryId")
    List<Document> findAllByCategoryId(@Param("categoryId") Long categoryId);

    Optional<Document> findByCategoryAndId(Category category, Long DocumentId);

}
