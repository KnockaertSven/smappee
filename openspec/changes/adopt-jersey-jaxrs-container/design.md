# Design

## Context

See [proposal.md](file:///C:/Users/Sven%20Knockaert/Documents/Coding/smappee/openspec/changes/adopt-jersey-jaxrs-container/proposal.md) for background and motivation. The system currently hosts endpoints on the JDK HTTP server (`com.sun.net.httpserver.HttpServer`), but manually inspects HTTP verbs, reads input streams, invokes resources, and serializes responses in both `Main.java` and `BaseIT.java`. Our resource classes are already annotated with Jakarta REST annotations (`@Path`, `@GET`, `@POST`, `@Produces`, `@Consumes`).

## Goals / Non-Goals

**Goals:**
- Eliminate manual `HttpExchange` request routing, parsing, and stream-handling boilerplate from `Main.java` and `BaseIT.java`.
- Introduce Jersey 3.1.x as the JAX-RS container on top of the JDK HTTP server.
- Provide a unified, shared `ApiServer` factory that configures `ResourceConfig`, Jackson serialization, and exception mapping.
- Automatically map domain and validation exceptions (`IllegalArgumentException`) to HTTP 400 Bad Request responses.

**Non-Goals:**
- Introducing a heavyweight servlet engine (e.g., Tomcat or standalone Jetty).
- Modifying database schemas, persistence logic, or simulator profiles.

## Decisions

### 1. Choice of Container: `jersey-container-jdk-http`
- **Choice**: Eclipse Jersey 3.1.x (`jersey-container-jdk-http`, `jersey-media-json-jackson`, `jersey-hk2`).
- **Rationale**: Jersey is the Jakarta REST Reference Implementation. Its JDK container connects directly to `com.sun.net.httpserver.HttpServer`, allowing us to keep our lightweight, fast-booting HTTP server while unlocking full declarative routing, parameter binding, and Jackson content negotiation. It also natively provides the `RuntimeDelegate` implementation.
- **Alternatives Considered**:
  - *RESTEasy*: Excellent in Quarkus/WildFly, but less ubiquitous than Jersey for standalone JDK HTTP server hosting.
  - *Apache CXF*: Traditional OSGi choice, but significantly heavier and more complex to configure outside a full Karaf distribution.
  - *Javalin*: Modern and fast, but uses a proprietary functional API rather than standard JAX-RS annotations.

### 2. Centralized Server Bootstrap via `ApiServer`
- **Choice**: Create a shared `ApiServer` utility class that constructs the `ResourceConfig` and boots the `HttpServer`.
- **Rationale**: Both `Main.java` (running in Docker Compose) and `BaseIT.java` (running integration tests) need the exact same routing, serialization, and error-handling behavior. Centralizing this eliminates duplication and prevents configuration drift.

### 3. Declarative Exception Mapping
- **Choice**: Register a JAX-RS `ExceptionMapper<IllegalArgumentException>` returning HTTP 400 Bad Request with a structured error payload.
- **Rationale**: Cleanly decouples domain validation in services from HTTP status code handling, allowing resources to throw domain exceptions naturally without boilerplate `try/catch` wrapping.

## Risks / Trade-offs

- **[Risk] Library footprint**: Adding Jersey and HK2 introduces ~4MB of runtime dependencies.  
  → *Mitigation*: Negligible impact on container build time and memory usage compared to the developer ergonomics and bug reduction gained.
- **[Risk] Test port isolation**: Integration tests running in parallel might conflict if bound to static ports.  
  → *Mitigation*: Continue using port `0` in `BaseIT` so the operating system assigns an ephemeral port per test run.
