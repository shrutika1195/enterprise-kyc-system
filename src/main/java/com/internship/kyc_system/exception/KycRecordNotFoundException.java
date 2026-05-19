package com.internship.kyc_system.exception;

public class KycRecordNotFoundException extends RuntimeException {
    public KycRecordNotFoundException(String message) {
        super(message);
    }
}