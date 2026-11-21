package com.coopmanager.service;

import com.coopmanager.dto.member.MemberRequest;
import com.coopmanager.dto.member.MemberResponse;
import com.coopmanager.dto.member.UpdateMemberRequest;
import com.coopmanager.entity.Member;
import com.coopmanager.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse create(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        Member member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .cin(request.getCin())
                .cultureType(request.getCultureType())
                .joinDate(request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now())
                .role(request.getRole())
                .active(true)
                .build();
        return toResponse(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findActive() {
        return memberRepository.findByActiveTrueOrderByLastNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public MemberResponse update(Long id, UpdateMemberRequest request) {
        Member member = getOrThrow(id);

        if (request.getFirstName() != null) member.setFirstName(request.getFirstName());
        if (request.getLastName() != null) member.setLastName(request.getLastName());
        if (request.getEmail() != null && !request.getEmail().equals(member.getEmail())) {
            if (memberRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            member.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        if (request.getAddress() != null) member.setAddress(request.getAddress());
        if (request.getCin() != null) member.setCin(request.getCin());
        if (request.getCultureType() != null) member.setCultureType(request.getCultureType());
        if (request.getJoinDate() != null) member.setJoinDate(request.getJoinDate());
        if (request.getRole() != null) member.setRole(request.getRole());
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void deactivate(Long id) {
        Member member = getOrThrow(id);
        member.setActive(false);
        member.setLeaveDate(LocalDate.now());
        memberRepository.save(member);
    }

    @Transactional
    public void reactivate(Long id) {
        Member member = getOrThrow(id);
        member.setActive(true);
        member.setLeaveDate(null);
        memberRepository.save(member);
    }

    private Member getOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + id));
    }

    public MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .address(member.getAddress())
                .cin(member.getCin())
                .cultureType(member.getCultureType())
                .joinDate(member.getJoinDate())
                .leaveDate(member.getLeaveDate())
                .role(member.getRole())
                .active(member.isActive())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
