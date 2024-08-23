package com.picktoss.picktossserver.domain.member.repository;

import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByClientId(String id);

    Optional<Member> findByEmail(String email);
}
