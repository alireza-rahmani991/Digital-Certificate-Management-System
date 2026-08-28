package com.example.digital_certificate.validation;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.digital_certificate.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.repository.DigitalCertificateRepository;

@Component
public class CertificateStatusScheduler {

    private final CertificateValidityChecker certificateValidityChecker;
    private final DigitalCertificateRepository certificateRepository;

    public CertificateStatusScheduler(CertificateValidityChecker certificateValidityChecker,
            DigitalCertificateRepository certificateRepository) {
        this.certificateValidityChecker = certificateValidityChecker;
        this.certificateRepository = certificateRepository;
    }

    @Scheduled(cron = "0 0 1 * * *")//runs every day at 1 AM
    @Transactional
    public void refreshStatuses() {

        List<DigitalCertificate> certificates = certificateRepository.findByStatusNot(
                CertificateStatus.REVOKED);

        for (DigitalCertificate certificate : certificates) {

            CertificateStatus newStatus = certificateValidityChecker.checkValidity(certificate);

            if (newStatus != certificate.getStatus()) {
                certificate.setStatus(newStatus);
            }
        }
    }
}
