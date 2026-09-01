package com.example.digital_certificate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.DTO.CertificateSearchDTO;
import com.example.digital_certificate.DTO.CertificatesStatisticsDTO;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.service.CertificateService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

        return ResponseEntity.status(HttpStatus.CREATED).body(digitalCertificate);
    }

    @GetMapping
    public Page<DigitalCertificate> getCertificates(
            @Valid CertificateSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = "validTo", direction = Sort.Direction.ASC) Pageable pageable

    ) {
        return certificateService.search(searchDTO, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalCertificate> getCertificate(@PathVariable UUID id) {
        DigitalCertificate digitalCertificate = certificateService.findCertificateById(id);

        return ResponseEntity.status(HttpStatus.OK).body(digitalCertificate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable UUID id) {
        certificateService.deleteCertificateById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<DigitalCertificate> revokeCertificate(@PathVariable UUID id) {
        DigitalCertificate digitalCertificate = certificateService.revokeCertificate(id);
        return ResponseEntity.ok(digitalCertificate);
    }

    @GetMapping("/expiring")
    public Page<DigitalCertificate> getExpiringCertificates(@RequestParam int days,
            @PageableDefault(size = 20, sort = "validTo", direction = Sort.Direction.ASC) Pageable pageable) {
        return certificateService.findbyExpiring(pageable, days);
    }

    @GetMapping("/statistics")
    public CertificatesStatisticsDTO getStatistics() {
        return certificateService.getStatistics();
    }

}
