package org.svenknockaert.smappee.telemetry.api;

import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

public interface TelemetryIngestionService {

    void ingest(MeterSampleDto sample);
}
