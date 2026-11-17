package org.svenknockaert.smappee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.svenknockaert.smappee.platform.api.ApiServer;
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

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0");

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    protected static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    protected static HikariDataSource dataSource;

    protected static MeterReadingRepository meterReadingRepository;

    private static HttpServer server;

    private static int serverPort;

    @BeforeAll
    static void startEnvironment() {
        MYSQL.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(MYSQL.getJdbcUrl());
        config.setUsername(MYSQL.getUsername());
        config.setPassword(MYSQL.getPassword());
        dataSource = new HikariDataSource(config);

        DatabaseMigrator.migrate(dataSource);

        TransactionManager transactionManager = new TransactionManager(dataSource);
        meterReadingRepository = new MeterReadingRepository();
        MeterSampleValidator meterSampleValidator = new MeterSampleValidator();
        TelemetryIngestionService telemetryIngestionService = new TelemetryIngestionServiceImpl(
                transactionManager, meterReadingRepository, meterSampleValidator);

        PingResource pingResource = new PingResource();
        TelemetryIngestionResource telemetryIngestionResource = new TelemetryIngestionResource(telemetryIngestionService);

        server = ApiServer.start(0, List.of(pingResource, telemetryIngestionResource));
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
