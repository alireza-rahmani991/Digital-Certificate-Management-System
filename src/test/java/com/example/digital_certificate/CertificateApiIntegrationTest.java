package com.example.digital_certificate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.digital_certificate.configuration.FixedClockTestConfig;

import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Import(FixedClockTestConfig.class)
@Transactional
public class CertificateApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldHandleCertificatePostGetRevokeStatisticsDelete() throws Exception {
        byte[] certificateBytes = TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW);
        MockMultipartFile file = new MockMultipartFile("certificate", "cert.cer", "application/octet-stream",
                certificateBytes);

        // post certificate
        String postResponse = mockMvc.perform(multipart("/api/certificates").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("VALID"))
                .andReturn().getResponse().getContentAsString();

        UUID uuid = UUID.fromString(jsonMapper.readTree(postResponse).get("id").asString());

        // get certificate
        mockMvc.perform(get("/api/certificates/{id}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"));

        // revoke certificate

        mockMvc.perform(patch("/api/certificates/{id}/revoke", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        // get statistics
        mockMvc.perform(get("/api/certificates/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revoked").value(1));

        // delete certificate
        mockMvc.perform(delete("/api/certificates/{id}", uuid))
                .andExpect(status().isNoContent());

        // confirm deletion
        mockMvc.perform(get("/api/certificates/{id}", uuid))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/certificates/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void shouldFailToSaveDuplicateCertificate() throws Exception {
        byte[] certificateBytes = TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW);
        MockMultipartFile file1 = new MockMultipartFile("certificate", "cert.cer", "application/octet-stream",
                certificateBytes);
        MockMultipartFile file2 = new MockMultipartFile("certificate", "cert2.cer", "application/octet-stream",
                certificateBytes);

        // post certificate
        mockMvc.perform(multipart("/api/certificates").file(file1))
                .andExpect(status().isCreated());

        // post duplicate certificate
        mockMvc.perform(multipart("/api/certificates").file(file2))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldHandleSearchFiltering() throws Exception {
        String postResponse = mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert.cer", "application/octet-stream",
                TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String serialNumber = jsonMapper.readTree(postResponse).get("serialNumber").asString();

        mockMvc.perform(get("/api/certificates").param("status", "VALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/certificates").param("serialNumber", serialNumber.substring(3, 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldHandleExpiringCertificates() throws Exception {
        mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert.cer", "application/octet-stream",
                TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/certificates/expiring").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(get("/api/certificates/expiring").param("days", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldHandleMultipleCertificates() throws Exception {
        mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert1.cer", "application/octet-stream",
                TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert1.cer", "application/octet-stream",
                TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert1.cer", "application/octet-stream",
                TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert2.cer", "application/octet-stream",
                TestCertificates.generateExpiredCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/certificates").file(new MockMultipartFile(
                "certificate", "cert3.cer", "application/octet-stream",
                TestCertificates.generateNotYetValidCert(FixedClockTestConfig.FIXED_NOW))))
                .andExpect(status().isCreated());

        byte[] certificateBytes = TestCertificates.generateValidCert(FixedClockTestConfig.FIXED_NOW);
        MockMultipartFile file = new MockMultipartFile("certificate", "cert.cer", "application/octet-stream",
                certificateBytes);

        String postResponse = mockMvc.perform(multipart("/api/certificates").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("VALID"))
                .andReturn().getResponse().getContentAsString();

        UUID uuid = UUID.fromString(jsonMapper.readTree(postResponse).get("id").asString());

        mockMvc.perform(patch("/api/certificates/{id}/revoke", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        mockMvc.perform(get("/api/certificates/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.revoked").value(1))
                .andExpect(jsonPath("$.notYetValid").value(1))
                .andExpect(jsonPath("$.expired").value(1))
                .andExpect(jsonPath("$.valid").value(3));
    }
}
