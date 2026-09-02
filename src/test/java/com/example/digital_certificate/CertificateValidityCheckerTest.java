package com.example.digital_certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.digital_certificate.entity.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.validation.CertificateValidityChecker;

public class CertificateValidityCheckerTest {

    private static final Instant NOW = Instant.parse("2026-02-03T12:00:00Z");

    private CertificateValidityChecker checker;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        checker = new CertificateValidityChecker(clock);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01T12:00:00Z, 2027-01-01T12:00:00Z, VALID",
            "2026-03-01T12:00:00Z, 2027-01-01T12:00:00Z, NOT_YET_VALID",
            "2025-01-01T12:00:00Z, 2026-01-01T12:00:00Z, EXPIRED"
    })
    void shouldCheckCertificateValidity(Instant validFrom, Instant validTo, CertificateStatus expectedStatus) {
        DigitalCertificate certificate = DigitalCertificate.builder().validFrom(validFrom).validTo(validTo).build();
        CertificateStatus result = checker.checkValidity(certificate);

        assertEquals(expectedStatus, result);

    }

    @ParameterizedTest
    @CsvSource({
            "2026-02-03T12:00:00Z, 2027-01-01T12:00:00Z, VALID",
            "2025-03-01T12:00:00Z, 2026-02-03T12:00:00Z, VALID"
    })
    void shouldHandleBoundaries(Instant validFrom, Instant validTo, CertificateStatus expectedStatus){
        DigitalCertificate certificate = DigitalCertificate.builder().validFrom(validFrom).validTo(validTo).build();
        CertificateStatus result = checker.checkValidity(certificate);

        assertEquals(expectedStatus, result);
    }
}
