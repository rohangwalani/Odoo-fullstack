package com.hackathon.backend.util;

import com.hackathon.backend.model.Company;
import com.hackathon.backend.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeIdGenerator {

    public String generate(Company company, String firstName, String lastName, int year, Employee latestEmployee) {
        String companyCode = getCode(company.getCompanyName(), 2);
        String initials = getCode(firstName, 2) + getCode(lastName, 2);
        
        int serialNumber = 1;
        if (latestEmployee != null && latestEmployee.getLoginId() != null) {
            String lastLoginId = latestEmployee.getLoginId();
            // The serial number is the last 4 digits
            if (lastLoginId.length() >= 4) {
                try {
                    String serialPart = lastLoginId.substring(lastLoginId.length() - 4);
                    serialNumber = Integer.parseInt(serialPart) + 1;
                } catch (NumberFormatException e) {
                    // fallback to 1 if parsing fails
                    serialNumber = 1;
                }
            }
        }

        return String.format("%s%s%d%04d", companyCode, initials, year, serialNumber);
    }

    private String getCode(String text, int length) {
        if (text == null || text.trim().isEmpty()) {
            return "XX";
        }
        text = text.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (text.length() >= length) {
            return text.substring(0, length);
        } else {
            // Pad with X if too short
            return String.format("%-" + length + "s", text).replace(' ', 'X');
        }
    }
}
