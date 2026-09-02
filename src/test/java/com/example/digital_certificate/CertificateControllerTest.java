package com.example.digital_certificate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.digital_certificate.DTO.CertificatesStatisticsDTO;
import com.example.digital_certificate.controller.CertificateController;
import com.example.digital_certificate.entity.CertificateStatus;
import com.example.digital_certificate.entity.DigitalCertificate;
import com.example.digital_certificate.exception.CertificateDoesNotExistException;
import com.example.digital_certificate.exception.DuplicateCertificateException;
import com.example.digital_certificate.service.CertificateService;

@WebMvcTest(CertificateController.class)
public class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CertificateService certificateService;

    @Test
    void shouldSaveAndReturn201WhenValidCertificateIsPosted() throws Exception {
        DigitalCertificate saved = DigitalCertificate.builder().serialNumber("123").build();
        when(certificateService.saveCertificate(any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile("certificate", "cert.cer", "application/octet-stream",
                new byte[] { 1, 2, 3 });

        mockMvc.perform(multipart("/api/certificates").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("123"));
    }

    @Test
    void shouldReturn400WhenRequestIsNotMultipart() throws Exception {
        mockMvc.perform(multipart("/api/certificates"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenPostingDuplicateCertificate() throws Exception {
        when(certificateService.saveCertificate(any()))
                .thenThrow(new DuplicateCertificateException("duplicate certificate"));

        MockMultipartFile file = new MockMultipartFile("certificate", "cert.cer", "application/octet-stream",
                new byte[] { 1, 2, 3 });
        mockMvc.perform(multipart("/api/certificates").file(file))
                .andExpect(status().isConflict())
                .andExpect(content().string("duplicate certificate"));
    }

    @Test
    void shouldReturn200GettingCertificatesWithNoParam() throws Exception {
        when(certificateService.search(any(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/certificates")).andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenValidFromIsAfterValidTo() throws Exception {
        mockMvc.perform(get("/api/certificates")
                .param("validFrom", "2027-01-01T00:00:00Z")
                .param("validTo", "2026-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenSerialNumberIsTooLong() throws Exception {
        mockMvc.perform(get("/api/certificates").param("serialNumber", "1".repeat(201)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenGettingCertificateById() throws Exception {
        DigitalCertificate certificate = DigitalCertificate.builder().serialNumber("123").build();
        when(certificateService.findCertificateById(any())).thenReturn(certificate);

        UUID uuid = UUID.randomUUID();
        mockMvc.perform(get("/api/certificates/{id}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("123"));
    }

    @Test
    void shouldReturn400WhenGettingCertificateByInvalidId() throws Exception {
        mockMvc.perform(get("/api/certificates/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenGettingCertificateByNonExistingId() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(certificateService.findCertificateById(uuid)).thenThrow(new CertificateDoesNotExistException("not found"));
        mockMvc.perform(get("/api/certificates/{id}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenDeletingCertificateById() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(delete("/api/certificates/{id}", uuid))
                .andExpect(status().isNoContent());
        verify(certificateService).deleteCertificateById(uuid);
    }

    @Test
    void shouldReturn400WhenDeletingNotfoundCertificate() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new CertificateDoesNotExistException("not found")).when(certificateService).deleteCertificateById(uuid);
        mockMvc.perform(delete("/api/certificates/{id}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WhenRevokingCertificateById() throws Exception {
        DigitalCertificate certificate = DigitalCertificate.builder().status(CertificateStatus.REVOKED).build();
        when(certificateService.revokeCertificate(any())).thenReturn(certificate);
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(patch("/api/certificates/{id}/revoke", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
    }

    @Test
    void getExpiring_negativeDays_returns400() throws Exception {
        when(certificateService.findbyExpiring(any(), eq(-1)))
                .thenThrow(new IllegalArgumentException("Invalid days value: -1. Days must be greater than 0"));
        mockMvc.perform(get("/api/certificates/expiring").param("days", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatistics_returnsServiceResult() throws Exception {
        when(certificateService.getStatistics()).thenReturn(new CertificatesStatisticsDTO(10, 5, 2, 1, 2, 3));
        mockMvc.perform(get("/api/certificates/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));
    }
}
