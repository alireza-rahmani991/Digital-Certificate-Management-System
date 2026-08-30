package com.example.digital_certificate.DTO;

import java.time.Instant;

import com.example.digital_certificate.CertificateStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record CertificateSearchDTO(
    @Size(max = 200) String serialNumber,
    @Size(max = 200)String subject,
    @Size(max = 200) String issuer,
    @Size(max = 200) String commonName,
    @Size(max = 200) String organization,
    @Size(max = 2) String country,
    Instant validFrom,
    Instant validTo,
    CertificateStatus status
) {
    @AssertTrue(message = "validFrom must not be after validTo")
    public boolean isValidRange() {
        return validFrom == null || validTo == null || !validFrom.isAfter(validTo);
    }
    
}
