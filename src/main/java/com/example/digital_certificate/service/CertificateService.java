package com.example.digital_certificate.service;

import org.springframework.stereotype.Service;

import com.example.digital_certificate.CertificateParser;
import com.example.digital_certificate.repository.DigitalCertificateRepository;

@Service
public class CertificateService {

    private final DigitalCertificateRepository digitalCertificateRepository;
    private final CertificateParser certificateParser;

    public CertificateService(DigitalCertificateRepository digitalCertificateRepository, CertificateParser certificateParser) {
        this.digitalCertificateRepository = digitalCertificateRepository;
        this.certificateParser = certificateParser;
    }
    
}
