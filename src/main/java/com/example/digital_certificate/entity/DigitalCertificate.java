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

    public DigitalCertificate() {
    }

    public DigitalCertificate(String serialNumber, String subject, String issuer, String commonName,
            String organization, String country, Instant validFrom, Instant validTo,
            String signatureAlgorithm, String publicKeyAlgorithm, Integer publicKeySize, String fingerprint) {
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
        this.createdAt = Instant.now();

        Instant now = Instant.now();
        if(validFrom.isAfter(now)){
            this.status = CertificateStatus.NOT_YET_VALID;
        }
        else if(validTo.isBefore(now)){
            this.status = CertificateStatus.EXPIRED;
        }
        else{
            this.status = CertificateStatus.VALID;
        }


    }

    public UUID getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getPublicKeyAlgorithm() {
        return publicKeyAlgorithm;
    }

    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }

    public Integer getPublicKeySize() {
        return publicKeySize;
    }

    public void setPublicKeySize(Integer publicKeySize) {
        this.publicKeySize = publicKeySize;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = Instant.now();
    }

    @Override
    public String toString() {
        return "DigitalCertificate [id=" + id + "\n serialNumber=" + serialNumber + "\n subject=" + subject + "\n issuer="
                + issuer + "\n commonName=" + commonName + "\n organization=" + organization + "\n country=" + country
                + "\n validFrom=" + validFrom + "\n validTo=" + validTo + "\n status=" + status + "\n signatureAlgorithm="
                + signatureAlgorithm + "\n publicKeyAlgorithm=" + publicKeyAlgorithm + "\n publicKeySize=" + publicKeySize
                + "\n fingerprint=" + fingerprint + "\n createdAt=" + createdAt + "]";
    }

}