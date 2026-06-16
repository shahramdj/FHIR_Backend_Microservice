package com.shahramdj.fhirbackendmicroservice.mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/fhir")
@ConditionalOnProperty(name = "mock.fhir.enabled", havingValue = "true")
public class MockFhirController {

    private final MockFhirDataRepository mockFhirDataRepository;

    public MockFhirController(MockFhirDataRepository mockFhirDataRepository) {
        this.mockFhirDataRepository = mockFhirDataRepository;
    }

    @GetMapping("/Patient/{id}")
    public Map<String, Object> getPatient(@PathVariable String id) {
        Map<String, Object> patient = mockFhirDataRepository.findPatientById(id);
        if (patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        }
        return patient;
    }

    @GetMapping("/{resourceType}")
    public Map<String, Object> searchResourceByPatient(
            @PathVariable String resourceType,
            @RequestParam("patient") String patientId) {
        List<Map<String, Object>> resources = mockFhirDataRepository.findResourcesByPatient(resourceType, patientId);
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "searchset");
        bundle.put("total", resources.size());
        bundle.put("entry", resources.stream().map(resource -> Map.of("resource", resource)).toList());
        return bundle;
    }
}
