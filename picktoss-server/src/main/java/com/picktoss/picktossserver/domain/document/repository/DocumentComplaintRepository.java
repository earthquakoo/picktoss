package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.DocumentComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentComplaintRepository extends JpaRepository<DocumentComplaint, Long> {
}
