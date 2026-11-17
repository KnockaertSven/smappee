package org.svenknockaert.smappee.telemetry.internal;

import java.time.Instant;

public record StoredMeterReading(
        long id,
        String chargerId,
        Instant eventTime,
        long sequenceNumber,
        long activeEnergyWh,
        double temperature,
        Instant ingestionTime) {
}
