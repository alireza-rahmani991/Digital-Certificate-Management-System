package com.example.digital_certificate.validation;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.example.digital_certificate.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;

@Component
public class CertificateValidityChecker {

    private final Clock clock;

    public CertificateValidityChecker(Clock clock) {
        this.clock = clock;
    }

    public CertificateStatus checkValidity(DigitalCertificate certificate) {

        Instant now = Instant.now(clock);

        if (certificate.getValidFrom().isAfter(now)) {
             return CertificateStatus.NOT_YET_VALID;
        } else if (certificate.getValidTo().isBefore(now)) {
            return CertificateStatus.EXPIRED;
        } else {
            return CertificateStatus.VALID;
        }
    }

    public CertificateStatus checkValidity(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            return CertificateStatus.VALID;
        } catch (CertificateExpiredException e) {
            return CertificateStatus.EXPIRED;
        } catch (CertificateNotYetValidException e) {
            return CertificateStatus.NOT_YET_VALID;
        }
    }
}
