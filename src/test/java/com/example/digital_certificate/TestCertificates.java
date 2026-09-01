package com.example.digital_certificate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public final class TestCertificates {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private TestCertificates() {
    }

    public static byte[] generateValidCert() {
        return generateValidCert(Instant.now());
    }

    public static byte[] generateValidCert(Instant referenceNow) {
        return generateCert("CN=example.ir, O=Example Company, C=IR",
                referenceNow.minus(1, ChronoUnit.DAYS),
                referenceNow.plus(365, ChronoUnit.DAYS));
    }

    public static byte[] generateExpiredCert() {
        return generateExpiredCert(Instant.now());
    }

    public static byte[] generateExpiredCert(Instant referenceNow) {
        return generateCert("CN=expired.ir, O=Example Company, C=IR",
                referenceNow.minus(400, ChronoUnit.DAYS),
                referenceNow.minus(1, ChronoUnit.DAYS));
    }

    public static byte[] generateNotYetValidCert() {
        return generateNotYetValidCert(Instant.now());
    }

    public static byte[] generateNotYetValidCert(Instant referenceNow) {
        return generateCert("CN=future.ir, O=Example Company, C=IR",
                referenceNow.plus(1, ChronoUnit.DAYS),
                referenceNow.plus(365, ChronoUnit.DAYS));
    }

    /**
     * Full control over the subject and validity window, for any case the
     * convenience methods above don't cover.
     */
    public static byte[] generateCert(String subjectDn, Instant validFrom, Instant validTo) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X500Name subject = new X500Name(subjectDn);
            BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis())
                    .multiply(BigInteger.valueOf((long) (Math.random() * 1_000_000)));

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject, // issuer == subject: self-signed
                    serialNumber,
                    Date.from(validFrom),
                    Date.from(validTo),
                    subject,
                    keyPair.getPublic());

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC")
                    .build(keyPair.getPrivate());

            X509CertificateHolder certHolder = certBuilder.build(signer);

            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certHolder);

            return certificate.getEncoded();
        } catch (NoSuchAlgorithmException | OperatorCreationException | java.security.cert.CertificateException e) {
            throw new RuntimeException("failed to generate test certificate", e);
        }
    }
}