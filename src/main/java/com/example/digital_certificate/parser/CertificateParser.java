package com.example.digital_certificate.parser;


import java.io.InputStream;

import com.example.digital_certificate.entity.DigitalCertificate;

public interface CertificateParser {
    public DigitalCertificate parseCertificate(InputStream inputStream); 
}
