package com.example.digital_certificate.exception;

public class CertificateProcessingException extends RuntimeException {

    public CertificateProcessingException(String message, Throwable cause){
        super(message, cause);
    }
    
}
