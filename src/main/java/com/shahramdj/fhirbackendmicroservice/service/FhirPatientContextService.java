package com.shahramdj.fhirbackendmicroservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.shahramdj.fhirbackendmicroservice.client.FhirResourceClient;

import org.springframework.stereotype.Service;

@Service
public class FhirPatientContextService {

    private final FhirResourceClient fhirResourceClient;

    public FhirPatientContextService(FhirResourceClient fhirResourceClient) {
        this.fhirResourceClient = fhirResourceClient;
    }

    public Map<String, Object> getPatientSummary(String patientId) {
        return toPatientSummary(fhirResourceClient.getPatient(patientId));
    }

    public List<Map<String, Object>> getAllergies(String patientId) {
        return bundleResources(fhirResourceClient.searchByPatient("AllergyIntolerance", patientId)).stream()
                .map(this::toAllergy)
                .toList();
    }

    public List<Map<String, Object>> getConditions(String patientId) {
        return bundleResources(fhirResourceClient.searchByPatient("Condition", patientId)).stream()
                .map(this::toCondition)
                .toList();
    }

    public List<Map<String, Object>> getMedications(String patientId) {
        return bundleResources(fhirResourceClient.searchByPatient("MedicationRequest", patientId)).stream()
                .map(this::toMedication)
                .toList();
    }

    public List<Map<String, Object>> getRecentLabs(String patientId) {
        return bundleResources(fhirResourceClient.searchByPatient("Observation", patientId)).stream()
                .map(this::toLab)
                .sorted(Comparator.comparingLong(
                        (Map<String, Object> resource) -> temporalSortKey(stringValue(resource.get("issued"))))
                        .reversed())
                .limit(10)
                .toList();
    }

    public List<Map<String, Object>> getImaging(String patientId) {
        return bundleResources(fhirResourceClient.searchByPatient("ImagingStudy", patientId)).stream()
                .map(this::toImaging)
                .toList();
    }

    public List<Map<String, Object>> getProcedures(String patientId) {
        return bundleResources(fhirResourceClient.searchByPatient("Procedure", patientId)).stream()
                .map(this::toProcedure)
                .toList();
    }

