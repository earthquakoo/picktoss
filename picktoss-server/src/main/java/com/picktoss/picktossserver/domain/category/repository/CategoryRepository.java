package com.picktoss.picktossserver.domain.category.repository;

import com.picktoss.picktossserver.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c " +
            "WHERE c.language = :language")
    List<Category> findAllByLanguage(
            @Param("language") String language
    );
}
