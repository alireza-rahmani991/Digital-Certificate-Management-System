package com.example.digital_certificate.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateCertificateException.class)
    public ResponseEntity<String> handleDuplicateCertificate(DuplicateCertificateException e) {
        log.warn("duplicate certificate upload rejected: {}", e.getMessage());
        return ResponseEntity.status(409).body(e.getMessage());
    }

    @ExceptionHandler(CertificateDoesNotExistException.class)
    public ResponseEntity<String> handleCertificateDoesNotExist(CertificateDoesNotExistException e) {
        log.debug("certificate not found: {} ", e.getMessage());
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("invalid request: {}", e.getMessage());
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        log.error("illegal application state: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body("internal server error");
    }

    @ExceptionHandler(CertificateProcessingException.class)
    public ResponseEntity<String> handleProcessingException(CertificateProcessingException e) {
        log.error("certificate processing failed: {}", e);
        return ResponseEntity.status(500).body("failed to process certificate");
    }

    @ExceptionHandler(InvalidCertificateException.class)
    public ResponseEntity<String> handleInvalidCertificateException(InvalidCertificateException e) {
        log.warn("Invalid X.509 certificate uploaded: {}", e.getMessage());
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBindException(BindException e) {
        log.warn("invalid search parameters: {}", e.getMessage());
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingServletRequestPart(MissingServletRequestPartException e) {
        log.warn("missing required request part: {}", e.getMessage());
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException e) {
        log.warn("multipart request could not be processed: {}", e.getMessage());
        return ResponseEntity.status(400).body(e.getMessage());
    }
}
