package com.example.digital_certificate.specification;

import org.springframework.data.jpa.domain.Specification;

import com.example.digital_certificate.DTO.CertificateSearchDTO;
import com.example.digital_certificate.entity.DigitalCertificate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DigitalCertificateSpecification {

    public static Specification<DigitalCertificate> filter(CertificateSearchDTO request) {
        List<Specification<DigitalCertificate>> specs = new ArrayList<>();

        addIfPresent(specs, request.serialNumber(), v -> containsIgnoreCase("serialNumber", v));
        addIfPresent(specs, request.subject(), v -> containsIgnoreCase("subject", v));
        addIfPresent(specs, request.issuer(), v -> containsIgnoreCase("issuer", v));
        addIfPresent(specs, request.commonName(), v -> containsIgnoreCase("commonName", v));
        addIfPresent(specs, request.organization(), v -> equalsIgnoreCase("organization", v));
        addIfPresent(specs, request.country(), v -> equalsIgnoreCase("country", v));
        // addIfPresent(specs, request.fingerprint(), v -> equalsIgnoreCase("fingerprint", v));

        if (request.status() != null) {
            specs.add(equalsExact("status", request.status()));
        }
        if (request.validFrom() != null) {
            specs.add(after("validFrom", request.validFrom()));
        }
        if (request.validTo() != null) {
            specs.add(before("validTo", request.validTo()));
        }

        return Specification.allOf(specs);
    }

    

    private static Specification<DigitalCertificate> containsIgnoreCase(String field, String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    private static Specification<DigitalCertificate> equalsIgnoreCase(String field, String value) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.toLowerCase());
    }

    private static <T> Specification<DigitalCertificate> equalsExact(String field, T value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private static Specification<DigitalCertificate> after(String field, Instant value) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
    }

    private static Specification<DigitalCertificate> before(String field, Instant value) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
    }

    private static void addIfPresent(
            List<Specification<DigitalCertificate>> specs,
            String value,
            Function<String, Specification<DigitalCertificate>> factory) {
        if (value != null && !value.isBlank()) {
            specs.add(factory.apply(value));
        }
    }
}