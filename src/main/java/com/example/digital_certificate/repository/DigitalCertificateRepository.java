package com.example.digital_certificate.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.digital_certificate.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;

public interface DigitalCertificateRepository
        extends JpaRepository<DigitalCertificate, UUID>, JpaSpecificationExecutor<DigitalCertificate> {
    public boolean existsByFingerprint(String fingerprint);

    List<DigitalCertificate> findByStatusNot(CertificateStatus status);
}
