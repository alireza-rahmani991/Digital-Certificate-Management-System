package com.example.digital_certificate.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.digital_certificate.service.CertificateService;

@RestController
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }
}
