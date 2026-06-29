package com.training.week1.Controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.week1.Dto.EditedDataRequest;
import com.training.week1.Service.ExportService;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    // ══════════════════════════════════════════════════════════
    //  GET /api/export/excel
    //  Returns the original unmodified report as .xlsx
    // ══════════════════════════════════════════════════════════
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelBytes = exportService.generateExcelReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "dashboard_report.xlsx");
            headers.setContentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            );
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Excel generation failed: " + e.getMessage()).getBytes());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  POST /api/export/excel/edited
    //  Receives edited table JSON → returns edited .xlsx
    // ══════════════════════════════════════════════════════════
    @PostMapping("/excel/edited")
    public ResponseEntity<byte[]> exportEditedExcel(@RequestBody EditedDataRequest editedData) {
        try {
            byte[] excelBytes = exportService.generateEditedExcelReport(
                editedData.getHeaders(),
                editedData.getRows()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "edited_report.xlsx");
            headers.setContentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            );
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Edited Excel generation failed: " + e.getMessage()).getBytes());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  GET /api/export/zip
    //
    //  WHAT IT DOES:
    //    - Calls ExportService to generate each file (Excel,
    //      charts, summary CSV)
    //    - Bundles them all into a single ZIP in memory
    //    - Returns the ZIP as a downloadable byte[] response
    // ══════════════════════════════════════════════════════════
    @GetMapping("/zip")
    public ResponseEntity<byte[]> exportZip() {
        try {
            // ── Step 1: Generate all file contents via ExportService ──
            byte[] excelBytes   = exportService.generateExcelReport();
            byte[] missingChart = exportService.generateMissingChart();
            byte[] deptChart    = exportService.generateDepartmentChart();
            byte[] summaryCSV   = exportService.generateSummaryCSV();

            // ── Step 2: Write all files into a ZIP in memory ──
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {

                // Entry 1 — Original Excel report
                zos.putNextEntry(new ZipEntry("original_report.xlsx"));
                zos.write(excelBytes);
                zos.closeEntry();

                // Entry 2 — Missing values chart
                zos.putNextEntry(new ZipEntry("charts/missing_values.png"));
                zos.write(missingChart);
                zos.closeEntry();

                // Entry 3 — Department chart
                zos.putNextEntry(new ZipEntry("charts/department.png"));
                zos.write(deptChart);
                zos.closeEntry();

                // Entry 4 — Summary CSV
                zos.putNextEntry(new ZipEntry("summary.csv"));
                zos.write(summaryCSV);
                zos.closeEntry();

            } // ← ZipOutputStream auto-closed here (try-with-resources)

            byte[] zipBytes = baos.toByteArray();

            // ── Step 3: Set response headers and return ──
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "dashboard_full_report.zip");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(zipBytes.length);

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("ZIP generation failed (IO): " + e.getMessage()).getBytes());

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("ZIP generation failed: " + e.getMessage()).getBytes());
        }
    }
}