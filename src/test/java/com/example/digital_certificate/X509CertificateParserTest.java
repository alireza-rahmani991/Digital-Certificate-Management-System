package com.example.digital_certificate;

import java.io.InputStream;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.exception.InvalidCertificateException;
import com.example.digital_certificate.parser.CertificateParser;
import com.example.digital_certificate.parser.X509CertificateParser;
import com.example.digital_certificate.validation.CertificateValidityChecker;

@ExtendWith(MockitoExtension.class)
public class X509CertificateParserTest {

    @Mock
    private CertificateValidityChecker checker;
    private CertificateParser parser;

    @BeforeEach
    void setUp() {

        parser = new X509CertificateParser(checker);
    }

    @Test
    void shouldParseRSACertificate() {
        when(checker.checkValidity(any(DigitalCertificate.class))).thenReturn(CertificateStatus.VALID);
        InputStream input = getClass().getResourceAsStream("/certificates/rsa-test-cert.pem");
        assertNotNull(input);
        DigitalCertificate result = parser.parseCertificate(input);

        assertNotNull(result);

        assertEquals("Test Certificate", result.getCommonName());
        assertEquals("Example Corp", result.getOrganization());
        assertEquals("US", result.getCountry());

        assertEquals("RSA", result.getPublicKeyAlgorithm());
        assertEquals(2048, result.getPublicKeySize());

        assertNotNull(result.getSerialNumber());
        assertNotNull(result.getSubject());
        assertNotNull(result.getIssuer());
        assertEquals(Instant.parse("2026-08-29T20:05:10Z"), result.getValidFrom());
        assertEquals(Instant.parse("2036-08-26T20:05:10Z"), result.getValidTo());
        assertNotNull(result.getSignatureAlgorithm());
        assertEquals("4F:0C:6B:0A:14:8E:C4:DA:12:93:6E:5C:22:8C:E9:5F:A8:5D:59:6E:83:2D:19:46:24:DB:38:49:B7:17:62:23",
                result.getFingerprint());

        assertEquals(CertificateStatus.VALID, result.getStatus());

        verify(checker).checkValidity(any(DigitalCertificate.class));
    }

    @Test
    void shouldParseECCertificate() {
        when(checker.checkValidity(any(DigitalCertificate.class))).thenReturn(CertificateStatus.VALID);
        InputStream input = getClass().getResourceAsStream("/certificates/ec-test-cert.pem");
        assertNotNull(input);
        DigitalCertificate result = parser.parseCertificate(input);

        assertNotNull(result);

        assertEquals("Test Certificate", result.getCommonName());
        assertEquals("Example Corp", result.getOrganization());
        assertEquals("US", result.getCountry());

        assertEquals("EC", result.getPublicKeyAlgorithm());
        assertEquals(256, result.getPublicKeySize());

        assertNotNull(result.getSerialNumber());
        assertNotNull(result.getSubject());
        assertNotNull(result.getIssuer());
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getSignatureAlgorithm());
        assertEquals("55:C6:33:64:34:BC:EC:D5:68:FC:2B:DB:42:25:C1:05:39:96:35:A2:48:DB:A7:92:FE:DD:07:D5:82:81:39:A2",
                result.getFingerprint());

        assertEquals(CertificateStatus.VALID, result.getStatus());

        verify(checker).checkValidity(any(DigitalCertificate.class));
    }

    @Test
    void shouldHandleMissingCommonName() {
        when(checker.checkValidity(any(DigitalCertificate.class))).thenReturn(CertificateStatus.VALID);
        InputStream input = getClass().getResourceAsStream("/certificates/rsa-no-cn-cert.pem");
        assertNotNull(input);
        DigitalCertificate result = parser.parseCertificate(input);

        assertNotNull(result);

        assertNull(result.getCommonName());
        assertEquals("Example Corp", result.getOrganization());
        assertEquals("US", result.getCountry());

        assertEquals("RSA", result.getPublicKeyAlgorithm());
        assertEquals(2048, result.getPublicKeySize());

        assertNotNull(result.getSerialNumber());
        assertNotNull(result.getSubject());
        assertNotNull(result.getIssuer());
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getSignatureAlgorithm());
        assertEquals("6D:B5:7E:B6:D1:0A:AB:0A:8A:16:C2:A1:DE:72:0F:C0:42:05:A0:91:CC:F0:78:A4:9D:92:19:3C:F3:F1:BB:BC",
                result.getFingerprint());

        assertEquals(CertificateStatus.VALID, result.getStatus());

        verify(checker).checkValidity(any(DigitalCertificate.class));
    }

    @Test
    void shouldHandleMissingOrganizationAndCountry() {
        when(checker.checkValidity(any(DigitalCertificate.class))).thenReturn(CertificateStatus.VALID);
        InputStream input = getClass().getResourceAsStream("/certificates/rsa-no-org-country-cert.pem");
        assertNotNull(input);
        DigitalCertificate result = parser.parseCertificate(input);

        assertNotNull(result);

        assertEquals("Test Certificate", result.getCommonName());
        assertNull(result.getOrganization());
        assertNull(result.getCountry());

        assertEquals("RSA", result.getPublicKeyAlgorithm());
        assertEquals(2048, result.getPublicKeySize());

        assertNotNull(result.getSerialNumber());
        assertNotNull(result.getSubject());
        assertNotNull(result.getIssuer());
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getSignatureAlgorithm());
        assertEquals("4C:67:72:86:C6:12:68:B2:59:D1:11:56:76:E3:4C:F1:18:79:B0:09:89:5B:D1:B1:C8:0A:8A:F6:52:CE:92:02",
                result.getFingerprint());

        assertEquals(CertificateStatus.VALID, result.getStatus());

        verify(checker).checkValidity(any(DigitalCertificate.class));
    }

    @Test
    void shouldRejectInvalidCertificate() {

        InputStream input = getClass()
                .getResourceAsStream("/certificates/invalid-certificate.cer");

        assertNotNull(input);

        assertThrows(
                InvalidCertificateException.class,
                () -> parser.parseCertificate(input));

        verifyNoInteractions(checker);
    }

    @Test
    void shouldSetCertificateStatusReturnedByChecker() {

        when(checker.checkValidity(any(DigitalCertificate.class)))
                .thenReturn(CertificateStatus.EXPIRED);

        InputStream input = getClass()
                .getResourceAsStream("/certificates/rsa-test-cert.pem");

        assertNotNull(input);

        DigitalCertificate result = parser.parseCertificate(input);

        assertNotNull(result);
        assertEquals(CertificateStatus.EXPIRED, result.getStatus());

        verify(checker).checkValidity(any(DigitalCertificate.class));
    }
}
