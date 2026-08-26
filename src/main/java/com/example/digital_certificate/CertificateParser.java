package com.example.digital_certificate;

import java.security.cert.X509Certificate;
import java.time.Instant;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.example.digital_certificate.entity.DigitalCertificate;

public class CertificateParser {

    public DigitalCertificate parseCertificate(X509Certificate certificate) {

        String serialNumber = certificate.getSerialNumber().toString();
        String subject = certificate.getSubjectX500Principal().getName();
        String issuer = certificate.getIssuerX500Principal().getName();
        String commonName = getAttribute(subject, "CN");
        String organization = getAttribute(subject, "O");
        String country = getAttribute(subject, "C");
        Instant validFrom = certificate.getNotBefore().toInstant();
        Instant validTo = certificate.getNotAfter().toInstant();
        String signatureAlgorithm = certificate.getSigAlgName();
        String publicKeyAlgorithm = certificate.getPublicKey().getAlgorithm();

        DigitalCertificate digitalCertificate = new DigitalCertificate(
            serialNumber,
            subject,
            issuer,
            commonName,
            organization,
            country,
            validFrom,
            validTo,
            signatureAlgorithm,
            publicKeyAlgorithm,
            null,
            null
        );
        return digitalCertificate;
    }

    String getAttribute(String subject, String type) {
        try {
            LdapName ldapName = new LdapName(subject);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase(type)) {
                    return rdn.getValue().toString();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
