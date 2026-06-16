package com.shahramdj.fhirbackendmicroservice.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahramdj.fhirbackendmicroservice.service.FhirPatientContextService;
import com.shahramdj.fhirbackendmicroservice.service.VoiceCommandRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientContextController.class)
class PatientContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FhirPatientContextService patientContextService;

    @Test
    void returnsPatientSummary() throws Exception {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", "123");
        summary.put("age", 45);
        summary.put("sex", "male");
        summary.put("demographics", Map.of("name", "John Doe"));

        when(patientContextService.getPatientSummary("123")).thenReturn(summary);

        mockMvc.perform(get("/api/patient/123/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.age").value(45))
                .andExpect(jsonPath("$.demographics.name").value("John Doe"));
    }

    @Test
    void returnsPatientContextAggregation() throws Exception {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("patient", Map.of("id", "123"));
        context.put("allergies", List.of(Map.of("substance", "Latex")));
        context.put("conditions", List.of(Map.of("description", "Hypertension")));
        context.put("medications", List.of(Map.of("medication", "Insulin")));
        context.put("recentLabs", List.of(Map.of("test", "Hemoglobin")));
        context.put("imaging", List.of(Map.of("description", "CT Chest")));
        context.put("procedures", List.of(Map.of("description", "Appendectomy")));

        when(patientContextService.getPatientContext("123")).thenReturn(context);

        mockMvc.perform(get("/api/patient-context/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.id").value("123"))
                .andExpect(jsonPath("$.allergies[0].substance").value("Latex"))
                .andExpect(jsonPath("$.procedures[0].description").value("Appendectomy"));
    }

    @Test
    void routesVoiceCommandRequests() throws Exception {
        Map<String, Object> response = Map.of(
                "patientId", "123",
                "resource", "allergies",
                "data", List.of(Map.of("substance", "Latex")));

        when(patientContextService.executeVoiceCommand(any(VoiceCommandRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/voice/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "patientId", "123",
                                "command", "show allergies"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resource").value("allergies"))
                .andExpect(jsonPath("$.data[0].substance").value("Latex"));

        verify(patientContextService).executeVoiceCommand(new VoiceCommandRequest("123", "show allergies"));
    }

    @Test
    void rejectsUnsupportedVoiceCommands() throws Exception {
        when(patientContextService.executeVoiceCommand(any(VoiceCommandRequest.class)))
                .thenThrow(new IllegalArgumentException("Unsupported voice command"));

        mockMvc.perform(post("/api/voice/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "patientId", "123",
                                "command", "do something else"))))
                .andExpect(status().isBadRequest());
    }
}
