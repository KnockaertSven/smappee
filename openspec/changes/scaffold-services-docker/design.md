# Design

## Context

The OSGi technical foundation and Testcontainers integration harness are in place. We now establish the full system topology required by the Smappee case before filling in concrete bitemporal logic. See proposal.md for overall motivation.

## Goals / Non-Goals

**Goals:**
- Scaffold the backend service interfaces and Declarative Services component skeletons:
  - `TelemetryIngestionService` and `TelemetryQueryService` in `org.svenknockaert.smappee.telemetry.api`.
  - Ingestion and Query REST resources in `org.svenknockaert.smappee.telemetry.api`.
  - Domain / DTO records for sample push payloads and query results in `org.svenknockaert.smappee.telemetry.api.dto`.
- Scaffold the charger simulator in `chargers/` with a lightweight `Dockerfile` and `simulator.py` acknowledging its configured `CHARGER_ID`, `BACKEND_URL`, and `BEHAVIOR`.
- Add a multi-stage root `Dockerfile` to package the Java backend into a container.
- Add `docker-compose.yml` orchestrating `mysql`, `backend`, and three charger simulator containers (`charger-1`, `charger-2`, `charger-3`).
- Add `.dockerignore` to optimize build context.

**Non-Goals:**
- Implementing bitemporal SQL schema, table indexes, and calculation algorithms (handled in the feature implementation change).
- Implementing full chaos and network-drop logic in the simulator (scaffolding verifies container wiring and environment parsing first).

## Decisions

### 1. Unified Repository & Container Layout
- **Choice**: Monorepo with root `Dockerfile`, `chargers/Dockerfile`, and root `docker-compose.yml`.
- **Rationale**: Meets the mandatory deliverable of a single Git repository testable from a fresh clone via `docker compose up`.

### 2. Service Component Boundaries
- **Choice**: Segregate Ingestion and Query into dedicated service interfaces:
  - `TelemetryIngestionService`: Handles incoming 5-minute samples (validation, atomicity, idempotency).
  - `TelemetryQueryService`: Answers bitemporal queries (latest reading, interval evolution, energy consumption).
- **Rationale**: Adheres to Single Responsibility Principle and allows independent mocking, testing, and evolution.

### 3. Simulator Implementation: Python 3.12 Alpine
- **Choice**: `python:3.12-alpine` running `simulator.py` using Python's standard library (`urllib.request`, `json`, `os`, `time`).
- **Rationale**: Avoids extra build tools or large runtimes, keeps the container image under 45MB, and starts instantly.

### 4. Deterministic Container Startup via Healthchecks
- **Choice**: MySQL configured with `healthcheck: mysqladmin ping`. Backend depends on MySQL with `condition: service_healthy`. Chargers depend on backend.
- **Rationale**: Eliminates startup race conditions without requiring external wait scripts.

## Risks / Trade-offs

- **[Risk]** Heavy Docker build times when packaging Java backend.  
  → *Mitigation*: Multi-stage Dockerfile caching Maven dependencies in early layers and using `.dockerignore`.
- **[Risk]** Port collisions with local MySQL or HTTP services.  
  → *Mitigation*: Map host ports configurable via environment variables with sensible defaults (e.g., `8080:8080`, `3306:3306`).
