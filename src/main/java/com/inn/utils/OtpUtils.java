package com.inn.utils;

import java.security.SecureRandom;

public class OtpUtils {

    private static final int OTP_LENGTH = 6;
    private static final String DIGITS = "0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a secure random 6-digit OTP
     * @return 6-digit OTP string
     */
    public static String generateOTP() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }
}

