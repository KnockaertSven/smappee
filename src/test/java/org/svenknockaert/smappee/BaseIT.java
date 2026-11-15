package org.svenknockaert.smappee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.svenknockaert.smappee.platform.api.EchoPayload;
import org.svenknockaert.smappee.platform.internal.EchoResource;
import org.svenknockaert.smappee.platform.internal.PingResource;
import org.testcontainers.containers.MySQLContainer;

public abstract class BaseIT {

    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0");

    protected static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    protected static final HttpClient CLIENT = HttpClient.newHttpClient();

    protected static HikariDataSource dataSource;

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

        PingResource pingResource = new PingResource();
        EchoResource echoResource = new EchoResource();

        server = HttpServer.create(new InetSocketAddress(0), 0);

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

        server.start();
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
