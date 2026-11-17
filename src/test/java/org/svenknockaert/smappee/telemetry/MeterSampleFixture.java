package org.svenknockaert.smappee.telemetry;

import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

import java.time.Instant;

public final class MeterSampleFixture {

    private MeterSampleFixture() {
    }

    public static MeterSampleDto createWithCharger(String chargerId) {
        return createSample(chargerId, Instant.parse("2026-11-15T14:00:00Z"), 1500L, 25.5, 101L);
    }

    public static MeterSampleDto createSample(
            String chargerId,
            Instant eventTime,
            long activeEnergyWh,
            double temperature,
            long sequenceNumber) {
        return new MeterSampleDto(chargerId, eventTime, activeEnergyWh, temperature, sequenceNumber);
    }
}
