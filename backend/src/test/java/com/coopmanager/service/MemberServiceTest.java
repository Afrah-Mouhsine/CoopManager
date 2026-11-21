package com.coopmanager.service;

import com.coopmanager.dto.member.MemberRequest;
import com.coopmanager.dto.member.MemberResponse;
import com.coopmanager.dto.member.UpdateMemberRequest;
import com.coopmanager.entity.Member;
import com.coopmanager.entity.Role;
import com.coopmanager.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks MemberService memberService;

    private Member sampleMember;

    @BeforeEach
    void setUp() {
        sampleMember = Member.builder()
                .id(1L)
                .firstName("Ahmed")
                .lastName("Benali")
                .email("ahmed@coop.ma")
                .password("encoded_pass")
                .cultureType("Olive")
                .joinDate(LocalDate.now())
                .role(Role.MEMBER)
                .active(true)
                .build();
    }

    @Test
    void create_shouldPersistAndReturnMember() {
        MemberRequest req = new MemberRequest();
        req.setFirstName("Ahmed");
        req.setLastName("Benali");
        req.setEmail("ahmed@coop.ma");
        req.setPassword("pass1234");
        req.setCultureType("Olive");
        req.setRole(Role.MEMBER);

        when(memberRepository.existsByEmail("ahmed@coop.ma")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded_pass");
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        MemberResponse response = memberService.create(req);

        assertThat(response.getEmail()).isEqualTo("ahmed@coop.ma");
        assertThat(response.getRole()).isEqualTo(Role.MEMBER);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void create_shouldThrowWhenEmailExists() {
        MemberRequest req = new MemberRequest();
        req.setEmail("ahmed@coop.ma");
        req.setPassword("pass1234");
        req.setFirstName("A");
        req.setLastName("B");
        req.setCultureType("Olive");
        req.setRole(Role.MEMBER);

        when(memberRepository.existsByEmail("ahmed@coop.ma")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void findAll_shouldReturnAllMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(sampleMember));

        List<MemberResponse> result = memberService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Ahmed");
    }

    @Test
    void findById_shouldReturnMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));

        MemberResponse result = memberService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deactivate_shouldSetActiveToFalseAndSetLeaveDate() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        memberService.deactivate(1L);

        assertThat(sampleMember.isActive()).isFalse();
        assertThat(sampleMember.getLeaveDate()).isNotNull();
        verify(memberRepository).save(sampleMember);
    }

    @Test
    void reactivate_shouldSetActiveToTrueAndClearLeaveDate() {
        sampleMember.setActive(false);
        sampleMember.setLeaveDate(LocalDate.now().minusDays(1));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        memberService.reactivate(1L);

        assertThat(sampleMember.isActive()).isTrue();
        assertThat(sampleMember.getLeaveDate()).isNull();
    }

    @Test
    void update_shouldUpdateOnlyProvidedFields() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any())).thenReturn(sampleMember);

        UpdateMemberRequest req = new UpdateMemberRequest();
        req.setPhone("0612345678");

        MemberResponse result = memberService.update(1L, req);

        assertThat(sampleMember.getPhone()).isEqualTo("0612345678");
        assertThat(result).isNotNull();
    }
}
