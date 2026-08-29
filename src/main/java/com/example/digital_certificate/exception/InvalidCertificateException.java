package com.example.digital_certificate.exception;

public class InvalidCertificateException extends RuntimeException {
    public InvalidCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}
