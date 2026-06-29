package com.training.week1.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    // ══════════════════════════════════════════════════════════
    //  generateExcelReport()
    //  Builds a basic Excel report and returns it as byte[]
    // ══════════════════════════════════════════════════════════
    public byte[] generateExcelReport() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Report");

            // ── Header Row ──
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Department", "Salary", "Status"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Sample Data Rows ──
            Object[][] data = {
                {1, "Alice",   "Engineering", 75000, "Active"},
                {2, "Bob",     "Marketing",   62000, "Active"},
                {3, "Charlie", "HR",          55000, "Inactive"},
                {4, "Diana",   "Engineering", 80000, "Active"},
            };

            int rowNum = 1;
            for (Object[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    if (rowData[i] instanceof Integer) {
                        cell.setCellValue((Integer) rowData[i]);
                    } else if (rowData[i] instanceof Double) {
                        cell.setCellValue((Double) rowData[i]);
                    } else {
                        cell.setCellValue(rowData[i].toString());
                    }
                }
            }

            // ── Auto-size columns ──
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  generateEditedExcelReport()
    //  Builds Excel from dynamic headers + rows passed as JSON
    // ══════════════════════════════════════════════════════════
    public byte[] generateEditedExcelReport(
            List<String> headers,
            List<Map<String, String>> rows) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Edited Report");

            // ── Header Row ──
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // ── Data Rows ──
            int rowNum = 1;
            for (Map<String, String> rowData : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.size(); i++) {
                    row.createCell(i).setCellValue(
                        rowData.getOrDefault(headers.get(i), "")
                    );
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  generateMissingChart()
    //  Returns a placeholder PNG byte[] for missing values chart
    //  Replace with actual chart generation (e.g., JFreeChart)
    // ══════════════════════════════════════════════════════════
    public byte[] generateMissingChart() throws IOException {
        // ── Placeholder: returns minimal valid PNG bytes ──
        // ── Replace with real chart library output ──
        return generatePlaceholderPNG("Missing Values Chart");
    }

    // ══════════════════════════════════════════════════════════
    //  generateDepartmentChart()
    //  Returns a placeholder PNG byte[] for department chart
    // ══════════════════════════════════════════════════════════
    public byte[] generateDepartmentChart() throws IOException {
        return generatePlaceholderPNG("Department Chart");
    }

    // ══════════════════════════════════════════════════════════
    //  generateSummaryCSV()
    //  Builds a simple summary CSV and returns as byte[]
    // ══════════════════════════════════════════════════════════
    public byte[] generateSummaryCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        csv.append("Total Employees,4\n");
        csv.append("Active,3\n");
        csv.append("Inactive,1\n");
        csv.append("Avg Salary,68000\n");
        return csv.toString().getBytes();
    }

    // ══════════════════════════════════════════════════════════
    //  generateZipReport()
    //  Bundles Excel + Charts + CSV into a ZIP → returns byte[]
    // ══════════════════════════════════════════════════════════
    public byte[] generateZipReport() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            // ── Entry 1: Original Excel Report ──
            addZipEntry(zos, "original_report.xlsx", generateExcelReport());

            // ── Entry 2: Missing Values Chart ──
            addZipEntry(zos, "charts/missing_values.png", generateMissingChart());

            // ── Entry 3: Department Chart ──
            addZipEntry(zos, "charts/department.png", generateDepartmentChart());

            // ── Entry 4: Summary CSV ──
            addZipEntry(zos, "summary.csv", generateSummaryCSV());

        } // ← ZipOutputStream auto-closed here

        return baos.toByteArray();
    }

    // ══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════

    // ── Adds a single file entry into the ZipOutputStream ──
    private void addZipEntry(ZipOutputStream zos, String entryName, byte[] data)
            throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(data);
        zos.closeEntry();
    }

    // ── Returns a minimal 1x1 transparent PNG as placeholder ──
    private byte[] generatePlaceholderPNG(String label) {
        // Minimal valid 1x1 PNG (67 bytes) — replace with real chart bytes
        return new byte[]{
            (byte)0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A,
            0x00,0x00,0x00,0x0D,0x49,0x48,0x44,0x52,
            0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x01,
            0x08,0x02,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x0C,0x49,0x44,0x41,0x54,
            0x08,(byte)0xD7,0x63,(byte)0xF8,(byte)0xCF,(byte)0xC0,0x00,0x00,
            0x00,0x02,0x00,0x01,(byte)0xE2,0x21,(byte)0xBC,0x33,
            0x00,0x00,0x00,0x00,0x49,0x45,0x4E,0x44,
            (byte)0xAE,0x42,0x60,(byte)0x82
        };
    }
}