package com.example.digital_certificate.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.digital_certificate.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;

public interface DigitalCertificateRepository
        extends JpaRepository<DigitalCertificate, UUID>, JpaSpecificationExecutor<DigitalCertificate> {
    boolean existsByFingerprint(String fingerprint);

    List<DigitalCertificate> findByStatusNot(CertificateStatus status);

    Page<DigitalCertificate> findByStatusAndValidToBetween(CertificateStatus status, Instant from, Instant to,
            Pageable pageable);

    long countByStatus(CertificateStatus status);

    long countByStatusAndValidToBetween(CertificateStatus status, Instant from, Instant to);
}
