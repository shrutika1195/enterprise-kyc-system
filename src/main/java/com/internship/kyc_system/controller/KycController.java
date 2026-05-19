package com.internship.kyc_system.controller;

import com.internship.kyc_system.dto.KycRequestDto;
import com.internship.kyc_system.entity.KycRecord;
import com.internship.kyc_system.exception.KycRecordNotFoundException;
import com.internship.kyc_system.service.KycService;
import com.internship.kyc_system.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * KYC REST Controller.
 *
 * Improvements:
 * - Returns ResponseEntity with proper HTTP status codes (201 Created, 400, 404, etc.)
 * - OTP endpoint returns meaningful JSON error on failure
 * - IllegalArgumentException from service layer mapped to 400 Bad Request
 * - Phone number saved to KycRecord for audit
 */
@RestController
@RequestMapping("/api/kyc")
public class KycController {

    private final KycService kycService;
    private final OtpService otpService;

    public KycController(KycService kycService, OtpService otpService) {
        this.kycService = kycService;
        this.otpService = otpService;
    }

    /**
     * POST /api/kyc/generate-otp?email=user@example.com&phoneNumber=9876543210
     * Triggers OTP generation and email delivery.
     * Returns 200 OK on success, 400 if email is invalid.
     */
    @PostMapping("/generate-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String email, @RequestParam String phoneNumber) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email address is required."));
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Phone number is required."));
        }

        // Basic email format validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please enter a valid email address."));
        }

        // Basic Indian mobile number format check (10 digits, optionally +91 prefix)
        String cleaned = phoneNumber.replaceAll("^\\+91", "").replaceAll("[^0-9]", "");
        if (cleaned.length() != 10) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please enter a valid 10-digit Indian mobile number."));
        }

        otpService.generateAndSendOtp(email, phoneNumber);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + email));
    }

    /**
     * POST /api/kyc  (multipart/form-data)
     * Submits a new KYC application after OTP verification.
     * Returns 201 Created with the saved record, or 400/422 on validation/OTP failure.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewKyc(
            @Valid @ModelAttribute KycRequestDto requestDto,
            @RequestParam("file") MultipartFile file) {

        boolean isOtpValid = otpService.verifyOtp(requestDto.getEmail(), requestDto.getOtp());
        if (!isOtpValid) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Invalid or expired OTP. Please request a new OTP and try again."));
        }

        KycRecord newRecord = new KycRecord();
        newRecord.setApplicantName(requestDto.getApplicantName().trim());
        newRecord.setDocumentType(requestDto.getDocumentType());
        newRecord.setDocumentNumber(requestDto.getDocumentNumber().toUpperCase().trim());
        newRecord.setPhoneNumber(requestDto.getPhoneNumber());
        newRecord.setEmail(requestDto.getEmail());  // NEW: Save email to record

        try {
            KycRecord saved = kycService.createRecord(newRecord, file);
            return ResponseEntity.status(201).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to store document. Please try again."));
        }
    }

    /**
     * PUT /api/kyc/{id}/status?status=APPROVED
     * Admin-only endpoint to approve or reject a KYC record.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateKycStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            KycRecord updated = kycService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/kyc
     * Admin-only: returns all KYC records.
     */
    @GetMapping
    public ResponseEntity<List<KycRecord>> getAllKyc() {
        return ResponseEntity.ok(kycService.getAllRecords());
    }

    /**
     * GET /api/kyc/{id}
     * Returns a single KYC record by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KycRecord> getKycById(@PathVariable Long id) {
        KycRecord record = kycService.getRecordById(id)
                .orElseThrow(() -> new KycRecordNotFoundException(
                        "KYC Record not found with ID: " + id));
        return ResponseEntity.ok(record);
    }
}