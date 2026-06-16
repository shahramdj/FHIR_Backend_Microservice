package com.shahramdj.fhirbackendmicroservice.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
