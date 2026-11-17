package org.svenknockaert.smappee.telemetry.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.svenknockaert.smappee.platform.persistence.TransactionManager;
import org.svenknockaert.smappee.telemetry.api.TelemetryIngestionService;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

import java.util.List;

@Component(service = TelemetryIngestionService.class)
public class TelemetryIngestionServiceImpl implements TelemetryIngestionService {

    private final TransactionManager transactionManager;
    private final MeterReadingRepository repository;
    private final MeterSampleValidator validator;

    @Activate
    public TelemetryIngestionServiceImpl(
            @Reference TransactionManager transactionManager,
            @Reference MeterReadingRepository repository,
            @Reference MeterSampleValidator validator) {
        this.transactionManager = transactionManager;
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public void ingestBatch(List<MeterSampleDto> samples) {
        if (samples == null || samples.isEmpty()) {
            throw new IllegalArgumentException("Batch samples cannot be null or empty");
        }

        for (MeterSampleDto sample : samples) {
            validator.validate(sample);
        }

        transactionManager.run(connection -> repository.saveBatch(connection, samples));
    }
}
