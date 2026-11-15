# Proposal

## Why

To establish the complete multi-service scaffolding and container topology required by the Smappee case before implementing the detailed bitemporal logic and simulation dynamics. Having all service boundaries, Dockerfiles, and `docker-compose.yml` in place ensures the entire multi-container architecture (backend, MySQL, and 3 charger containers) can be booted and verified together.

## What Changes

- Scaffold the core Java backend service contracts and Declarative Services components:
  - `TelemetryIngestionService`: Contract for validating and accepting pushed 5-minute meter samples.
  - `TelemetryQueryService`: Contract for serving latest readings, interval revision history, and consumed energy aggregations.
  - Scaffolding REST endpoints for ingestion (`/api/telemetry/samples`) and query (`/api/chargers`).
- Scaffold the charger simulator in a dedicated `chargers/` directory with its own lightweight Dockerfile and entrypoint script accepting environment variables (`CHARGER_ID`, `BACKEND_URL`, `BEHAVIOR`).
- Create root `Dockerfile` for the Java backend service.
- Create root `docker-compose.yml` defining the complete system topology:
  - `mysql` database container with healthcheck.
  - `backend` Felix service container.
  - 3 independent charger instances (`charger-1`, `charger-2`, `charger-3`) wired to the backend.

## Capabilities

### New Capabilities
- `telemetry-services`: Defines service boundaries for telemetry sample ingestion and queries, alongside multi-container orchestration for the backend and simulated charger fleet.

### Modified Capabilities
<!-- None -->

## Impact

- Adds service interfaces and component skeletons in `org.svenknockaert.smappee.telemetry`.
- Adds `chargers/` folder with simulator script and Dockerfile.
- Adds root `Dockerfile` and `docker-compose.yml`.
