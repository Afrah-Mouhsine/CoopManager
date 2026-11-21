package com.coopmanager.dto.auth;

import com.coopmanager.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long memberId;
    private String fullName;
    private String email;
    private Role role;
}
