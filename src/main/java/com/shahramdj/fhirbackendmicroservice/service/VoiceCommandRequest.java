package com.shahramdj.fhirbackendmicroservice.service;

import jakarta.validation.constraints.NotBlank;

public record VoiceCommandRequest(
        @NotBlank String patientId,
        @NotBlank String command) {
}
