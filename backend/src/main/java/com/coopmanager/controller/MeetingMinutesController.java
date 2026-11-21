package com.coopmanager.controller;

import com.coopmanager.entity.Member;
import com.coopmanager.service.MeetingMinutesService;
import com.coopmanager.service.MeetingMinutesService.MinutesRequest;
import com.coopmanager.service.MeetingMinutesService.MinutesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/minutes")
@RequiredArgsConstructor
@Tag(name = "Meeting Minutes", description = "Assembly meeting PV management")
@SecurityRequirement(name = "bearerAuth")
public class MeetingMinutesController {

    private final MeetingMinutesService minutesService;

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Create meeting minutes draft")
    public ResponseEntity<MinutesResponse> create(
            @RequestBody MinutesRequest request,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(minutesService.create(request, currentMember.getId()));
    }

    @GetMapping
    @Operation(summary = "List meeting minutes (members see only published)")
    public ResponseEntity<List<MinutesResponse>> findAll(
            @AuthenticationPrincipal Member currentMember) {
        boolean publishedOnly = currentMember.getRole().name().equals("MEMBER");
        return ResponseEntity.ok(minutesService.findAll(publishedOnly));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meeting minutes by ID")
    public ResponseEntity<MinutesResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(minutesService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Update unpublished minutes")
    public ResponseEntity<MinutesResponse> update(
            @PathVariable Long id,
            @RequestBody MinutesRequest request) {
        return ResponseEntity.ok(minutesService.update(id, request));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Publish meeting minutes (makes visible to all members)")
    public ResponseEntity<MinutesResponse> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.ok(minutesService.publish(id, currentMember.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    @Operation(summary = "Delete unpublished minutes")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        minutesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
