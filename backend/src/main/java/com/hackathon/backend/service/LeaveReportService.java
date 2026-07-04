package com.hackathon.backend.service;

import com.hackathon.backend.model.LeaveRequest;
import com.hackathon.backend.repository.LeaveRequestRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class LeaveReportService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveReportService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public ByteArrayInputStream generateExcelReport() throws IOException {
        List<LeaveRequest> leaves = leaveRequestRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Leave Report");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Employee Name", "Leave Type", "Start Date", "End Date", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (LeaveRequest leave : leaves) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(leave.getId());
                row.createCell(1).setCellValue(leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName());
                row.createCell(2).setCellValue(leave.getLeaveType().name());
                row.createCell(3).setCellValue(leave.getFromDate().toString());
                row.createCell(4).setCellValue(leave.getToDate().toString());
                row.createCell(5).setCellValue(leave.getStatus().name());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream generatePdfReport() throws DocumentException {
        List<LeaveRequest> leaves = leaveRequestRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Leave Report\n\n"));

        for (LeaveRequest leave : leaves) {
            String record = String.format("ID: %d | Name: %s %s | Type: %s | Date: %s to %s | Status: %s",
                    leave.getId(), leave.getEmployee().getFirstName(), leave.getEmployee().getLastName(),
                    leave.getLeaveType().name(), leave.getFromDate(), leave.getToDate(), leave.getStatus().name());
            document.add(new Paragraph(record));
        }

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
