# Proposal

## Why

To establish the foundational technical architecture for the Smappee charger telemetry system on Apache Felix (OSGi) as mandated by the engineering case, without implementing domain features prematurely. Setting up the OSGi build tooling, bundle lifecycle, HTTP whiteboard, and automated integration testing with Testcontainers upfront ensures the operational platform is verified before building telemetry ingestion and bitemporal queries.

## What Changes

- Configure Maven build plugins (`bnd-maven-plugin`, `maven-jar-plugin`) to package the project as a valid OSGi bundle with Declarative Services component descriptors.
- Add core dependencies for OSGi Declarative Services (`@Component`, `@Reference`), JAX-RS / HTTP whiteboard, Jackson JSON binding, HikariCP, and MySQL JDBC driver.
- Add code quality and null-safety dependencies (`org.jspecify:jspecify` + `jspecify-packageinfo-generator-maven-plugin`).
- Add testing infrastructure dependencies (JUnit 5, AssertJ, and Testcontainers MySQL).
- Implement a basic health/echo resource (`/api/ping` or `/health`) registered via OSGi Declarative Services and HTTP whiteboard to verify component wiring and I/O handling.
- Implement an automated Integration Test (IT) that boots the application context / components against Testcontainers to verify basic I/O.

## Capabilities

### New Capabilities
- `platform-runtime`: Provides the OSGi Declarative Services bundle runtime, HTTP whiteboard REST endpoint registration, and basic ping/health check I/O verification.

### Modified Capabilities
<!-- None -->

## Impact

- Build system: Configures `pom.xml` with OSGi bundle packaging, bnd plugin, and testing dependencies.
- Runtime architecture: Establishes OSGi component annotations and HTTP whiteboard handling.
- Testing: Introduces integration test harness using Testcontainers.
