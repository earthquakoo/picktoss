package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionComplaintRepository extends JpaRepository<CollectionComplaint, Long> {
}
