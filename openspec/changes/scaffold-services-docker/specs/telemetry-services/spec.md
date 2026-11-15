# Spec Delta

## Purpose

Establishes structural service contracts for telemetry sample ingestion and queries, as well as multi-container deployment orchestration for the backend and simulated charger fleet.

## ADDED Requirements

### Requirement: Telemetry Sample Ingestion Contract
The system SHALL expose an HTTP POST endpoint at `/api/telemetry/samples` that accepts pushed charger meter samples and acknowledges receipt.

#### Scenario: Ingestion endpoint receives sample payload
- **WHEN** a client posts a meter sample payload to `/api/telemetry/samples`
- **THEN** the system returns HTTP 202 Accepted acknowledging receipt

### Requirement: Telemetry Query Contract
The system SHALL expose an HTTP GET endpoint at `/api/chargers/{id}/latest` to query telemetry status for a given charger identifier.

#### Scenario: Query latest reading for a charger
- **WHEN** a client sends a GET request to `/api/chargers/charger-01/latest`
- **THEN** the system returns HTTP 200 OK with the latest known telemetry state

### Requirement: Multi-Container Fleet Orchestration
The system SHALL define a multi-container Docker Compose configuration capable of bootstrapping MySQL, the backend application service, and at least three simulated charger containers.

#### Scenario: Fleet composition boots via docker compose
- **WHEN** the docker compose environment is brought up
- **THEN** containers for MySQL, backend, and three charger instances are initialized on a shared network
