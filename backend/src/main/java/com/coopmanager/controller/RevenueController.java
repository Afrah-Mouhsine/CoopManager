package com.coopmanager.controller;

import com.coopmanager.dto.revenue.RevenueCalculationRequest;
import com.coopmanager.dto.revenue.RevenueDistributionResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
@Tag(name = "Revenue", description = "Revenue sharing calculation and distribution")
@SecurityRequirement(name = "bearerAuth")
public class RevenueController {

    private final RevenueService revenueService;

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "Calculate revenue distribution (or simulation)")
    public ResponseEntity<RevenueDistributionResponse> calculate(
            @Valid @RequestBody RevenueCalculationRequest request,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(revenueService.calculate(request, currentMember.getId()));
    }

    @PatchMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "Finalize a simulation distribution")
    public ResponseEntity<RevenueDistributionResponse> finalize(@PathVariable Long id) {
        return ResponseEntity.ok(revenueService.finalize(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "List all distributions")
    public ResponseEntity<List<RevenueDistributionResponse>> findAll() {
        return ResponseEntity.ok(revenueService.findAll());
    }

    @GetMapping("/finalized")
    @Operation(summary = "List finalized distributions (visible to all)")
    public ResponseEntity<List<RevenueDistributionResponse>> findFinalized() {
        return ResponseEntity.ok(revenueService.findFinalized());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get distribution details")
    public ResponseEntity<RevenueDistributionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(revenueService.findById(id));
    }

    @GetMapping("/{id}/my-share")
    @Operation(summary = "Get my personal share from a distribution")
    public ResponseEntity<RevenueDistributionResponse> myShare(
            @PathVariable Long id,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.ok(revenueService.findMyShare(id, currentMember.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
    @Operation(summary = "Delete a simulation (cannot delete finalized)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        revenueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
