package org.svenknockaert.smappee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.svenknockaert.smappee.platform.api.ApiServer;
import org.svenknockaert.smappee.platform.api.EchoResource;
import org.svenknockaert.smappee.platform.api.PingResource;
import org.svenknockaert.smappee.platform.persistence.DatabaseMigrator;
import org.svenknockaert.smappee.platform.persistence.TransactionManager;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionResource;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionService;
import org.svenknockaert.smappee.telemetry.internal.MeterReadingRepository;
import org.svenknockaert.smappee.telemetry.internal.MeterSampleValidator;
import org.svenknockaert.smappee.telemetry.internal.TelemetryIngestionServiceImpl;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public abstract class BaseIT {

    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0");

    protected static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    protected static final HttpClient CLIENT = HttpClient.newHttpClient();

    protected static HikariDataSource dataSource;

    protected static TransactionManager transactionManager;

    protected static MeterReadingRepository meterReadingRepository;

    protected static HttpServer server;

    protected static int serverPort;

    @BeforeAll
    static void startEnvironment() throws IOException {
        MYSQL.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(MYSQL.getJdbcUrl());
        config.setUsername(MYSQL.getUsername());
        config.setPassword(MYSQL.getPassword());
        dataSource = new HikariDataSource(config);

        DatabaseMigrator.migrate(dataSource);

        transactionManager = new TransactionManager(dataSource);
        meterReadingRepository = new MeterReadingRepository();
        MeterSampleValidator meterSampleValidator = new MeterSampleValidator();
        TelemetryIngestionService telemetryIngestionService = new TelemetryIngestionServiceImpl(
                transactionManager, meterReadingRepository, meterSampleValidator);

        PingResource pingResource = new PingResource();
        EchoResource echoResource = new EchoResource();
        TelemetryIngestionResource telemetryIngestionResource = new TelemetryIngestionResource(telemetryIngestionService);

        server = ApiServer.start(0, List.of(pingResource, echoResource, telemetryIngestionResource));
        serverPort = server.getAddress().getPort();
    }

    @AfterAll
    static void stopEnvironment() {
        if (server != null) {
            server.stop(0);
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (MYSQL.isRunning()) {
            MYSQL.stop();
        }
    }

    protected HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + path))
                .GET()
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendPost(String path, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
