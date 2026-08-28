package com.example.digital_certificate.service;

import com.example.digital_certificate.CertificateStatus;
import com.example.digital_certificate.DigitalCertificateApplication;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.parser.CertificateParser;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.exception.CertificateDoesNotExist;
import com.example.digital_certificate.exception.DuplicateCertificateException;
import com.example.digital_certificate.repository.DigitalCertificateRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CertificateService {

    private final DigitalCertificateRepository digitalCertificateRepository;
    private final CertificateParser certificateParser;

    public CertificateService(DigitalCertificateRepository digitalCertificateRepository,
            CertificateParser certificateParser, DigitalCertificateApplication digitalCertificateApplication) {
        this.digitalCertificateRepository = digitalCertificateRepository;
        this.certificateParser = certificateParser;
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
            e.printStackTrace();
        }
        return null;
    }

    public DigitalCertificate revokeCertificate(UUID id) {
        DigitalCertificate digitalCertificate = digitalCertificateRepository.findById(id)
                .orElseThrow(() -> new CertificateDoesNotExist("no certificate with id " + id + "exist in database "));
        digitalCertificate.setStatus(CertificateStatus.REVOKED);
        return digitalCertificateRepository.save(digitalCertificate);
    }

}
