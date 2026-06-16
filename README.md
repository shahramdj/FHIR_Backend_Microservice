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
