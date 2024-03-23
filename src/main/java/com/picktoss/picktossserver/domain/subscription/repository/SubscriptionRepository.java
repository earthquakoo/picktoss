package com.picktoss.picktossserver.domain.subscription.repository;

import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.member.id = :memberId")
    List<Subscription> findAllByMemberId(@Param("memberId") String memberId);
}
