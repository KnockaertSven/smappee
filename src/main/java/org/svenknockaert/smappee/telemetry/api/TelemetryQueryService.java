package org.svenknockaert.smappee.telemetry.api;

import java.time.Instant;
import java.util.Optional;
import org.svenknockaert.smappee.telemetry.api.dto.EnergyConsumptionResponse;
import org.svenknockaert.smappee.telemetry.api.dto.IntervalHistoryResponse;
import org.svenknockaert.smappee.telemetry.api.dto.LatestReadingResponse;

public interface TelemetryQueryService {

    Optional<LatestReadingResponse> getLatestReading(String chargerId);

    IntervalHistoryResponse getIntervalHistory(String chargerId, Instant targetInterval);

    EnergyConsumptionResponse getEnergyConsumed(String chargerId, Instant from, Instant to);
}
