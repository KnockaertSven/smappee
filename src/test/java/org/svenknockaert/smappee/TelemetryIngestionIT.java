package org.svenknockaert.smappee;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.svenknockaert.smappee.telemetry.MeterSampleFixture.createSample;
import static org.svenknockaert.smappee.telemetry.MeterSampleFixture.createWithCharger;

class TelemetryIngestionIT extends BaseIT {

    @Test
    void should_accept_single_meter_sample_when_posting_valid_json() throws IOException, InterruptedException {
        //given
        var sample = createWithCharger("charger-single");
        String json = MAPPER.writeValueAsString(sample);

        //when
        var response = sendPost("/api/telemetry/samples", json);

        //then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void should_accept_batch_samples_when_posting_valid_json_array() throws IOException, InterruptedException, SQLException {
        //given
        String chargerId = "charger-batch";
        var sample1 = createSample(
                chargerId, Instant.parse("2026-11-15T14:00:00Z"), 1000L, 22.0, 1L);
        var sample2 = createSample(
                chargerId, Instant.parse("2026-11-15T14:05:00Z"), 1200L, 22.5, 2L);
        String json = MAPPER.writeValueAsString(List.of(sample1, sample2));

        //when
        var response = sendPost("/api/telemetry/samples", json);

        //then
        assertThat(response.statusCode()).isEqualTo(200);
        try (Connection connection = dataSource.getConnection()) {
            assertThat(meterReadingRepository.countByChargerId(connection, chargerId)).isEqualTo(2);
        }
    }

    @Test
    void should_accept_batch_envelope_when_posting_wrapped_samples() throws IOException, InterruptedException {
        //given
        var sample = createWithCharger("charger-envelope");
        String json = "{\"samples\": [" + MAPPER.writeValueAsString(sample) + "]}";

        //when
        var response = sendPost("/api/telemetry/samples", json);

        //then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void should_deduplicate_idempotently_without_duplicating_records_when_replaying_identical_sample()
            throws IOException, InterruptedException, SQLException {
        //given
        String chargerId = "charger-replay";
        var sample = createSample(
                chargerId, Instant.parse("2026-11-15T14:00:00Z"), 2500L, 24.0, 10L);
        String json = MAPPER.writeValueAsString(sample);

        var firstResponse = sendPost("/api/telemetry/samples", json);
        assertThat(firstResponse.statusCode()).isEqualTo(200);

        Instant originalIngestionTime;
        try (Connection connection = dataSource.getConnection()) {
            var reading = meterReadingRepository.findByChargerEventAndSequence(
                    connection, chargerId, sample.eventTime(), sample.sequenceNumber());
            assertThat(reading).isPresent();
            originalIngestionTime = reading.get().ingestionTime();
        }

        //when
        var replayResponse = sendPost("/api/telemetry/samples", json);

        //then
        assertThat(replayResponse.statusCode()).isEqualTo(200);
        try (Connection connection = dataSource.getConnection()) {
            assertThat(meterReadingRepository.countByChargerId(connection, chargerId)).isEqualTo(1);
            var readingAfterReplay = meterReadingRepository.findByChargerEventAndSequence(
                    connection, chargerId, sample.eventTime(), sample.sequenceNumber());
            assertThat(readingAfterReplay).isPresent();
            assertThat(readingAfterReplay.get().ingestionTime()).isEqualTo(originalIngestionTime);
        }
    }

    @Test
    void should_persist_revision_as_new_record_when_same_interval_has_new_sequence()
            throws IOException, InterruptedException, SQLException {
        //given
        String chargerId = "charger-revision";
        Instant eventTime = Instant.parse("2026-11-15T14:00:00Z");
        var originalSample = createSample(chargerId, eventTime, 3000L, 21.0, 100L);
        var revisedSample = createSample(chargerId, eventTime, 3500L, 21.5, 105L);

        var firstResponse = sendPost("/api/telemetry/samples", MAPPER.writeValueAsString(originalSample));
        assertThat(firstResponse.statusCode()).isEqualTo(200);

        //when
        var revisionResponse = sendPost("/api/telemetry/samples", MAPPER.writeValueAsString(revisedSample));

        //then
        assertThat(revisionResponse.statusCode()).isEqualTo(200);
        try (Connection connection = dataSource.getConnection()) {
            assertThat(meterReadingRepository.countByChargerId(connection, chargerId)).isEqualTo(2);
            var readings = meterReadingRepository.findByChargerId(connection, chargerId);
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).sequenceNumber()).isEqualTo(100L);
            assertThat(readings.get(1).sequenceNumber()).isEqualTo(105L);
        }
    }

    @Test
    void should_reject_with_bad_request_and_rollback_batch_when_any_sample_has_negative_active_energy()
            throws IOException, InterruptedException, SQLException {
        //given
        String chargerId = "charger-rollback";
        var validSample = createSample(
                chargerId, Instant.parse("2026-11-15T14:00:00Z"), 1000L, 20.0, 1L);
        var invalidSample = createSample(
                chargerId, Instant.parse("2026-11-15T14:05:00Z"), -50L, 20.0, 2L);
        String json = MAPPER.writeValueAsString(List.of(validSample, invalidSample));

        //when
        var response = sendPost("/api/telemetry/samples", json);

        //then
        assertThat(response.statusCode()).isEqualTo(400);
        try (Connection connection = dataSource.getConnection()) {
            assertThat(meterReadingRepository.countByChargerId(connection, chargerId)).isZero();
        }
    }

    @Test
    void should_reject_with_bad_request_when_posting_abnormal_temperature() throws IOException, InterruptedException {
        //given
        var abnormalSample = createSample(
                "charger-temp", Instant.parse("2026-11-15T14:00:00Z"), 1000L, 150.0, 1L);
        String json = MAPPER.writeValueAsString(abnormalSample);

        //when
        var response = sendPost("/api/telemetry/samples", json);

        //then
        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void should_reject_with_bad_request_when_posting_malformed_json() throws IOException, InterruptedException {
        //given
        String malformedJson = "{not-valid-json}";

        //when
        var response = sendPost("/api/telemetry/samples", malformedJson);

        //then
        assertThat(response.statusCode()).isEqualTo(400);
    }
}
