package org.svenknockaert.smappee.telemetry.api.dto;

import java.time.Instant;

public record LatestReadingResponse(
        String chargerId,
        Instant eventTime,
        Instant ingestionTime,
        long activeEnergyWh,
        double temperature) {
}
