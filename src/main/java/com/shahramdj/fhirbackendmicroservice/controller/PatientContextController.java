package com.shahramdj.fhirbackendmicroservice.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import com.shahramdj.fhirbackendmicroservice.service.FhirPatientContextService;
import com.shahramdj.fhirbackendmicroservice.service.VoiceCommandRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "backend.api.enabled", havingValue = "true", matchIfMissing = true)
public class PatientContextController {

    private final FhirPatientContextService patientContextService;

    public PatientContextController(FhirPatientContextService patientContextService) {
        this.patientContextService = patientContextService;
    }

    @GetMapping("/patient/{id}/summary")
    public Map<String, Object> getPatientSummary(@PathVariable String id) {
        return patientContextService.getPatientSummary(id);
    }

    @GetMapping("/patient/{id}/allergies")
    public List<Map<String, Object>> getPatientAllergies(@PathVariable String id) {
        return patientContextService.getAllergies(id);
    }

    @GetMapping("/patient/{id}/medications")
    public List<Map<String, Object>> getPatientMedications(@PathVariable String id) {
        return patientContextService.getMedications(id);
    }

    @GetMapping("/patient/{id}/labs")
    public List<Map<String, Object>> getPatientLabs(@PathVariable String id) {
        return patientContextService.getRecentLabs(id);
    }

    @GetMapping("/patient/{id}/imaging")
    public List<Map<String, Object>> getPatientImaging(@PathVariable String id) {
        return patientContextService.getImaging(id);
    }

    @GetMapping("/patient-context/{patientId}")
    public Map<String, Object> getPatientContext(@PathVariable String patientId) {
        return patientContextService.getPatientContext(patientId);
    }

    @PostMapping("/voice/command")
    public Map<String, Object> handleVoiceCommand(@Valid @RequestBody VoiceCommandRequest request) {
        try {
            return patientContextService.executeVoiceCommand(request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
