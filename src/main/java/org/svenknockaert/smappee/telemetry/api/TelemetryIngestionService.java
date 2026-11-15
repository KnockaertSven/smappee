package org.svenknockaert.smappee.telemetry.api;

import java.util.List;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

public interface TelemetryIngestionService {

    void ingest(MeterSampleDto sample);

    void ingestBatch(List<MeterSampleDto> samples);
}
