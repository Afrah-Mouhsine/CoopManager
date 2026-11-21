package com.coopmanager.controller;

import com.coopmanager.dto.finance.FinancialSummaryDTO;
import com.coopmanager.dto.finance.TransactionRequest;
import com.coopmanager.dto.finance.TransactionResponse;
import com.coopmanager.entity.Member;
import com.coopmanager.service.FinancialService;
import com.coopmanager.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "Financial cashbook and reporting")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('PRESIDENT','TREASURER')")
public class FinancialController {

    private final FinancialService financialService;
    private final ReportExportService exportService;

    @PostMapping("/transactions")
    @Operation(summary = "Record a financial transaction")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal Member currentMember) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financialService.create(request, currentMember.getId()));
    }

    @GetMapping("/transactions")
    @Operation(summary = "List transactions with optional date filter")
    public ResponseEntity<List<TransactionResponse>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(financialService.findAll(from, to));
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get a transaction by ID")
    public ResponseEntity<TransactionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(financialService.findById(id));
    }

    @PutMapping("/transactions/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(financialService.update(id, request));
    }

    @DeleteMapping("/transactions/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        financialService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get annual financial summary with monthly trend")
    public ResponseEntity<FinancialSummaryDTO> getSummary(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        return ResponseEntity.ok(financialService.getSummary(year));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export transactions to Excel file")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws IOException {

        byte[] excelData = exportService.exportTransactionsToExcel(from, to);
        String filename = "rapport-financier-" + from.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + to.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }
}
