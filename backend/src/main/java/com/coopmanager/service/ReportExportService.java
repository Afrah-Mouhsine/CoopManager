package com.coopmanager.service;

import com.coopmanager.dto.finance.TransactionResponse;
import com.coopmanager.entity.FinancialTransaction;
import com.coopmanager.repository.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final FinancialTransactionRepository transactionRepo;
    private final FinancialService financialService;

    public byte[] exportTransactionsToExcel(LocalDate from, LocalDate to) throws IOException {
        List<FinancialTransaction> transactions =
                transactionRepo.findByTransactionDateBetweenOrderByTransactionDateDesc(from, to);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            titleCell.setCellValue("Rapport Financier — " + from.format(fmt) + " au " + to.format(fmt));
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Column headers
            String[] headers = {"Date", "Type", "Catégorie", "Description", "Référence", "Montant (MAD)"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 3;
            double totalIncome = 0;
            double totalExpense = 0;

            for (FinancialTransaction tx : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tx.getTransactionDate().format(fmt));
                row.createCell(1).setCellValue(tx.getType().name());
                row.createCell(2).setCellValue(tx.getCategory().name());
                row.createCell(3).setCellValue(tx.getDescription());
                row.createCell(4).setCellValue(tx.getReference() != null ? tx.getReference() : "");

                Cell amountCell = row.createCell(5);
                double amount = tx.getAmount().doubleValue();
                amountCell.setCellValue(amount);
                if (tx.getType() == FinancialTransaction.TransactionType.INCOME) {
                    totalIncome += amount;
                } else {
                    totalExpense += amount;
                }

                // Color income green, expense red
                CellStyle amountStyle = workbook.createCellStyle();
                Font amountFont = workbook.createFont();
                if (tx.getType() == FinancialTransaction.TransactionType.INCOME) {
                    amountFont.setColor(IndexedColors.DARK_GREEN.getIndex());
                } else {
                    amountFont.setColor(IndexedColors.RED.getIndex());
                }
                amountStyle.setFont(amountFont);
                amountCell.setCellStyle(amountStyle);
            }

            // Summary rows
            rowNum++;
            Row sumHeader = sheet.createRow(rowNum++);
            CellStyle sumStyle = workbook.createCellStyle();
            Font sumFont = workbook.createFont();
            sumFont.setBold(true);
            sumStyle.setFont(sumFont);

            Cell incLabel = sumHeader.createCell(4);
            incLabel.setCellValue("Total Revenus:");
            incLabel.setCellStyle(sumStyle);
            sumHeader.createCell(5).setCellValue(totalIncome);

            Row sumRow = sheet.createRow(rowNum++);
            Cell expLabel = sumRow.createCell(4);
            expLabel.setCellValue("Total Dépenses:");
            expLabel.setCellStyle(sumStyle);
            sumRow.createCell(5).setCellValue(totalExpense);

            Row netRow = sheet.createRow(rowNum);
            Cell netLabel = netRow.createCell(4);
            netLabel.setCellValue("Solde Net:");
            netLabel.setCellStyle(sumStyle);
            netRow.createCell(5).setCellValue(totalIncome - totalExpense);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
