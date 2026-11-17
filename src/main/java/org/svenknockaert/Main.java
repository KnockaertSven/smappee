package org.svenknockaert;

import com.zaxxer.hikari.HikariDataSource;
import org.svenknockaert.smappee.platform.api.ApiServer;
import org.svenknockaert.smappee.platform.api.PingResource;
import org.svenknockaert.smappee.platform.persistence.DatabaseMigrator;
import org.svenknockaert.smappee.platform.persistence.TransactionManager;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionResource;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionService;
import org.svenknockaert.smappee.telemetry.internal.MeterReadingRepository;
import org.svenknockaert.smappee.telemetry.internal.MeterSampleValidator;
import org.svenknockaert.smappee.telemetry.internal.TelemetryIngestionServiceImpl;

import java.util.List;

public class Main {

    static void main() throws Exception {
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

        int port = 8080;
        ApiServer.start(port, List.of(pingResource, ingestionResource));

        System.out.println("[Backend] Smappee API server listening on port " + port);

        Thread.currentThread().join();
    }
}
