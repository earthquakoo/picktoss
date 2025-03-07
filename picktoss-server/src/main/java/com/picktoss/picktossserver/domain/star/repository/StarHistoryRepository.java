package com.picktoss.picktossserver.domain.star.repository;

import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StarHistoryRepository extends JpaRepository<StarHistory, Long> {
}
