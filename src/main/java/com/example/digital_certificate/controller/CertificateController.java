package com.example.digital_certificate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.service.CertificateService;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping
    public ResponseEntity<DigitalCertificate> postNewCertificate(
            @RequestParam("certificate") MultipartFile certificate) {
        DigitalCertificate digitalCertificate = certificateService.saveCertificate(certificate);

        return ResponseEntity.status(201).body(digitalCertificate);
    }

    @GetMapping
    public String getCertificates() {
        return new String();
    }

    @GetMapping("/{id}")
    public DigitalCertificate getCertificate(@PathVariable UUID id) {
        return new DigitalCertificate();
    }

    @DeleteMapping("/{id}")
    public void deleteCertificate(@PathVariable UUID id) {
        return;
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<DigitalCertificate> revokeCertificate(@PathVariable UUID id) {
        DigitalCertificate digitalCertificate =certificateService.revokeCertificate(id);
        return ResponseEntity.ok(digitalCertificate);
    }

    @GetMapping("/expiring")
    public String getExpiringCertificates(@RequestParam UUID id) {
        return new String();
    }

    @GetMapping("/statistics")
    public String getMethodName() {
        return new String();
    }

}
