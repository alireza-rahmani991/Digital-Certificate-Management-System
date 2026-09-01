package com.example.digital_certificate;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.digital_certificate.configuration.FixedClockTestConfig;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.repository.DigitalCertificateRepository;
import com.example.digital_certificate.validation.CertificateStatusScheduler;

import jakarta.persistence.EntityManager;

import org.testcontainers.junit.jupiter.Container;

@SpringBootTest
@Testcontainers
@Import(FixedClockTestConfig.class)
public class CertificateStatusSchedulerTest {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

        @Autowired
        private CertificateStatusScheduler scheduler;
        @Autowired
        private DigitalCertificateRepository repository;
        @Autowired
        private EntityManager entityManager;

        private final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

        @Test
        @Transactional
        void shouldUpdateStatusOfNewlyExpiredCertificates() {
                // arrange
                DigitalCertificate certificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.VALID)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("123")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.minus(50, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                DigitalCertificate result = repository.findById(certificate.getId()).orElseThrow();
                assertThat(result.getStatus()).isEqualTo(CertificateStatus.EXPIRED);
        }

        @Test
        @Transactional
        void shouldNotTouchRevokedCertificates() {
                // arrange
                DigitalCertificate certificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.REVOKED)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("123")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                DigitalCertificate result = repository.findById(certificate.getId()).orElseThrow();
                assertThat(result.getStatus()).isEqualTo(CertificateStatus.REVOKED);
        }

        @Test
        @Transactional
        void shouldChangeNotYetValidToValid() {
                // arrange
                DigitalCertificate certificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.NOT_YET_VALID)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("123")
                                                .validFrom(NOW.minus(1, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                DigitalCertificate result = repository.findById(certificate.getId()).orElseThrow();
                assertThat(result.getStatus()).isEqualTo(CertificateStatus.VALID);
        }

        @Test
        @Transactional
        void shouldNotChangeExpiredCertificates() {
                // arrange
                DigitalCertificate certificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.EXPIRED)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("123")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.minus(20, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                DigitalCertificate result = repository.findById(certificate.getId()).orElseThrow();
                assertThat(result.getStatus()).isEqualTo(CertificateStatus.EXPIRED);
        }

        @Test
        @Transactional
        void shouldNotChangeValidCertificates() {
                // arrange
                DigitalCertificate certificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.VALID)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("123")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                DigitalCertificate result = repository.findById(certificate.getId()).orElseThrow();
                assertThat(result.getStatus()).isEqualTo(CertificateStatus.VALID);
        }

        @Test
        @Transactional
        void shouldNotChangeNotYetValidCertifiactes() {
                // arrange
                DigitalCertificate certificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.NOT_YET_VALID)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("123")
                                                .validFrom(NOW.plus(1, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                DigitalCertificate result = repository.findById(certificate.getId()).orElseThrow();
                assertThat(result.getStatus()).isEqualTo(CertificateStatus.NOT_YET_VALID);
        }

        @Test
        @Transactional
        void shouldHandleMultipleCertificates() {
                // arrange
                DigitalCertificate validCertificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.VALID)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("1")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());
                DigitalCertificate notYetValidCertificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.NOT_YET_VALID)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("2")
                                                .validFrom(NOW.minus(1, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());
                DigitalCertificate revokedCertificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.REVOKED)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("3")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.plus(50, ChronoUnit.DAYS))
                                                .build());

                DigitalCertificate expiredCertificate = repository.save(
                                DigitalCertificate.builder()
                                                .status(CertificateStatus.EXPIRED)
                                                .serialNumber("test")
                                                .subject("test subject")
                                                .issuer("test issuer")
                                                .signatureAlgorithm("rsa")
                                                .publicKeyAlgorithm("rsa")
                                                .publicKeySize(2048)
                                                .fingerprint("4")
                                                .validFrom(NOW.minus(400, ChronoUnit.DAYS))
                                                .validTo(NOW.minus(20, ChronoUnit.DAYS))
                                                .build());

                // act
                scheduler.refreshStatuses();
                entityManager.flush();
                entityManager.clear();

                // assert
                assertThat(repository.findById(validCertificate.getId()).orElseThrow().getStatus())
                                .isEqualTo(CertificateStatus.VALID);
                assertThat(repository.findById(notYetValidCertificate.getId()).orElseThrow().getStatus())
                                .isEqualTo(CertificateStatus.VALID);
                assertThat(repository.findById(revokedCertificate.getId()).orElseThrow().getStatus())
                                .isEqualTo(CertificateStatus.REVOKED);
                assertThat(repository.findById(expiredCertificate.getId()).orElseThrow().getStatus())
                                .isEqualTo(CertificateStatus.EXPIRED);
        }
}
