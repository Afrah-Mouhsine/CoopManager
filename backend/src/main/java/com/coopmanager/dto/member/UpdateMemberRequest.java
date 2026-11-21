package com.coopmanager.dto.member;

import com.coopmanager.entity.Role;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateMemberRequest {
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String phone;
    private String address;
    private String cin;
    private String cultureType;
    private LocalDate joinDate;
    private Role role;
    private String newPassword;
}
