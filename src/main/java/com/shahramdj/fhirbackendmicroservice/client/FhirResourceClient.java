package com.shahramdj.fhirbackendmicroservice.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FhirResourceClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    public FhirResourceClient(
            RestClient.Builder restClientBuilder,
            @Value("${fhir.base-url:http://localhost:8081/fhir}") String fhirBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(fhirBaseUrl).build();
    }

    public Map<String, Object> getPatient(String patientId) {
        return restClient.get()
                .uri("/Patient/{patientId}", patientId)
                .retrieve()
                .body(MAP_RESPONSE);
    }

    public Map<String, Object> searchByPatient(String resourceType, String patientId) {
        return restClient.get()
                .uri("/{resourceType}?patient={patientId}", resourceType, patientId)
                .retrieve()
                .body(MAP_RESPONSE);
    }
}
