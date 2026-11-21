package com.coopmanager.controller;

import com.coopmanager.dto.production.ProductionDashboardDTO;
import com.coopmanager.dto.production.ProductionRequest;
import com.coopmanager.dto.production.ProductionResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.service.ProductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@Tag(name = "Production", description = "Production tracking and validation")
@SecurityRequirement(name = "bearerAuth")
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER','MEMBER')")
    @Operation(summary = "Record a production entry")
    public ResponseEntity<ProductionResponse> create(
            @Valid @RequestBody ProductionRequest request,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productionService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all production entries (filterable by date range)")
    public ResponseEntity<List<ProductionResponse>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(productionService.findByDateRange(from, to));
        }
        return ResponseEntity.ok(productionService.findAll());
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get production entries for a specific member")
    public ResponseEntity<List<ProductionResponse>> findByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(productionService.findByMember(memberId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "List pending entries awaiting validation")
    public ResponseEntity<List<ProductionResponse>> findPending() {
        return ResponseEntity.ok(productionService.findPending());
    }

    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "Validate a production entry")
    public ResponseEntity<ProductionResponse> validate(
            @PathVariable Long id,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.ok(productionService.validate(id, currentMember.getId()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "Reject a production entry")
    public ResponseEntity<ProductionResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.ok(productionService.reject(id, currentMember.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "Delete a pending production entry")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get production dashboard data for a given year")
    public ResponseEntity<ProductionDashboardDTO> getDashboard(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        return ResponseEntity.ok(productionService.getDashboard(year));
    }
}
