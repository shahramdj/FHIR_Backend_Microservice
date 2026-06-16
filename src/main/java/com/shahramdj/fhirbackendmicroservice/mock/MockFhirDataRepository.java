package com.shahramdj.fhirbackendmicroservice.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class MockFhirDataRepository {

    private static final TypeReference<Map<String, List<Map<String, Object>>>> MOCK_DATA_TYPE = new TypeReference<>() {
    };
    private static final List<String> SUPPORTED_RESOURCE_TYPES = List.of(
            "AllergyIntolerance",
            "Condition",
            "MedicationRequest",
            "Observation",
            "DiagnosticReport",
            "ImagingStudy",
            "DocumentReference",
            "Encounter",
            "Procedure");

    private final Map<String, List<Map<String, Object>>> data;

    public MockFhirDataRepository(ObjectMapper objectMapper) {
        this.data = loadData(objectMapper);
    }

    public Map<String, Object> findPatientById(String patientId) {
        return data.getOrDefault("patients", List.of()).stream()
                .filter(resource -> patientId.equals(resource.get("id")))
                .findFirst()
                .orElse(null);
    }

    public List<Map<String, Object>> findResourcesByPatient(String resourceType, String patientId) {
        if (!SUPPORTED_RESOURCE_TYPES.contains(resourceType)) {
            return List.of();
        }

        String key = keyForResourceType(resourceType);
        return data.getOrDefault(key, List.of()).stream()
                .filter(resource -> patientId.equals(patientIdFromResource(resource)))
                .toList();
    }

    private String keyForResourceType(String resourceType) {
        return switch (resourceType) {
            case "AllergyIntolerance" -> "allergyIntolerances";
            case "Condition" -> "conditions";
            case "MedicationRequest" -> "medicationRequests";
            case "Observation" -> "observations";
            case "DiagnosticReport" -> "diagnosticReports";
            case "ImagingStudy" -> "imagingStudies";
            case "DocumentReference" -> "documentReferences";
            case "Encounter" -> "encounters";
            case "Procedure" -> "procedures";
            default -> "";
        };
    }

    private String patientIdFromResource(Map<String, Object> resource) {
        Object subjectValue = resource.get("patient");
        if (!(subjectValue instanceof Map<?, ?> patientReference)) {
            return null;
        }

        Object referenceValue = patientReference.get("reference");
        if (!(referenceValue instanceof String reference) || reference.isBlank()) {
            return null;
        }

        String[] parts = reference.split("/");
        return parts.length < 2 ? null : parts[parts.length - 1];
    }

    private Map<String, List<Map<String, Object>>> loadData(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource("mock-fhir/patient-data.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, MOCK_DATA_TYPE);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load mock FHIR data", exception);
        }
    }
}
