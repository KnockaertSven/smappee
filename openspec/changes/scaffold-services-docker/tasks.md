# Tasks

## 1. Backend Service Contracts & Skeletons

- [x] 1.1 Create telemetry DTO records (`MeterSampleDto`, `LatestReadingResponse`, `IntervalHistoryResponse`, `EnergyConsumptionResponse`) in `org.svenknockaert.smappee.telemetry.api.dto` adhering to project styling rules.
- [x] 1.2 Define `TelemetryIngestionService` interface in `org.svenknockaert.smappee.telemetry.api` for accepting and processing meter sample batches.
- [x] 1.3 Define `TelemetryQueryService` interface in `org.svenknockaert.smappee.telemetry.api` for querying latest readings, revision histories, and consumed energy.
- [x] 1.4 Implement `TelemetryIngestionResource` at `/api/telemetry/samples` and `TelemetryQueryResource` at `/api/chargers` in `org.svenknockaert.smappee.telemetry.api` annotated with Declarative Services `@Component` and JAX-RS path mappings.

## 2. Charger Simulator Scaffolding

- [x] 2.1 Create `chargers/simulator.py` parsing environment variables (`CHARGER_ID`, `BACKEND_URL`, `BEHAVIOR`, `PUSH_INTERVAL_SECONDS`) with structured startup logging and loop scaffolding.
- [x] 2.2 Create `chargers/Dockerfile` using `python:3.12-alpine` to package and run the simulator script.

## 3. Containerization & Orchestration

- [x] 3.1 Create root `Dockerfile` defining a multi-stage container build for the Java backend application.
- [x] 3.2 Create `.dockerignore` at repository root excluding `target/`, `.idea/`, `.git/`, and temp files from build context.
- [x] 3.3 Create root `docker-compose.yml` configuring `mysql` (with healthcheck), `backend` service, and 3 charger container instances (`charger-1`, `charger-2`, `charger-3`).
