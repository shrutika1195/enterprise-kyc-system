package com.internship.kyc_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OTP Service with Email Integration
 * - Sends OTP via email (Free & Unlimited)
 * - OTP expiry (configurable, default 2 minutes)
 * - Thread-safe storage using ConcurrentHashMap
 */
@Service
public class OtpService {

    // Each entry stores the OTP string and its expiry timestamp
    private record OtpEntry(String otp, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:#{null}}")
    private String fromEmail;

    @Value("${otp.expiry.seconds:120}")
    private int otpExpirySeconds;

    private final Random random = new Random();

    /**
     * Generates a 6-digit OTP, stores it with expiry, and sends via email.
     */
    public void generateAndSendOtp(String email, String phoneNumber) {
        // Clean up expired OTPs on each new request
        otpStorage.entrySet().removeIf(e -> e.getValue().isExpired());

        String otp = String.format("%06d", random.nextInt(999999));
        Instant expiry = Instant.now().plusSeconds(otpExpirySeconds);

        // Use email as key for storage
        otpStorage.put(email, new OtpEntry(otp, expiry));

        if (fromEmail == null || fromEmail.equals("your-email@gmail.com")) {
            // Development fallback — visible in server console
            System.out.println("\n==========================================");
            System.out.println("  [DEV MODE] OTP for " + email + ": " + otp);
            System.out.println("  Phone: " + phoneNumber);
            System.out.println("  Expires in " + otpExpirySeconds + " seconds");
            System.out.println("  Please configure spring.mail.username in application.properties");
            System.out.println("==========================================\n");
        } else {
            sendOtpViaEmail(email, otp, phoneNumber);
        }
    }

    /**
     * Sends OTP via email
     */
    private void sendOtpViaEmail(String email, String otp, String phoneNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Your KYC Verification OTP");
            message.setText(
                    "Dear User,\n\n" +
                            "Your OTP for KYC verification is: " + otp + "\n\n" +
                            "Phone Number: " + phoneNumber + "\n" +
                            "Valid for: " + otpExpirySeconds + " seconds\n\n" +
                            "This OTP is valid only for this KYC submission.\n" +
                            "Do not share this OTP with anyone.\n\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Regards,\n" +
                            "KYC Portal Team"
            );

            mailSender.send(message);
            System.out.println("[EMAIL] OTP sent successfully to " + email + " for phone: " + phoneNumber);

        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send OTP to " + email + ": " + e.getMessage());
            // Fallback to console
            System.out.println("\n==========================================");
            System.out.println("  [FALLBACK] OTP for " + email + ": " + otp);
            System.out.println("  Please check your email configuration");
            System.out.println("==========================================\n");
        }
    }

    /**
     * Verifies an OTP. Returns true if valid and not expired, and removes it.
     */
    public boolean verifyOtp(String email, String userProvidedOtp) {
        if (email == null || userProvidedOtp == null) return false;

        OtpEntry entry = otpStorage.get(email);
        if (entry == null) {
            System.out.println("[OTP] No OTP found for email: " + email);
            return false;
        }

        if (entry.isExpired()) {
            otpStorage.remove(email);
            System.out.println("[OTP] Expired OTP for email: " + email);
            return false;
        }

        if (entry.otp().equals(userProvidedOtp.trim())) {
            otpStorage.remove(email);
            System.out.println("[OTP] Successfully verified OTP for email: " + email);
            return true;
        }

        System.out.println("[OTP] Invalid OTP attempt for email: " + email);
        return false;
    }
}