package com.example.digital_certificate.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.parser.CertificateParser;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.exception.DuplicateCertificateException;
import com.example.digital_certificate.repository.DigitalCertificateRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CertificateService {

    private final DigitalCertificateRepository digitalCertificateRepository;
    private final CertificateParser certificateParser;

    public CertificateService(DigitalCertificateRepository digitalCertificateRepository,
            CertificateParser certificateParser) {
        this.digitalCertificateRepository = digitalCertificateRepository;
        this.certificateParser = certificateParser;
    }

    public DigitalCertificate saveCertificate(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            DigitalCertificate digitalCertificate = certificateParser.parseCertificate(inputStream);
            log.debug("digital certificate created successfully : serial-number={}",
                    digitalCertificate.getSerialNumber());

            if (digitalCertificateRepository.existsByFingerprint(digitalCertificate.getFingerprint())) {
                throw new DuplicateCertificateException();
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

}
