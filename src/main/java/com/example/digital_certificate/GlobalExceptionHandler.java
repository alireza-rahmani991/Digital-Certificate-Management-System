package com.example.digital_certificate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.digital_certificate.exception.DuplicateCertificateException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateCertificateException.class)
    public ResponseEntity<String> handleDuplicateCertificate(DuplicateCertificateException e) {
        log.error("certificate already exists in database");
        return ResponseEntity.status(409).body(e.getMessage());
    }
}
