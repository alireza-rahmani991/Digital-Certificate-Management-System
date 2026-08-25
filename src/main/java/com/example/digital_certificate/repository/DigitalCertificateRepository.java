package com.example.digital_certificate.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.digital_certificate.entity.DigitalCertificate;

public interface DigitalCertificateRepository extends JpaRepository<DigitalCertificate,UUID>{
    
}
