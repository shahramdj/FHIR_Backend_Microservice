# FHIR_Backend_Microservice

A backend microservice that connects to EHR systems and provides medical information to the smart glass frontend.

## API Endpoints

- `GET /api/patient/{id}/summary`
- `GET /api/patient/{id}/allergies`
- `GET /api/patient/{id}/medications`
- `GET /api/patient/{id}/labs`
- `GET /api/patient/{id}/imaging`
- `GET /api/patient-context/{patientId}`
- `POST /api/voice/command`

The service translates these simplified APIs into upstream FHIR requests such as:

- `GET /Patient/{id}`
- `GET /AllergyIntolerance?patient={id}`
- `GET /Condition?patient={id}`
- `GET /MedicationRequest?patient={id}`
- `GET /Observation?patient={id}`
- `GET /ImagingStudy?patient={id}`
- `GET /Procedure?patient={id}`

Set `fhir.base-url` to point to the upstream FHIR server.

## Mock FHIR server with 5 patients

This repository includes a built-in mock FHIR server and seeded data for 5 patients at:

- `src/main/resources/mock-fhir/patient-data.json`

### Run mock server (FHIR endpoints only)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock-fhir
```

The mock server runs on `http://localhost:8081/fhir` and exposes:

- `GET /fhir/Patient/{id}`
- `GET /fhir/AllergyIntolerance?patient={id}`
- `GET /fhir/Condition?patient={id}`
- `GET /fhir/MedicationRequest?patient={id}`
- `GET /fhir/Observation?patient={id}`
- `GET /fhir/DiagnosticReport?patient={id}`
- `GET /fhir/ImagingStudy?patient={id}`
- `GET /fhir/DocumentReference?patient={id}`
- `GET /fhir/Encounter?patient={id}`
- `GET /fhir/Procedure?patient={id}`
- `GET /fhir/Media?patient={id}` — returns a FHIR Bundle of Media resources for the patient
- `GET /fhir/Media/{id}` — returns the X-ray image as `image/jpeg`
- `GET /fhir/Media/{id}?metadata=true` — returns the FHIR Media resource metadata as JSON

Each of the 5 mock patients has a chest X-ray JPEG image stored in `src/main/resources/mock-fhir/images/`.
The images are generated grayscale mock X-rays (512×512 px) with anatomical landmarks and a patient-specific radiology finding label.

### Point backend to mock server

Use:

- `fhir.base-url=http://localhost:8081/fhir`
