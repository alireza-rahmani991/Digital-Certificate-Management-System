package com.example.digital_certificate.service;

import com.example.digital_certificate.DTO.CertificateSearchDTO;
import com.example.digital_certificate.DTO.CertificatesStatisticsDTO;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.parser.CertificateParser;
import com.example.digital_certificate.entity.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.exception.CertificateDoesNotExistException;
import com.example.digital_certificate.exception.CertificateProcessingException;
import com.example.digital_certificate.exception.DuplicateCertificateException;
import com.example.digital_certificate.repository.DigitalCertificateRepository;
import com.example.digital_certificate.specification.DigitalCertificateSpecification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CertificateService {

    private final DigitalCertificateRepository digitalCertificateRepository;
    private final CertificateParser certificateParser;
    private final Clock clock;

    public CertificateService(DigitalCertificateRepository digitalCertificateRepository,
            CertificateParser certificateParser, Clock clock) {
        this.digitalCertificateRepository = digitalCertificateRepository;
        this.certificateParser = certificateParser;
        this.clock = clock;
    }

    public DigitalCertificate saveCertificate(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            DigitalCertificate digitalCertificate = certificateParser.parseCertificate(inputStream);
            log.debug("digital certificate created successfully : serial-number={}",
                    digitalCertificate.getSerialNumber());

            if (digitalCertificateRepository.existsByFingerprint(digitalCertificate.getFingerprint())) {
                throw new DuplicateCertificateException(
                        "certificate with fingerprint " + digitalCertificate.getFingerprint() + " already exists");
            }

            digitalCertificate = digitalCertificateRepository.save(digitalCertificate);
            log.info("digital certificate created and saved successfully : serial-number={}",
                    digitalCertificate.getSerialNumber());
            return digitalCertificate;
        } catch (IOException e) {
            throw new CertificateProcessingException("failed to read certificate file", e);
        }
    }

    public DigitalCertificate revokeCertificate(UUID id) {
        DigitalCertificate digitalCertificate = digitalCertificateRepository.findById(id)
                .orElseThrow(() -> new CertificateDoesNotExistException(
                        "no certificate with id " + id + "exist in database "));
        digitalCertificate.setStatus(CertificateStatus.REVOKED);
        return digitalCertificateRepository.save(digitalCertificate);
    }

    public DigitalCertificate findCertificateById(UUID id) {
        return digitalCertificateRepository.findById(id)
                .orElseThrow(() -> new CertificateDoesNotExistException(
                        "no certificate with id " + id + "exist in database "));
    }

    public void deleteCertificateById(UUID id) {
        if (!digitalCertificateRepository.existsById(id)) {
            throw new CertificateDoesNotExistException("certificate with id " + id + " does not exist in database ");
        }
        digitalCertificateRepository.deleteById(id);
        log.info("digital certificate deleted successfully : id={}", id);
    }

    public Page<DigitalCertificate> search(CertificateSearchDTO searchDto, Pageable pageable) {
        Specification<DigitalCertificate> spec = DigitalCertificateSpecification.filter(searchDto);
        return digitalCertificateRepository.findAll(spec, pageable);
    }

    public Page<DigitalCertificate> findbyExpiring(Pageable pageable, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException(
                    "Invalid days value: " + days + ". Days must be greater than 0");
        }
        Instant now = Instant.now(clock);
        Instant expiringLimit = now.plus(days, ChronoUnit.DAYS);

        return digitalCertificateRepository.findByStatusAndValidToBetween(CertificateStatus.VALID, now, expiringLimit,
                pageable);
    }

    public CertificatesStatisticsDTO getStatistics() {
        Instant now = Instant.now(clock);
        Instant expiringLimit = now.plus(30, ChronoUnit.DAYS);
        return new CertificatesStatisticsDTO(
                digitalCertificateRepository.count(),
                digitalCertificateRepository.countByStatus(CertificateStatus.VALID),
                digitalCertificateRepository.countByStatus(CertificateStatus.EXPIRED),
                digitalCertificateRepository.countByStatus(CertificateStatus.REVOKED),
                digitalCertificateRepository.countByStatus(CertificateStatus.NOT_YET_VALID),
                digitalCertificateRepository.countByStatusAndValidToBetween(CertificateStatus.VALID, now,
                        expiringLimit));
    }
}
