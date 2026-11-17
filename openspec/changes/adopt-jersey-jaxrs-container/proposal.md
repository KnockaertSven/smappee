# Proposal

## Why

Currently, HTTP routing and request processing in `Main` and `BaseIT` are implemented via verbose, manual JDK `HttpExchange` handlers. This creates duplicated boilerplate for reading input streams, writing response headers, parsing JSON, and mapping errors. Because our resource classes are already annotated with standard JAX-RS annotations (`@Path`, `@GET`, `@POST`, `@Produces`, `@Consumes`), adopting the industry-standard Jersey JAX-RS container will eliminate this manual plumbing and provide automated request dispatch, parameter binding, content negotiation, and exception mapping.

## What Changes

- Add Jersey JDK HTTP Server container dependencies (`jersey-container-jdk-http`, `jersey-media-json-jackson`, `jersey-hk2`) to `pom.xml`.
- Replace manual `HttpExchange` context creation in `Main.java` and `BaseIT.java` with a shared `ResourceConfig` bootstrap using `JdkHttpServerFactory.createHttpServer`.
- Automatically register and route requests to JAX-RS resource classes (`PingResource`, `EchoResource`, `TelemetryIngestionResource`, and future query resources).
- Leverage standard JAX-RS content serialization with Jackson and automatic HTTP error code translation (400 Bad Request on invalid payloads, 404 Not Found on unknown paths, 405 Method Not Allowed).

## Capabilities

### New Capabilities
- `rest-runtime`: Declarative JAX-RS HTTP endpoint hosting, request dispatching, content serialization, and error mapping over JDK HTTP server.

### Modified Capabilities
<!-- None -->

## Impact

- **Dependencies**: Adds Jersey 3.1.x runtime dependencies to `pom.xml`.
- **Application Bootstrap**: Simplifies `Main.java` server initialization from ~100 lines of manual handlers to a clean, declarative `ResourceConfig`.
- **Integration Test Harness**: Simplifies `BaseIT.java` test server setup to use the same Jersey `ResourceConfig`.
- **API Resources**: Enables full use of standard JAX-RS features (`@PathParam`, `@QueryParam`, `Response.status(...)`) across all current and future endpoints.