    public Map<String, Object> getPatientContext(String patientId) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("patient", getPatientSummary(patientId));
        context.put("allergies", getAllergies(patientId));
        context.put("conditions", getConditions(patientId));
        context.put("medications", getMedications(patientId));
        context.put("recentLabs", getRecentLabs(patientId));
        context.put("imaging", getImaging(patientId));
        context.put("procedures", getProcedures(patientId));
        return context;
    }

    public Map<String, Object> executeVoiceCommand(VoiceCommandRequest request) {
        String command = request.command().toLowerCase(Locale.ROOT);

        if (command.contains("allerg")) {
            return voiceResponse(request.patientId(), "allergies", getAllergies(request.patientId()));
        }
        if (command.contains("med")) {
            return voiceResponse(request.patientId(), "medications", getMedications(request.patientId()));
        }
        if (command.contains("lab") || command.contains("observation")) {
            return voiceResponse(request.patientId(), "labs", getRecentLabs(request.patientId()));
        }
        if (command.contains("imag") || command.contains("xray") || command.contains("x-ray")
                || command.contains("mri") || command.contains("ct")) {
            return voiceResponse(request.patientId(), "imaging", getImaging(request.patientId()));
        }
        if (command.contains("condition") || command.contains("history")) {
            return voiceResponse(request.patientId(), "conditions", getConditions(request.patientId()));
        }
        if (command.contains("procedure") || command.contains("surger")) {
            return voiceResponse(request.patientId(), "procedures", getProcedures(request.patientId()));
        }
        if (command.contains("context") || command.contains("everything")) {
            return voiceResponse(request.patientId(), "patient-context", getPatientContext(request.patientId()));
        }
        if (command.contains("summary") || command.contains("demographic") || command.contains("patient")) {
            return voiceResponse(request.patientId(), "summary", getPatientSummary(request.patientId()));
        }

        throw new IllegalArgumentException("Unsupported voice command");
    }

    private Map<String, Object> voiceResponse(String patientId, String resource, Object data) {
        return linkedMap(
                "patientId", patientId,
                "resource", resource,
                "data", data);
    }

    private Map<String, Object> toPatientSummary(Map<String, Object> patient) {
        return linkedMap(
                "id", stringValue(patient.get("id")),
                "demographics", linkedMap(
                        "name", primaryName(patient),
                        "birthDate", stringValue(patient.get("birthDate")),
                        "gender", stringValue(patient.get("gender"))),
                "identifiers", identifiers(patient),
                "age", calculateAge(stringValue(patient.get("birthDate"))),
                "sex", stringValue(patient.get("gender")));
    }

    private Map<String, Object> toAllergy(Map<String, Object> resource) {
        return linkedMap(
                "id", stringValue(resource.get("id")),
                "substance", codeableConceptText(getMap(resource, "code")),
                "criticality", stringValue(resource.get("criticality")),
                "clinicalStatus", codeableConceptText(getMap(resource, "clinicalStatus")),
                "verificationStatus", codeableConceptText(getMap(resource, "verificationStatus")));
    }

    private Map<String, Object> toCondition(Map<String, Object> resource) {
        return linkedMap(
                "id", stringValue(resource.get("id")),
                "description", codeableConceptText(getMap(resource, "code")),
                "clinicalStatus", codeableConceptText(getMap(resource, "clinicalStatus")),
                "verificationStatus", codeableConceptText(getMap(resource, "verificationStatus")));
    }

    private Map<String, Object> toMedication(Map<String, Object> resource) {
        String medication = codeableConceptText(getMap(resource, "medicationCodeableConcept"));
        if (medication == null) {
            medication = referenceDisplay(getMap(resource, "medicationReference"));
        }

        return linkedMap(
                "id", stringValue(resource.get("id")),
                "medication", medication,
                "status", stringValue(resource.get("status")),
                "authoredOn", stringValue(resource.get("authoredOn")));
    }

    private Map<String, Object> toLab(Map<String, Object> resource) {
        Map<String, Object> valueQuantity = getMap(resource, "valueQuantity");
        String issued = firstNonBlank(
                stringValue(resource.get("effectiveDateTime")),
                stringValue(resource.get("issued")),
                stringValue(getMap(resource, "effectivePeriod").get("start")));

        return linkedMap(
                "id", stringValue(resource.get("id")),
                "test", codeableConceptText(getMap(resource, "code")),
                "value", firstNonBlank(
                        stringValue(valueQuantity.get("value")),
                        stringValue(resource.get("valueString")),
                        codeableConceptText(getMap(resource, "valueCodeableConcept"))),
                "unit", stringValue(valueQuantity.get("unit")),
                "status", stringValue(resource.get("status")),
                "issued", issued);
    }

    private Map<String, Object> toImaging(Map<String, Object> resource) {
        Map<String, Object> firstSeries = firstMap(getList(resource, "series"));
        return linkedMap(
                "id", stringValue(resource.get("id")),
                "description", firstNonBlank(
                        stringValue(resource.get("description")),
                        stringValue(firstSeries.get("description")),
                        codeableConceptText(getMap(resource, "procedureCode"))),
                "status", stringValue(resource.get("status")),
                "started", stringValue(resource.get("started")),
                "modality", codeableConceptText(getMap(firstSeries, "modality")));
    }

    private Map<String, Object> toProcedure(Map<String, Object> resource) {
        return linkedMap(
                "id", stringValue(resource.get("id")),
                "description", codeableConceptText(getMap(resource, "code")),
                "status", stringValue(resource.get("status")),
                "performed", firstNonBlank(
                        stringValue(resource.get("performedDateTime")),
                        stringValue(getMap(resource, "performedPeriod").get("start"))));
    }

    private List<Map<String, Object>> identifiers(Map<String, Object> patient) {
        List<Map<String, Object>> identifiers = new ArrayList<>();
        for (Object identifierValue : getList(patient, "identifier")) {
            Map<String, Object> identifier = asMap(identifierValue);
            identifiers.add(linkedMap(
                    "system", stringValue(identifier.get("system")),
                    "value", stringValue(identifier.get("value"))));
        }
        return identifiers;
    }

    private String primaryName(Map<String, Object> patient) {
        Map<String, Object> name = firstMap(getList(patient, "name"));
        List<String> parts = new ArrayList<>();
        for (Object given : getList(name, "given")) {
            String value = stringValue(given);
            if (value != null && !value.isBlank()) {
                parts.add(value);
            }
        }
        String family = stringValue(name.get("family"));
        if (family != null && !family.isBlank()) {
            parts.add(family);
        }
        return parts.isEmpty() ? null : String.join(" ", parts);
    }

    private List<Map<String, Object>> bundleResources(Map<String, Object> bundle) {
        List<Map<String, Object>> resources = new ArrayList<>();
        for (Object entryValue : getList(bundle, "entry")) {
            Map<String, Object> entry = asMap(entryValue);
            if (!entry.isEmpty()) {
                resources.add(getMap(entry, "resource"));
            }
        }
        return resources;
    }

    private Integer calculateAge(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            return null;
        }
        try {
            return Math.toIntExact(ChronoUnit.YEARS.between(LocalDate.parse(birthDate), LocalDate.now()));
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String codeableConceptText(Map<String, Object> concept) {
        String text = stringValue(concept.get("text"));
        if (text != null && !text.isBlank()) {
            return text;
        }
        for (Object codingValue : getList(concept, "coding")) {
            Map<String, Object> coding = asMap(codingValue);
            String display = firstNonBlank(
                    stringValue(coding.get("display")),
                    stringValue(coding.get("code")));
            if (display != null && !display.isBlank()) {
                return display;
            }
        }
        return null;
    }

    private String referenceDisplay(Map<String, Object> reference) {
        return firstNonBlank(
                stringValue(reference.get("display")),
                stringValue(reference.get("reference")));
    }

    private long temporalSortKey(String value) {
        if (value == null || value.isBlank()) {
            return Long.MIN_VALUE;
        }
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(value).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        return Long.MIN_VALUE;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> getMap(Map<String, Object> parent, String key) {
        return asMap(parent.get(key));
    }

    private List<?> getList(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        return value instanceof List<?> list ? list : List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private Map<String, Object> firstMap(List<?> values) {
        return values.isEmpty() ? Map.of() : asMap(values.get(0));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Map<String, Object> linkedMap(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            map.put(String.valueOf(values[index]), values[index + 1]);
        }
        return map;
    }
}
