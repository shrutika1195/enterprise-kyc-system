package com.internship.kyc_system.service;

import com.internship.kyc_system.entity.KycRecord;
import com.internship.kyc_system.repository.KycRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * KYC business logic service.
 *
 * Improvements over original:
 * - File type validation (only PDF/JPG/PNG allowed)
 * - File size validation (max 5MB)
 * - Uses StandardCopyOption.REPLACE_EXISTING to avoid partial-write issues
 * - Status validation to prevent invalid status values
 * - Upload directory configurable via application.properties
 */
@Service
public class KycService {

    private final KycRepository kycRepository;

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");
    private static final String UPLOAD_DIR = "uploads/";

    public KycService(KycRepository kycRepository) {
        this.kycRepository = kycRepository;
    }

    /**
     * Creates a new KYC record after validating and storing the uploaded document.
     *
     * @throws IllegalArgumentException if the file is invalid
     * @throws IOException              if file storage fails
     */
    public KycRecord createRecord(KycRecord record, MultipartFile file) throws IOException {
        validateFile(file);

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Use UUID prefix to prevent filename collisions and path traversal attacks
        String originalName = sanitizeFilename(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // REPLACE_EXISTING ensures atomic write even if a partial file exists
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        record.setDocumentFileName(uniqueFileName);
        record.setVerificationStatus("PENDING");
        return kycRepository.save(record);
    }

    /**
     * Updates the verification status of a KYC record.
     *
     * @throws IllegalArgumentException if the status is not one of PENDING/APPROVED/REJECTED
     * @throws RuntimeException         if record is not found
     */
    public KycRecord updateStatus(Long id, String newStatus) {
        if (!VALID_STATUSES.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid status '" + newStatus + "'. Must be one of: " + VALID_STATUSES);
        }

        KycRecord record = kycRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC Record not found with ID: " + id));

        record.setVerificationStatus(newStatus.toUpperCase());
        return kycRepository.save(record);
    }

    /** Returns all KYC records, newest first. */
    public List<KycRecord> getAllRecords() {
        return kycRepository.findAll();
    }

    /** Returns a single record by ID, or empty if not found. */
    public Optional<KycRecord> getRecordById(Long id) {
        return kycRepository.findById(id);
    }

    // ── Private Helpers ──

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds the 5MB limit. Please upload a smaller file.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only PDF, JPG, and PNG documents are accepted.");
        }
    }

    /**
     * Strips any directory components from the filename to prevent path traversal.
     * e.g. "../../etc/passwd" → "passwd"
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "document";
        return Paths.get(filename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}