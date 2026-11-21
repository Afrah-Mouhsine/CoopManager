package com.coopmanager.dto.member;

import com.coopmanager.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String cin;
    private String cultureType;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
