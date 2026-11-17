package org.svenknockaert.smappee.telemetry.internal;

import org.osgi.service.component.annotations.Component;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

@Component(service = MeterSampleValidator.class)
public class MeterSampleValidator {

    private static final double MIN_TEMPERATURE = -50.0;
    private static final double MAX_TEMPERATURE = 120.0;

    public void validate(MeterSampleDto sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Meter sample payload cannot be null");
        }
        if (sample.chargerId() == null || sample.chargerId().isBlank()) {
            throw new IllegalArgumentException("Charger ID cannot be null or blank");
        }
        if (sample.eventTime() == null) {
            throw new IllegalArgumentException("Event time cannot be null");
        }
        if (sample.activeEnergyWh() < 0) {
            throw new IllegalArgumentException("Active energy cannot be negative");
        }
        if (Double.isNaN(sample.temperature()) || sample.temperature() < MIN_TEMPERATURE || sample.temperature() > MAX_TEMPERATURE) {
            throw new IllegalArgumentException(
                    "Temperature %s is outside physically valid bounds [%s, %s]".formatted(
                            sample.temperature(), MIN_TEMPERATURE, MAX_TEMPERATURE));
        }
    }
}
