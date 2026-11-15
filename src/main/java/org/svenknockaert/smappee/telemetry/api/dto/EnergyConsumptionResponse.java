package org.svenknockaert.smappee.telemetry.api.dto;

import java.time.Instant;

public record EnergyConsumptionResponse(
        String chargerId,
        Instant from,
        Instant to,
        long totalEnergyConsumedWh) {
}
