# Smappee Charger Telemetry Engineering Case

## How to run
```bash
docker compose up --build
```
* Integration tests can be run via `mvn test` (uses testcontainers MySQL)

## What is in place
* Ingestion REST endpoint (`POST /api/telemetry/samples`) to receive 5-minute meter readings
* Bitemporal MySQL setup: splits event time from ingestion time so we can process historical data
* Idempotent sample deduplication, plus support for revised intervals
* Basic validation & transactional DB writes to reject bad readings (HTTP 400)
* 3 simulated charger containers, wired up with docker compose healthchecks so they only push when the backend is up
* Integration tests with testcontainers to verify E2E flows, replays and revisions

## What I had to cut due to 3hr time constraints
* Charger simulator logic: kept it simple without chaos modes, real-world edge cases are covered by integration tests instead
* Configurable timings, configurable host names, etc
* Versioning checks of dependencies

## What I would add myself if I had more time
* Use Spring Boot to simplify a lot of manual setup:
  * OOTB config for transaction management, JPA
  * OOTB config for flyway
  * OOTB setup for HTTP server (Tomcat)
  * Easier integration testing setup
  * etc...
* Set up proper hexagonal architecture with a domain, application services, ports & adapters, ...
  * Possibly paired with an archunit test to enforce boundaries
* Set up Jspecify to handle nullability at compile time
* Add Kafka or a streaming pipeline for scaling to thousands of chargers
* Take proper time to work TDD first
