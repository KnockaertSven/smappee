# Proposal

## Why

External EV chargers push periodic (5-minute) meter samples over REST to the backend. These pushes encounter real-world conditions including network dropouts, offline buffer replays, out-of-order deliveries, clock resynchronizations, and occasional malformed or corrupted readings. We need a robust ingestion pipeline that safely validates, deduplicates, and commits telemetry data into MySQL with bitemporal awareness and transactional atomicity.

## What Changes

- Implement batch and single-sample HTTP ingestion in `TelemetryIngestionResource` (`POST /api/telemetry/samples`).
- Implement transactional persistence in `TelemetryIngestionService` supporting MySQL storage.
- Guarantee **Idempotency**: Identical pushes (matching `chargerId`, `eventTime`, and `sequenceNumber`) are detected and acknowledged without duplicating rows or skewing ingestion timestamps.
- Guarantee **Atomicity**: Pushed batches are committed fully or rejected completely without leaving partial writes.
- Guarantee **Resilience & Validation**: Malformed payloads, negative energy readings, decreasing cumulative energy counters, and unrealistic physical jumps are rejected with HTTP 400 Bad Request.
- Support **Bitemporality**: Store both event time (the sample measurement interval) and backend ingestion time (receipt timestamp) so revisions and clock corrections can be recorded while preserving audit history.
- Query API and read operations are explicitly deferred to a subsequent change.

## Capabilities

### New Capabilities
- `telemetry-ingestion`: Ingests, validates, deduplicates, and transactionally persists time-series meter telemetry samples with bitemporal tracking.

### Modified Capabilities
<!-- None -->

## Impact

- **API**: Updates `POST /api/telemetry/samples` to support single and batch meter sample payloads, returning HTTP 202 Accepted on success and HTTP 400 Bad Request on validation errors.
- **Data Model**: Introduces a MySQL schema table (`meter_readings`) with composite uniqueness on `(charger_id, event_time, sequence_number)` and bitemporal indexing on `(charger_id, event_time, ingestion_time DESC)`.
- **Backend Services**: Implements OSGi service component for `TelemetryIngestionService` backed by JDBC / DataSource transaction management.
- **Charger Fleet Simulator**: Simulated charger containers can reliably push buffered samples and observe idempotent replay handling.
