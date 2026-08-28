package com.example.digital_certificate.parser;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Random;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.springframework.stereotype.Component;

import com.example.digital_certificate.entity.DigitalCertificate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class X509CertificateParser implements CertificateParser {

    @Override
    public DigitalCertificate parseCertificate(InputStream inputStream) {
        try{
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            log.debug("certificate created successfully : serial-number={}", certificate.getSerialNumber());

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
        Integer publicKeySize = getPublicKeySize(certificate);
        Random random = new Random();
        return DigitalCertificate.builder()
                .serialNumber(serialNumber)
                .subject(subject)
                .issuer(issuer)
                .commonName(commonName)
                .organization(organization)
                .country(country)
                .validFrom(validFrom)
                .validTo(validTo)
                .signatureAlgorithm(signatureAlgorithm)
                .publicKeyAlgorithm(publicKeyAlgorithm)
                .publicKeySize(publicKeySize)
                .fingerprint(String.valueOf(random.nextInt(100000)))
                .build();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
        
    }

    private String getAttribute(String subject, String type) {
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

    private Integer getPublicKeySize(X509Certificate certificate) {
        PublicKey publicKey = certificate.getPublicKey();

        if (publicKey instanceof RSAPublicKey rsaKey) {
            return rsaKey.getModulus().bitLength();
        } else if (publicKey instanceof ECPublicKey ecKey) {
            return ecKey.getParams().getOrder().bitLength();
        }
        return null;
    }
}
