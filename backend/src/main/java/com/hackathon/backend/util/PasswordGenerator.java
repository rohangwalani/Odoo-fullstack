package com.hackathon.backend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateTemporaryPassword() {
        // Generates a 5 digit random number between 10000 and 99999
        int number = 10000 + random.nextInt(90000);
        return "Emp@" + number;
    }
}
