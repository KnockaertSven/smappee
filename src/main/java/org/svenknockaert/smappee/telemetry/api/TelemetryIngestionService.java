package org.svenknockaert.smappee.telemetry.api;

import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

import java.util.List;

public interface TelemetryIngestionService {

    void ingestBatch(List<MeterSampleDto> samples);
}
