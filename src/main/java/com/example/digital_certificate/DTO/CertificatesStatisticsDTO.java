package com.example.digital_certificate.DTO;

public record CertificatesStatisticsDTO(
        long total,
        long valid,
        long revoked,
        long notYetValid,
        long expiringIn30Days) {

}
