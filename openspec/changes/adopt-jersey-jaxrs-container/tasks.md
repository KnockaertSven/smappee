# Tasks

## 1. Dependencies & Exception Mapping

- [x] 1.1 Add Jersey 3.1.x dependencies (`jersey-container-jdk-http`, `jersey-media-json-jackson`, `jersey-hk2`) to `pom.xml`.
- [x] 1.2 Implement `ValidationExceptionMapper` implementing `ExceptionMapper<IllegalArgumentException>` to map domain validation errors to HTTP 400 Bad Request with a structured JSON error payload.

## 2. Server Infrastructure & Application Wiring

- [x] 2.1 Implement `ApiServer` utility to configure Jersey `ResourceConfig` (registering JacksonFeature, exception mapper, and resource instances) and instantiate `HttpServer` via `JdkHttpServerFactory.createHttpServer`.
- [x] 2.2 Refactor `Main.java` to boot HTTP routing via `ApiServer` on port 8080, removing manual `HttpExchange` context handlers.
- [x] 2.3 Refactor `BaseIT.java` to boot test HTTP routing via `ApiServer` on port 0, removing duplicate manual `HttpExchange` context handlers.

## 3. Integration Verification

- [ ] 3.1 Verify `PingEchoIT` passes end-to-end against Jersey container (verifying GET `/api/ping`, POST `/api/echo`, and HTTP 400 malformed JSON handling).
- [ ] 3.2 Verify `TelemetryIngestionIT` passes end-to-end against Jersey container (verifying batch sample ingestion, idempotent replays, bitemporal revisions, and atomic rollbacks).
