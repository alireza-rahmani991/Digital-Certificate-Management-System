package com.example.digital_certificate.service;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.CertificateParser;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.repository.DigitalCertificateRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CertificateService {

    private final DigitalCertificateRepository digitalCertificateRepository;
    private final CertificateParser certificateParser;

    public CertificateService(DigitalCertificateRepository digitalCertificateRepository, CertificateParser certificateParser) {
        this.digitalCertificateRepository = digitalCertificateRepository;
        this.certificateParser = certificateParser;
    }

    public DigitalCertificate saveCertificate(MultipartFile file){
        try{
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate;
            try(InputStream inputStream = file.getInputStream()){
                certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                log.debug("certificate created successfully : serial-number={}", certificate.getSerialNumber());
            }
            DigitalCertificate digitalCertificate = certificateParser.parseCertificate(certificate);
            log.debug("digital certificate created successfully : serial-number={}", digitalCertificate.getSerialNumber());
            digitalCertificate = digitalCertificateRepository.save(digitalCertificate);
            log.info("digital certificate created and saved successfully : serial-number={}", digitalCertificate.getSerialNumber());
            return digitalCertificate;
            
        }
        catch (Exception e){
            throw new RuntimeException("Failed to parse certificate", e);
        }
    }
    
}
