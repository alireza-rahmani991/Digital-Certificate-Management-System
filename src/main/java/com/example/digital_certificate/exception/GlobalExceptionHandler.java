package com.example.digital_certificate.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateCertificateException.class)
    public ResponseEntity<String> handleDuplicateCertificate(DuplicateCertificateException e) {
        log.error("certificate already exists in database");
        return ResponseEntity.status(409).body(e.getMessage());
    }

    @ExceptionHandler(CertificateDoesNotExist.class)
    public ResponseEntity<String> handleCertificateDoesNotExist(CertificateDoesNotExist e) {
        log.error("certificate does not exist in database");
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
