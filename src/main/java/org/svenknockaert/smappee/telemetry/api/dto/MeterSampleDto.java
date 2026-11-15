package org.svenknockaert.smappee.telemetry.api.dto;

import java.time.Instant;

public record MeterSampleDto(
        String chargerId,
        Instant eventTime,
        long activeEnergyWh,
        double temperature,
        long sequenceNumber) {
}
