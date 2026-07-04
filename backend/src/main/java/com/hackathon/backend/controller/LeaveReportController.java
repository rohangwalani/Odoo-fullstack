package com.hackathon.backend.controller;

import com.hackathon.backend.service.LeaveReportService;
import com.lowagie.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin/leaves/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class LeaveReportController {

    private final LeaveReportService leaveReportService;

    public LeaveReportController(LeaveReportService leaveReportService) {
        this.leaveReportService = leaveReportService;
    }

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> downloadExcelReport() throws IOException {
        ByteArrayInputStream in = leaveReportService.generateExcelReport();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=leaves_report.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> downloadPdfReport() throws DocumentException {
        ByteArrayInputStream in = leaveReportService.generatePdfReport();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=leaves_report.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
