package org.svenknockaert.smappee.telemetry.api.dto;

import java.time.Instant;
import java.util.List;

public record IntervalHistoryResponse(
        String chargerId,
        Instant targetInterval,
        List<IntervalRevisionEntry> revisions) {

    public record IntervalRevisionEntry(
            Instant ingestionTime,
            long activeEnergyWh,
            double temperature,
            long sequenceNumber) {
    }
}
