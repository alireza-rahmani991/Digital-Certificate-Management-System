package com.example.digital_certificate.exception;

public class CertificateDoesNotExistException extends RuntimeException {
    
    public CertificateDoesNotExistException(String message) {
        super(message);
    }
}
