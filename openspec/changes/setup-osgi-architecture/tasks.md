# Tasks

## 1. Build & Dependencies Configuration

- [x] 1.1 Configure `pom.xml` with OSGi bundle packaging, `bnd-maven-plugin`, and `maven-jar-plugin` instructions to enable automatic bundle manifest generation.
- [x] 1.2 Add OSGi Declarative Services 1.5 (`org.osgi.service.component.annotations`), JAX-RS / HTTP whiteboard, and Jackson JSON processing dependencies to `pom.xml`.
- [x] 1.3 Add persistence dependencies (`com.mysql:mysql-connector-j`, `com.zaxxer:HikariCP`, `org.flywaydb:flyway-core`, `org.flywaydb:flyway-mysql`) to `pom.xml`.
- [x] 1.4 Add JSpecify (`org.jspecify:jspecify`) and `jspecify-packageinfo-generator-maven-plugin` to enforce null-marked package defaults.
- [x] 1.5 Add test dependencies (`org.junit.jupiter:junit-jupiter`, `org.assertj:assertj-core`, `org.testcontainers:testcontainers`, `org.testcontainers:junit-jupiter`, `org.testcontainers:mysql`) to `pom.xml`.

## 2. Platform Runtime & Basic I/O Components

- [x] 2.1 Create the API model records (`PingResponse`, `EchoPayload`) in `org.svenknockaert.smappee.platform.api` following project styling and immutability rules.
- [x] 2.2 Implement `PingResource` in `org.svenknockaert.smappee.platform.internal` annotated with `@Component` and JAX-RS `@Path("/api/ping")` to serve health checks.
- [x] 2.3 Implement `EchoResource` in `org.svenknockaert.smappee.platform.internal` annotated with `@Component` and JAX-RS `@Path("/api/echo")` to handle JSON I/O echoing.

## 3. Integration Testing with Testcontainers

- [x] 3.1 Create abstract `BaseIT` test harness configuring an ephemeral MySQL Testcontainer and component runner.
- [x] 3.2 Create `PingEchoIT` extending `BaseIT` using given/when/then sections to verify `/api/ping` and `/api/echo` endpoints against the Testcontainers environment.
