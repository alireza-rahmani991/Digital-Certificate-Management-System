package com.example.digital_certificate.service;

import org.springframework.stereotype.Service;

import com.example.digital_certificate.repository.DigitalCertificateRepository;

@Service
public class CertificateService {

    private final DigitalCertificateRepository digitalCertificateRepository;

    public CertificateService(DigitalCertificateRepository digitalCertificateRepository) {
        this.digitalCertificateRepository = digitalCertificateRepository;
    }
    
}
