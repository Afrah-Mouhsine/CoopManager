package com.coopmanager.controller;

import com.coopmanager.dto.member.MemberRequest;
import com.coopmanager.dto.member.MemberResponse;
import com.coopmanager.dto.member.UpdateMemberRequest;
import com.coopmanager.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member registry management")
@SecurityRequirement(name = "bearerAuth")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Create a new member (President only)")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all members")
    public ResponseEntity<List<MemberResponse>> findAll(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(includeInactive ? memberService.findAll() : memberService.findActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    public ResponseEntity<MemberResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Update member details (President only)")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateMemberRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Deactivate member (soft delete, President only)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        memberService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Reactivate a deactivated member (President only)")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        memberService.reactivate(id);
        return ResponseEntity.noContent().build();
    }
}
