# Design

## Context

The project is being initialized to build a charger telemetry service adhering to the Smappee case requirements on Apache Felix (OSGi). Current `pom.xml` is minimal with Java 27 compiler target. See proposal.md for overall motivation.

## Goals / Non-Goals

**Goals:**
- Configure `pom.xml` with packaging and plugins for building a compliant OSGi bundle (`bnd-maven-plugin`, `maven-jar-plugin`).
- Add core dependencies for OSGi Declarative Services (DS 1.5), JAX-RS / HTTP whiteboard, JSON processing (Jackson), connection pooling (HikariCP), and database connectivity (MySQL Connector/J, Flyway).
- Enforce project code quality rules: JSpecify `@NullMarked` packages via `jspecify-packageinfo-generator-maven-plugin`.
- Provide a clean component architecture with separated interfaces and POJO implementations ready for Declarative Services.
- Provide a basic ping/echo endpoint to prove request/response handling.
- Build an integration test harness using JUnit 5, AssertJ, and Testcontainers (MySQL) with a shared `BaseIT` to verify end-to-end component wiring and I/O.

**Non-Goals:**
- Implementing telemetry data models, bitemporal interval storage, or calculation algorithms (deferred to future feature changes).
- Docker Compose setup and multi-charger container simulation (deferred to separate change).
- Authentication / authorization (explicitly out of scope per debrief).

## Decisions

### 1. OSGi Build Tooling: `bnd-maven-plugin`
- **Choice**: Eclipse Bnd (`bnd-maven-plugin`) in combination with standard `maven-jar-plugin`.
- **Rationale**: Generates bundle manifests automatically from bytecode and Declarative Services annotations (`@Component`, `@Reference`). Avoids hand-crafting error-prone `MANIFEST.MF` headers.
- **Alternatives considered**: `maven-bundle-plugin` (older Apache Felix plugin, more verbose configuration).

### 2. Service Component Architecture & Whiteboard Pattern
- **Choice**: OSGi Declarative Services (DS 1.5) and HTTP Whiteboard pattern.
- **Rationale**: Components are plain Java classes (POJOs) with `@Component` annotations. Exposing REST endpoints via JAX-RS Whiteboard or Felix HTTP Whiteboard allows automatic service registration without manual servlet context plumbing.
- **Design Structure**:
  - `org.svenknockaert.smappee.platform.api`: Interfaces and DTOs (e.g., `HealthCheckService`, `PingResponse`).
  - `org.svenknockaert.smappee.platform.internal`: DS component implementations and REST resources (e.g., `PingResource`).

### 3. Integration Testing Strategy: Testcontainers + Black-Box I/O
- **Choice**: JUnit 5 integration tests extending an abstract `BaseIT`, running a real MySQL container via `org.testcontainers:mysql`.
- **Rationale**: Per project guidelines, tests must avoid mocks where possible and test true input/output. Since Declarative Services components are clean POJOs, we can instantiate and wire the component stack in an integration test harness, execute Flyway migrations against the Testcontainer, and invoke endpoints via HTTP or direct component I/O.
- **Alternatives considered**: Pax Exam (notoriously fragile and slow container boot in local test cycles).

### 4. Code Standards & Null-Safety
- **Choice**: JSpecify (`org.jspecify:jspecify`) + `jspecify-packageinfo-generator-maven-plugin`.
- **Rationale**: Makes all packages `@NullMarked` at compile time, eliminating defensive null checks while guaranteeing null safety.

## Risks / Trade-offs

- **[Risk]** OSGi classpath and bundle dependency resolution friction at runtime.  
  → *Mitigation*: Ensure third-party libraries (e.g. HikariCP, MySQL Connector) are either OSGi-ready bundles or appropriately embedded via bnd instructions.
- **[Risk]** Heavy test execution time when booting containers.  
  → *Mitigation*: Reuse Testcontainers instances across integration tests via a shared singleton container pattern in `BaseIT`.
