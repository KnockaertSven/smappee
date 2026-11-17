# Spec Delta

## Purpose

Provides automated JAX-RS declarative request dispatching, parameter binding, content negotiation, and error mapping for HTTP endpoints.

## ADDED Requirements

### Requirement: JAX-RS Declarative Request Routing
The system SHALL expose HTTP endpoints declared via standard JAX-RS resource annotations and route incoming client requests to their matching resource methods.

#### Scenario: Route incoming GET request
- **WHEN** a client sends a GET request to `/api/ping`
- **THEN** the system routes the request to the matching resource and returns HTTP 200 OK with JSON content

#### Scenario: Route incoming POST request
- **WHEN** a client sends a POST request to `/api/telemetry/samples` with a valid JSON payload
- **THEN** the system routes the request to the matching resource and returns HTTP 200 OK

### Requirement: Automatic Content Serialization and Deserialization
The system SHALL automatically deserialize incoming JSON request bodies into typed model objects and serialize response entities into JSON format.

#### Scenario: Deserializing JSON payload
- **WHEN** a client sends a POST request with a valid JSON payload matching the expected DTO
- **THEN** the system automatically deserializes the body into the target DTO without manual stream reading

### Requirement: Automated Error Mapping
The system SHALL automatically map invalid client requests, unsupported HTTP methods, and missing endpoints to standardized HTTP error status codes.

#### Scenario: Malformed JSON payload rejection
- **WHEN** a client sends a POST request with malformed or unparseable JSON
- **THEN** the system responds with HTTP 400 Bad Request

#### Scenario: Unknown route handling
- **WHEN** a client sends a request to an unregistered path
- **THEN** the system responds with HTTP 404 Not Found

#### Scenario: Unsupported method handling
- **WHEN** a client sends a GET request to an endpoint configured only for POST
- **THEN** the system responds with HTTP 405 Method Not Allowed
