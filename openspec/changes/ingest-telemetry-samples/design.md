# Design

## Context

See [proposal.md](file:///C:/Users/Sven%20Knockaert/Documents/Coding/smappee/openspec/changes/ingest-telemetry-samples/proposal.md) for motivation and problem background.
The system runs as an OSGi bundle on Apache Felix, using JAX-RS for REST endpoints, and connects to a MySQL database container. Chargers push 5-minute meter telemetry samples and may buffer/replay batches upon reconnecting after network outages.

## Goals / Non-Goals

**Goals:**
- Provide a robust REST ingestion endpoint supporting both single and batch sample submissions.
- Enforce transactional atomicity: all valid samples in a batch are committed together; any invalid sample aborts the entire batch.
- Enforce idempotency: re-sending the same sample (`charger_id`, `event_time`, `sequence_number`) is safely acknowledged without duplicating data or corrupting ingestion history.
- Capture bitemporal dimensions: store distinct `event_time` and `ingestion_time` to allow tracking revisions and corrections over time.

**Non-Goals:**
- Telemetry query / read API implementation (deferred to a subsequent change).
- Authentication and authorization (explicitly out of scope per engineering case instructions).
- User interface or dashboard components.

## Decisions

### 1. Relational Schema & Bitemporal Data Model

We introduce the `meter_readings` table in MySQL:

```sql
CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    charger_id VARCHAR(64) NOT NULL,
    event_time TIMESTAMP(3) NOT NULL,
    sequence_number BIGINT NOT NULL,
    active_energy_wh BIGINT NOT NULL,
    temperature DOUBLE NOT NULL,
    ingestion_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT uk_charger_event_seq UNIQUE KEY (charger_id, event_time, sequence_number),
    INDEX idx_bitemporal_lookup (charger_id, event_time, ingestion_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Rationale:**
- `CONSTRAINT uk_charger_event_seq` guarantees that identical pushes from the same charger for the same interval and sequence cannot be stored twice.
- Separate `event_time` and `ingestion_time` fulfill the bitemporal requirement: revised intervals arrive with a newer `sequence_number` and receive their own `ingestion_time`.
- `idx_bitemporal_lookup` optimizes future queries for both latest readings and as-of historical views.

**Alternatives Considered:**
- *Overwriting existing intervals (`UPDATE ... WHERE charger_id = ? AND event_time = ?`)*: Rejected because it destroys historical belief and fails the requirement to answer *"what did we believe that interval's reading was as of yesterday?"*.

### 2. Idempotency & Replay Handling Strategy

When inserting records:
```sql
INSERT INTO meter_readings (charger_id, event_time, sequence_number, active_energy_wh, temperature, ingestion_time)
VALUES (?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE id = id;
```

**Rationale:**
- If a charger retries a batch where some samples were already written before a disconnect, `ON DUPLICATE KEY UPDATE id = id` (or `INSERT IGNORE`) safely no-ops the duplicates without throwing an error or creating duplicate rows.
- The service returns `202 Accepted`, informing the charger that its push succeeded so it can safely clear its local buffer.

**Alternatives Considered:**
- *Explicit `SELECT` before `INSERT`*: Requires extra round-trips to MySQL per sample in a batch and is prone to race conditions under concurrent pushes.

### 3. API Contract and Batch Support

`POST /api/telemetry/samples` will accept:
- A single `MeterSampleDto` object.
- A batch payload (either a JSON array or a wrapper `BatchMeterSamplesDto`).

**Validation Rules:**
- `chargerId` must not be null or blank.
- `eventTime` must not be null.
- `activeEnergyWh` must be `>= 0`.
- `temperature` must be within physically valid operating bounds (e.g., -50°C to +120°C).
- If any validation rule fails on any sample in a batch, the transaction is rolled back and HTTP 400 Bad Request is returned.

### 4. OSGi Service Architecture & Transaction Boundary

- `TransactionManager`: Lightweight OSGi `@Component` providing functional transaction management (`execute(TransactionCallback<T>)`) wrapping `connection.setAutoCommit(false)`, `commit()`, and `rollback()` around JDBC operations using the HikariCP `DataSource`.
- `TelemetryIngestionResource`: JAX-RS resource bound to Apache Felix HTTP whiteboard. Validates request formatting and delegates to `TelemetryIngestionService`.
- `TelemetryIngestionServiceImpl`: OSGi `@Component` providing `TelemetryIngestionService`. Performs fail-fast domain validation across the batch and executes persistence inside `TransactionManager`.
- `MeterReadingRepository`: Encapsulates JDBC batch execution (`PreparedStatement.addBatch()` and `executeBatch()`) using `ON DUPLICATE KEY UPDATE id = id` for idempotent deduplication.

## Risks / Trade-offs

- **[Risk] High Replay Volumes after Charger Reconnection** → **Mitigation**: Batch insertion using JDBC `PreparedStatement.executeBatch()` inside a single transaction minimizes round-trips and lock contention.
- **[Risk] Clock Skew / Resyncs** → **Mitigation**: Relying on charger-supplied `event_time` + `sequence_number` for uniqueness prevents resynced clocks from colliding with actual distinct intervals.
- **[Risk] Partial Failure in Large Batches** → **Mitigation**: Strict transactional rollback on any validation failure ensures the database never has partially committed states.
