package com.coopmanager.service;

import com.coopmanager.dto.auth.AuthResponse;
import com.coopmanager.dto.auth.LoginRequest;
import com.coopmanager.entity.Member;
import com.coopmanager.repository.MemberRepository;
import com.coopmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        String accessToken = jwtService.generateToken(member);
        String refreshToken = jwtService.generateRefreshToken(member);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }

    public AuthResponse refresh(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        if (!jwtService.isTokenValid(refreshToken, member)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String newAccessToken = jwtService.generateToken(member);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .memberId(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}
