package com.picktoss.picktossserver.domain.member.repository;

import com.picktoss.picktossserver.domain.member.entity.WithdrawalReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalReasonRepository extends JpaRepository<WithdrawalReason, Long> {
}
