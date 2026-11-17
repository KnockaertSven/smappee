package org.svenknockaert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariDataSource;
import org.svenknockaert.smappee.platform.api.EchoResource;
import org.svenknockaert.smappee.platform.api.PingResource;
import org.svenknockaert.smappee.platform.api.dto.EchoPayload;
import org.svenknockaert.smappee.platform.persistence.DatabaseMigrator;
import org.svenknockaert.smappee.platform.persistence.TransactionManager;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionResource;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionService;
import org.svenknockaert.smappee.telemetry.internal.MeterReadingRepository;
import org.svenknockaert.smappee.telemetry.internal.MeterSampleValidator;
import org.svenknockaert.smappee.telemetry.internal.TelemetryIngestionServiceImpl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  Smappee Telemetry Backend Bootstrapping...");
        System.out.println("==================================================");

        DatabaseMigrator.migrateFromEnv();

        HikariDataSource dataSource = DatabaseMigrator.createDataSourceFromEnv();
        TransactionManager transactionManager = new TransactionManager(dataSource);
        MeterReadingRepository repository = new MeterReadingRepository();
        MeterSampleValidator validator = new MeterSampleValidator();
        TelemetryIngestionService ingestionService = new TelemetryIngestionServiceImpl(
                transactionManager, repository, validator);
        TelemetryIngestionResource ingestionResource = new TelemetryIngestionResource(ingestionService);
        PingResource pingResource = new PingResource();
        EchoResource echoResource = new EchoResource();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/ping", exchange -> {
            try {
                var response = pingResource.ping();
                byte[] bytes = MAPPER.writeValueAsBytes(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Throwable t) {
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.createContext("/api/echo", exchange -> {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                try (InputStream is = exchange.getRequestBody()) {
                    byte[] requestBytes = is.readAllBytes();
                    if (requestBytes.length == 0) {
                        exchange.sendResponseHeaders(400, -1);
                        return;
                    }

                    EchoPayload payload;
                    try {
                        payload = MAPPER.readValue(requestBytes, EchoPayload.class);
                    } catch (Exception e) {
                        exchange.sendResponseHeaders(400, -1);
                        return;
                    }

                    var response = echoResource.echo(payload);
                    byte[] bytes = MAPPER.writeValueAsBytes(response);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                }
            } catch (Throwable t) {
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.createContext("/api/telemetry/samples", exchange -> {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                try (InputStream is = exchange.getRequestBody()) {
                    byte[] requestBytes = is.readAllBytes();
                    if (requestBytes.length == 0) {
                        byte[] errBytes = MAPPER.writeValueAsBytes(Map.of("error", "Payload cannot be empty"));
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errBytes);
                        }
                        return;
                    }

                    String requestBody = new String(requestBytes, StandardCharsets.UTF_8);
                    try {
                        var response = ingestionResource.receiveSamples(requestBody);
                        byte[] bytes = MAPPER.writeValueAsBytes(response);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(202, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                    } catch (IllegalArgumentException e) {
                        byte[] errBytes = MAPPER.writeValueAsBytes(Map.of("error", e.getMessage()));
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errBytes);
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.start();
        System.out.println("[Backend] Smappee API server listening on port " + port);

        Thread.currentThread().join();
    }
}
