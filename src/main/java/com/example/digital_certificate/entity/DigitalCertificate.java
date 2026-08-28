package com.example.digital_certificate.entity;

import java.time.Instant;
import java.util.UUID;

import com.example.digital_certificate.CertificateStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "digital_certificates")
public class DigitalCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;
    @Column(name = "subject", nullable = false)
    private String subject;
    @Column(name = "issuer", nullable = false)
    private String issuer;
    @Column(name = "common_name")
    private String commonName;
    @Column(name = "organization")
    private String organization;
    @Column(name = "country")
    private String country;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;
    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificateStatus status;

    @Column(name = "signature_algorithm", nullable = false)
    private String signatureAlgorithm;
    @Column(name = "public_key_algorithm", nullable = false)
    private String publicKeyAlgorithm;
    @Column(name = "public_key_size", nullable = false)
    private Integer publicKeySize;

    @Column(name = "fingerprint", nullable = false, unique = true)
    private String fingerprint;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = Instant.now();
    }

    @Builder
    public DigitalCertificate(String serialNumber, String subject, String issuer, String commonName,
            String organization, String country, Instant validFrom, Instant validTo,
            String signatureAlgorithm, String publicKeyAlgorithm, Integer publicKeySize, String fingerprint, CertificateStatus status) {
        this.serialNumber = serialNumber;
        this.subject = subject;
        this.issuer = issuer;
        this.commonName = commonName;
        this.organization = organization;
        this.country = country;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.signatureAlgorithm = signatureAlgorithm;
        this.publicKeyAlgorithm = publicKeyAlgorithm;
        this.publicKeySize = publicKeySize;
        this.fingerprint = fingerprint;
        this.status = status;

    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }
}