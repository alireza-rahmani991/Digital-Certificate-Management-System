package com.example.digital_certificate;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_certificate.DTO.CertificatesStatisticsDTO;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.exception.CertificateDoesNotExistException;
import com.example.digital_certificate.exception.CertificateProcessingException;
import com.example.digital_certificate.exception.DuplicateCertificateException;
import com.example.digital_certificate.parser.CertificateParser;
import com.example.digital_certificate.repository.DigitalCertificateRepository;
import com.example.digital_certificate.service.CertificateService;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {
    @Mock
    private DigitalCertificateRepository repository;

    @Mock
    private CertificateParser parser;

    private Clock clock;

    private CertificateService service;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        this.clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new CertificateService(repository, parser, clock);

    }

    @Test
    void shouldSaveCertificate() throws Exception {
        // arragne
        MultipartFile file = mock(MultipartFile.class);

        InputStream inputStream = mock(InputStream.class);
        DigitalCertificate certificate = DigitalCertificate.builder().serialNumber("123").fingerprint("test").build();

        when(file.getInputStream()).thenReturn(inputStream);

        when(parser.parseCertificate(inputStream)).thenReturn(certificate);

        when(repository.existsByFingerprint("test")).thenReturn(false);

        when(repository.save(certificate)).thenReturn(certificate);

        // act
        DigitalCertificate result = service.saveCertificate(file);

        // assert
        assertSame(certificate, result);
        verify(file).getInputStream();
        verify(parser).parseCertificate(inputStream);
        verify(repository).existsByFingerprint("test");
        verify(repository).save(certificate);

    }

    @Test
    void shouldHandleDuplicateCertificate() throws Exception {
        // arragne
        MultipartFile file = mock(MultipartFile.class);

        InputStream inputStream = mock(InputStream.class);
        DigitalCertificate certificate = DigitalCertificate.builder().serialNumber("123").fingerprint("test").build();

        when(file.getInputStream()).thenReturn(inputStream);

        when(parser.parseCertificate(inputStream)).thenReturn(certificate);

        when(repository.existsByFingerprint("test")).thenReturn(true);

        // act
        assertThrows(DuplicateCertificateException.class, () -> service.saveCertificate(file));

        // assert
        verify(repository, never()).save(any());

    }

    @Test
    void shouldThrowExceptionWhenCannotReadFile() throws Exception {
        // arrange
        MultipartFile file = mock(MultipartFile.class);

        when(file.getInputStream()).thenThrow(new IOException("failed to read certificate file"));

        // act
        assertThrows(CertificateProcessingException.class, () -> service.saveCertificate(file));

        // assert
        verifyNoInteractions(parser);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRevokeCertificate() throws Exception {
        // arragne
        UUID uuid = UUID.randomUUID();
        DigitalCertificate certificate = DigitalCertificate.builder().status(CertificateStatus.VALID).build();

        when(repository.findById(uuid)).thenReturn(Optional.of(certificate));

        when(repository.save(certificate)).thenReturn(certificate);

        // act
        DigitalCertificate result = service.revokeCertificate(uuid);

        // assert
        assertEquals(CertificateStatus.REVOKED, result.getStatus());
        verify(repository).findById(uuid);
        verify(repository).save(certificate);

    }

    @Test
    void shouldThrowCertificateNotFoundWhenRevokingNonExistingCertificate() throws Exception {
        // arragne
        UUID uuid = UUID.randomUUID();

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        // act
        assertThrows(CertificateDoesNotExistException.class, () -> service.revokeCertificate(uuid));

        // assert
        verify(repository).findById(uuid);
        verify(repository, never()).save(any());

    }

    @Test
    void shouldFindCertificateById() {
        // arragne
        UUID uuid = UUID.randomUUID();
        DigitalCertificate certificate = DigitalCertificate.builder().build();

        when(repository.findById(uuid)).thenReturn(Optional.of(certificate));

        // act
        DigitalCertificate result = service.findCertificateById(uuid);

        // assert
        assertSame(certificate, result);
        verify(repository).findById(uuid);
    }

    @Test
    void shouldThrowCertificateNotFoundWhenSearchingForNonExistingCertificate() {
        // arragne
        UUID uuid = UUID.randomUUID();

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        // act
        assertThrows(CertificateDoesNotExistException.class, () -> service.findCertificateById(uuid));
    }

    @Test
    void shouldDeleteCertificate() {
        // arrange
        UUID id = UUID.randomUUID();

        when(repository.existsById(id))
                .thenReturn(true);
        // act
        service.deleteCertificateById(id);

        // assert
        verify(repository).existsById(id);
        verify(repository).deleteById(id);
    }

    @Test
    void shouldThrowCertificateNotFoundWhenDeletingNonExistingCertificate() {
        // arrange
        UUID id = UUID.randomUUID();

        when(repository.existsById(id))
                .thenReturn(false);

        // act
        assertThrows(CertificateDoesNotExistException.class, () -> service.deleteCertificateById(id));

        // assert
        verify(repository, never()).deleteById(any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -10 })
    void shouldRejectInvalidDays(int days) {

        Pageable pageable = Pageable.unpaged();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findbyExpiring(pageable, days));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldFindExpiringCertificates() {
        // arrange
        Pageable pageable = Pageable.unpaged();

        Page<DigitalCertificate> expected = Page.empty();

        when(repository.findByStatusAndValidToBetween(eq(CertificateStatus.VALID), eq(NOW),
                eq(NOW.plus(30, ChronoUnit.DAYS)), eq(pageable))).thenReturn(expected);

        // act
        Page<DigitalCertificate> result = service.findbyExpiring(pageable, 30);

        // assert
        assertSame(expected, result);
        verify(repository).findByStatusAndValidToBetween(
                CertificateStatus.VALID,
                NOW,
                NOW.plus(30, ChronoUnit.DAYS),
                pageable);
    }

    @Test
    void shouldReturnCertificateStatistics() {
        // arrange
        when(repository.count()).thenReturn(500L);
        when(repository.countByStatus(CertificateStatus.VALID)).thenReturn(100L);
        when(repository.countByStatus(CertificateStatus.EXPIRED)).thenReturn(200L);
        when(repository.countByStatus(CertificateStatus.REVOKED)).thenReturn(100L);
        when(repository.countByStatus(CertificateStatus.NOT_YET_VALID)).thenReturn(100L);
        when(repository.countByStatusAndValidToBetween(
                eq(CertificateStatus.VALID),
                eq(NOW),
                eq(NOW.plus(30, ChronoUnit.DAYS)))).thenReturn(30L);
        // act
        CertificatesStatisticsDTO result = service.getStatistics();
        
        // assert
        assertEquals(500L, result.total());
        assertEquals(100L, result.valid());
        assertEquals(200L, result.expired());
        assertEquals(100L, result.revoked());
        assertEquals(100L, result.notYetValid());
        assertEquals(30L, result.expiringIn30Days());

    }
}
