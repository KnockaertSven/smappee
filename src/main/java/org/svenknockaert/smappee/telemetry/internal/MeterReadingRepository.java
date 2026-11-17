package org.svenknockaert.smappee.telemetry.internal;

import org.osgi.service.component.annotations.Component;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(service = MeterReadingRepository.class)
public class MeterReadingRepository {

    private static final String INSERT_SQL = """
            INSERT INTO meter_readings (charger_id, event_time, sequence_number, active_energy_wh, temperature, ingestion_time)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE id = id
            """;

    private static final String FIND_BY_CHARGER_EVENT_SEQ_SQL = """
            SELECT id, charger_id, event_time, sequence_number, active_energy_wh, temperature, ingestion_time
            FROM meter_readings
            WHERE charger_id = ? AND event_time = ? AND sequence_number = ?
            """;

    private static final String FIND_BY_CHARGER_SQL = """
            SELECT id, charger_id, event_time, sequence_number, active_energy_wh, temperature, ingestion_time
            FROM meter_readings
            WHERE charger_id = ?
            ORDER BY event_time ASC, sequence_number ASC
            """;

    private static final String COUNT_BY_CHARGER_SQL = """
            SELECT COUNT(*) FROM meter_readings WHERE charger_id = ?
            """;

    public void save(Connection connection, MeterSampleDto sample) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            Instant now = Instant.now();
            statement.setString(1, sample.chargerId());
            statement.setTimestamp(2, Timestamp.from(sample.eventTime()));
            statement.setLong(3, sample.sequenceNumber());
            statement.setLong(4, sample.activeEnergyWh());
            statement.setDouble(5, sample.temperature());
            statement.setTimestamp(6, Timestamp.from(now));
            statement.executeUpdate();
        }
    }

    public Optional<StoredMeterReading> findByChargerEventAndSequence(
            Connection connection,
            String chargerId,
            Instant eventTime,
            long sequenceNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CHARGER_EVENT_SEQ_SQL)) {
            statement.setString(1, chargerId);
            statement.setTimestamp(2, Timestamp.from(eventTime));
            statement.setLong(3, sequenceNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    public List<StoredMeterReading> findByChargerId(Connection connection, String chargerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CHARGER_SQL)) {
            statement.setString(1, chargerId);

            try (ResultSet rs = statement.executeQuery()) {
                List<StoredMeterReading> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        }
    }

    public long countByChargerId(Connection connection, String chargerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COUNT_BY_CHARGER_SQL)) {
            statement.setString(1, chargerId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
    }

    private StoredMeterReading mapRow(ResultSet rs) throws SQLException {
        return new StoredMeterReading(
                rs.getLong("id"),
                rs.getString("charger_id"),
                rs.getTimestamp("event_time").toInstant(),
                rs.getLong("sequence_number"),
                rs.getLong("active_energy_wh"),
                rs.getDouble("temperature"),
                rs.getTimestamp("ingestion_time").toInstant());
    }
}
