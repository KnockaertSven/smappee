# Tasks

## 1. Database Schema & Persistence

- [x] 1.1 Create SQL schema definition for `meter_readings` table with composite unique key `(charger_id, event_time, sequence_number)` and bitemporal index `(charger_id, event_time, ingestion_time DESC)`.
- [x] 1.2 Implement `TransactionManager` component providing atomic transaction execution (`setAutoCommit(false)`, `commit()`, `rollback()`) over `DataSource`.
- [x] 1.3 Implement `MeterReadingRepository` supporting atomic batch inserts and idempotent deduplication via `ON DUPLICATE KEY UPDATE`.

## 2. Ingestion Service & Domain Validation

- [x] 2.1 Implement domain validator for meter samples to reject null values, negative active energy, and abnormal temperature readings.
- [x] 2.2 Implement `TelemetryIngestionServiceImpl` as an OSGi component coordinating transaction rollback on batch failure and acknowledging idempotent replays.

## 3. REST Ingestion Endpoint & Verification

- [x] 3.1 Enhance `TelemetryIngestionResource` (`POST /api/telemetry/samples`) to accept single and batch payloads, returning HTTP 202 Accepted on success and HTTP 400 Bad Request on validation errors.
- [x] 3.2 Create integration tests in `TelemetryIngestionIT` testing happy-path batch ingestion, identical replay deduplication (idempotency), and batch rollback on invalid input.
- [x] 3.3 Update `docker/simulator.py` to support buffered batch pushes and replay behavior.
