package com.shahramdj.fhirbackendmicroservice.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "mock.fhir.enabled=true",
        "backend.api.enabled=false"
})
@AutoConfigureMockMvc
class MockFhirControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsMockPatient() throws Exception {
        mockMvc.perform(get("/fhir/Patient/pat-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("Patient"))
                .andExpect(jsonPath("$.id").value("pat-001"))
                .andExpect(jsonPath("$.name[0].family").value("Stone"));
    }

    @Test
    void returnsMockResourceBundleByPatient() throws Exception {
        mockMvc.perform(get("/fhir/Observation").queryParam("patient", "pat-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("Bundle"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.entry[0].resource.id").value("obs-002"))
                .andExpect(jsonPath("$.entry[0].resource.patient.reference").value("Patient/pat-002"));
    }

    @Test
    void returnsMediaImageAsJpeg() throws Exception {
        mockMvc.perform(get("/fhir/Media/media-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void returnsMediaMetadataWhenRequested() throws Exception {
        mockMvc.perform(get("/fhir/Media/media-002").queryParam("metadata", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("Media"))
                .andExpect(jsonPath("$.id").value("media-002"))
                .andExpect(jsonPath("$.patient.reference").value("Patient/pat-002"))
                .andExpect(jsonPath("$.modality.text").value("XRAY"))
                .andExpect(jsonPath("$.content.contentType").value("image/jpeg"));
    }

    @Test
    void returnsMockMediaBundleByPatient() throws Exception {
        mockMvc.perform(get("/fhir/Media").queryParam("patient", "pat-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("Bundle"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.entry[0].resource.id").value("media-003"));
    }

    @Test
    void returns404ForUnknownMedia() throws Exception {
        mockMvc.perform(get("/fhir/Media/media-999"))
                .andExpect(status().isNotFound());
    }
}
