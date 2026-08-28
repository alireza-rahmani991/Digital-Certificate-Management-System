package com.example.digital_certificate.DTO;

import java.time.Instant;

import com.example.digital_certificate.CertificateStatus;

public record CertificateSearchDTO(
    String serialNumber,
    String subject,
    String issuer,
    String commonName,
    String organization,
    String country,
    Instant validFrom,
    Instant validTo,
    CertificateStatus status
) {
    
}
