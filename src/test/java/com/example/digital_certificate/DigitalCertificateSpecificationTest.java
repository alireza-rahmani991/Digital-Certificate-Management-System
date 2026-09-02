package com.example.digital_certificate;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.digital_certificate.DTO.CertificateSearchDTO;
import com.example.digital_certificate.entity.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.repository.DigitalCertificateRepository;
import com.example.digital_certificate.specification.DigitalCertificateSpecification;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class DigitalCertificateSpecificationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private DigitalCertificateRepository repository;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private DigitalCertificate acmeValid;
    private DigitalCertificate acmeExpired;
    private DigitalCertificate globexValid;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        acmeValid = repository.save(cert(builder -> builder
                .serialNumber("SN-1001")
                .subject("CN=acme.com, O=Acme Corp")
                .issuer("CN=Acme Root CA")
                .commonName("acme.com")
                .organization("Acme Corp")
                .country("US")
                .status(CertificateStatus.VALID)
                .validFrom(NOW.minus(30, ChronoUnit.DAYS))
                .validTo(NOW.plus(60, ChronoUnit.DAYS))));

        acmeExpired = repository.save(cert(builder -> builder
                .serialNumber("SN-1002")
                .subject("CN=old.acme.com, O=Acme Corp")
                .issuer("CN=Acme Root CA")
                .commonName("old.acme.com")
                .organization("Acme Corp")
                .country("US")
                .status(CertificateStatus.EXPIRED)
                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                .validTo(NOW.minus(30, ChronoUnit.DAYS))));

        globexValid = repository.save(cert(builder -> builder
                .serialNumber("SN-2001")
                .subject("CN=globex.io, O=Globex Inc")
                .issuer("CN=Globex Intermediate CA")
                .commonName("globex.io")
                .organization("Globex Inc")
                .country("DE")
                .status(CertificateStatus.VALID)
                .validFrom(NOW.minus(10, ChronoUnit.DAYS))
                .validTo(NOW.plus(90, ChronoUnit.DAYS))));
    }

    @Test
    void emptyCriteriaReturnsEverything() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(emptySearch()));

        assertThat(result).containsExactlyInAnyOrder(acmeValid, acmeExpired, globexValid);
    }

    @Test
    void filtersBySerialNumberSubstringCaseInsensitively() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withSerialNumber("sn-100"))));

        assertThat(result).containsExactlyInAnyOrder(acmeValid, acmeExpired);
    }

    @Test
    void filtersBySubjectSubstring() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withSubject("old.acme"))));

        assertThat(result).containsExactly(acmeExpired);
    }

    @Test
    void filtersByIssuerSubstring() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withIssuer("Globex"))));

        assertThat(result).containsExactly(globexValid);
    }

    @Test
    void filtersByCommonNameSubstring() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withCommonName("ACME.COM")))); // mixed case

        assertThat(result).containsExactlyInAnyOrder(acmeValid, acmeExpired);
    }

    @Test
    void filtersByOrganizationExactMatchCaseInsensitively() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withOrganization("acme corp"))));

        assertThat(result).containsExactlyInAnyOrder(acmeValid, acmeExpired);
    }

    @Test
    void organizationFilterDoesNotMatchPartialValue() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withOrganization("Acme"))));

        assertThat(result).isEmpty();
    }

    @Test
    void filtersByCountryExactMatch() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withCountry("DE"))));

        assertThat(result).containsExactly(globexValid);
    }

    @Test
    void filtersByStatus() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withStatus(CertificateStatus.EXPIRED))));

        assertThat(result).containsExactly(acmeExpired);
    }

    @Test
    void filtersByValidFromLowerBound() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withValidFrom(NOW.minus(15, ChronoUnit.DAYS)))));

        assertThat(result).containsExactly(globexValid);
    }

    @Test
    void filtersByValidToUpperBound() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withValidTo(NOW))));

        assertThat(result).containsExactly(acmeExpired);
    }

    @Test
    void combinesMultipleFiltersWithAnd() {
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s
                        .withOrganization("Acme Corp")
                        .withStatus(CertificateStatus.VALID))));

        assertThat(result).containsExactly(acmeValid);
    }

    @Test
    void blankStringFiltersAreIgnored() {
        // blank/whitespace-only strings should behave like "not provided"
        List<DigitalCertificate> result = repository.findAll(
                DigitalCertificateSpecification.filter(search(s -> s.withSubject("   "))));

        assertThat(result).containsExactlyInAnyOrder(acmeValid, acmeExpired, globexValid);
    }


    private DigitalCertificate cert(java.util.function.UnaryOperator<DigitalCertificate.DigitalCertificateBuilder> customizer) {
        DigitalCertificate.DigitalCertificateBuilder builder = DigitalCertificate.builder()
                .signatureAlgorithm("SHA256withRSA")
                .publicKeyAlgorithm("RSA")
                .publicKeySize(2048)
                .fingerprint(java.util.UUID.randomUUID().toString());
        return customizer.apply(builder).build();
    }

    private CertificateSearchDTO emptySearch() {
        return new CertificateSearchDTO(null, null, null, null, null, null, null, null, null);
    }

    private CertificateSearchDTO search(java.util.function.UnaryOperator<SearchBuilder> customizer) {
        return customizer.apply(new SearchBuilder()).build();
    }

    private static class SearchBuilder {
        private String serialNumber;
        private String subject;
        private String issuer;
        private String commonName;
        private String organization;
        private String country;
        private Instant validFrom;
        private Instant validTo;
        private CertificateStatus status;

        SearchBuilder withSerialNumber(String v) { this.serialNumber = v; return this; }
        SearchBuilder withSubject(String v) { this.subject = v; return this; }
        SearchBuilder withIssuer(String v) { this.issuer = v; return this; }
        SearchBuilder withCommonName(String v) { this.commonName = v; return this; }
        SearchBuilder withOrganization(String v) { this.organization = v; return this; }
        SearchBuilder withCountry(String v) { this.country = v; return this; }
        SearchBuilder withValidFrom(Instant v) { this.validFrom = v; return this; }
        SearchBuilder withValidTo(Instant v) { this.validTo = v; return this; }
        SearchBuilder withStatus(CertificateStatus v) { this.status = v; return this; }

        CertificateSearchDTO build() {
            return new CertificateSearchDTO(
                    serialNumber, subject, issuer, commonName, organization, country,
                    validFrom, validTo, status);
        }
    }
}