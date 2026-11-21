package com.coopmanager.dto.member;

import com.coopmanager.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phone;
    private String address;
    private String cin;

    @NotBlank
    private String cultureType;

    private LocalDate joinDate;

    @NotNull
    private Role role;
}
