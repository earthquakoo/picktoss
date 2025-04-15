package com.picktoss.picktossserver.domain.category.repository;

import com.picktoss.picktossserver.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
