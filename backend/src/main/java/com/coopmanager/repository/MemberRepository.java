package com.coopmanager.repository;

import com.coopmanager.entity.Member;
import com.coopmanager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Member> findByActiveTrue();

    List<Member> findByRole(Role role);

    List<Member> findByActiveTrueOrderByLastNameAsc();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.active = true")
    long countActiveMembers();
}
