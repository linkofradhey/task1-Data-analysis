package com.training.week1.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.week1.Service.ExportService;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    // ── Constructor Injection ──
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelBytes = exportService.generateExcelReport();

            HttpHeaders headers = new HttpHeaders();

            // ── Forces browser to download instead of open inline ──
            headers.setContentDispositionFormData(
                "attachment",
                "dashboard_report.xlsx"
            );

            // ── Correct MIME type for .xlsx files ──
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ));

            // ── Helps browser show accurate download progress ──
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // ── Returns 500 with error message as bytes ──
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Excel generation failed: " + e.getMessage()).getBytes());
        }
    }
    @GetMapping("/all")
    public ResponseEntity<byte[]> exportAll() {
        try {
            byte[] zipBytes = exportService.generateZipReport();

            HttpHeaders headers = new HttpHeaders();

            // ── Forces browser to download instead of open inline ──
            headers.setContentDispositionFormData(
                "attachment",
                "dashboard_full_report.zip"
            );

            // ── Generic binary MIME type for ZIP ──
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // ── Helps browser show accurate download progress ──
            headers.setContentLength(zipBytes.length);

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("ZIP generation failed: " + e.getMessage()).getBytes());
        }
    }
}
