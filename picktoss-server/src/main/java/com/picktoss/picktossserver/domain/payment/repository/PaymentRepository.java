package com.picktoss.picktossserver.domain.payment.repository;

import com.picktoss.picktossserver.domain.payment.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<TossPayment, Long> {
}
