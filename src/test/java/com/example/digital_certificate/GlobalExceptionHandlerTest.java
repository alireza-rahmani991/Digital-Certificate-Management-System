package com.example.digital_certificate;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.example.digital_certificate.exception.CertificateDoesNotExistException;
import com.example.digital_certificate.exception.CertificateProcessingException;
import com.example.digital_certificate.exception.DuplicateCertificateException;
import com.example.digital_certificate.exception.GlobalExceptionHandler;
import com.example.digital_certificate.exception.InvalidCertificateException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn409ForDuplicateCertificate() {
        DuplicateCertificateException ex = new DuplicateCertificateException("cert already exists");

        ResponseEntity<String> response = handler.handleDuplicateCertificate(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("cert already exists", response.getBody());
    }

    @Test
    void shouldReturn404ForCertificateNotFound() {
        CertificateDoesNotExistException ex = new CertificateDoesNotExistException("cert not found");

        ResponseEntity<String> response = handler.handleCertificateDoesNotExist(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("cert not found", response.getBody());
    }

    @Test
    void shouldReturn400ForIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("bad param");

        ResponseEntity<String> response = handler.handleIllegalArgumentException(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("bad param", response.getBody());
    }

    @Test
    void shouldReturn500AndHideDetailsForIllegalState() {
        IllegalStateException ex = new IllegalStateException("SHA-256 not available");

        ResponseEntity<String> response = handler.handleIllegalStateException(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("internal server error", response.getBody());
    }

    @Test
    void shouldReturn500AndHideDetailsForProcessingException() {
        CertificateProcessingException ex = new CertificateProcessingException("encoding failed",
                new RuntimeException("cause"));

        ResponseEntity<String> response = handler.handleProcessingException(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("failed to process certificate", response.getBody());
    }

    @Test
    void shouldReturn400ForInvalidCertificate() {
        InvalidCertificateException ex = new InvalidCertificateException(
                "uploaded file is not a valid X509 certificate", new RuntimeException());

        ResponseEntity<String> response = handler.handleInvalidCertificateException(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("uploaded file is not a valid X509 certificate", response.getBody());
    }
}
