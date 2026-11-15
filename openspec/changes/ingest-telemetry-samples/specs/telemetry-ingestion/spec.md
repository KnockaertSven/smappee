# Spec Delta

## Purpose

Provides resilient, idempotent, and atomic HTTP ingestion of 5-minute electric vehicle charger telemetry meter samples into a bitemporal data store.

## ADDED Requirements

### Requirement: Telemetry Ingestion HTTP Contract
The system SHALL expose an HTTP POST endpoint at `/api/telemetry/samples` accepting both single meter sample objects and batch sample payloads.

#### Scenario: Successfully ingest single meter sample
- **WHEN** a client posts a valid meter sample JSON object to `/api/telemetry/samples`
- **THEN** the system returns HTTP 202 Accepted acknowledging receipt

#### Scenario: Successfully ingest batch meter samples
- **WHEN** a client posts a valid array or batch envelope of meter samples to `/api/telemetry/samples`
- **THEN** the system returns HTTP 202 Accepted acknowledging receipt of the batch

### Requirement: Idempotent Replay Handling
The system SHALL ensure that replaying an identical meter sample matching an existing charger identifier, event time, and source sequence number does not duplicate records or overwrite previously recorded ingestion timestamps.

#### Scenario: Charger replays already ingested sample
- **GIVEN** a meter sample with charger `charger-01`, event time `2026-11-15T14:00:00Z`, and sequence `101` was already ingested
- **WHEN** the charger posts the same sample again
- **THEN** the system returns HTTP 202 Accepted without inserting duplicate database records or altering original ingestion times

### Requirement: Atomic Batch Ingestion
The system SHALL process batch sample ingestion requests atomically within a single transaction, ensuring that either all valid non-duplicate samples in the batch are committed or none are stored.

#### Scenario: Partial batch failure aborts entire transaction
- **WHEN** a client posts a batch containing both valid samples and one invalid sample with negative active energy
- **THEN** the system rejects the request with HTTP 400 Bad Request and commits no samples from the batch to the database

### Requirement: Input Validation and Anomaly Guarding
The system SHALL validate all incoming meter samples, rejecting payloads containing null fields, negative active energy values, or invalid temperatures.

#### Scenario: Reject negative active energy
- **WHEN** a client posts a sample with active energy less than zero
- **THEN** the system returns HTTP 400 Bad Request with a descriptive error message

#### Scenario: Reject malformed or null payload
- **WHEN** a client posts an empty or malformed JSON payload
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Bitemporal Sample Recording
The system SHALL record both the sample event time (measurement interval) and the ingestion time (timestamp when backend received the sample), enabling revisions to be captured as distinct historical records.

#### Scenario: Ingesting revised reading for existing interval
- **GIVEN** a sample for `charger-01` at event time `2026-11-15T14:00:00Z` with sequence `101` exists
- **WHEN** a revised sample for `charger-01` at event time `2026-11-15T14:00:00Z` with a new sequence `105` is posted
- **THEN** the system stores the revision as a new record with its own current ingestion timestamp
