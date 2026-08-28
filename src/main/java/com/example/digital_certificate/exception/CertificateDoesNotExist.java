package com.example.digital_certificate.exception;

public class CertificateDoesNotExist extends RuntimeException {
    
    public CertificateDoesNotExist(String message) {
        super(message);
    }
}
