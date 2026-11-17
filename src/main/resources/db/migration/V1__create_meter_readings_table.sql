CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    charger_id VARCHAR(64) NOT NULL,
    event_time TIMESTAMP(3) NOT NULL,
    sequence_number BIGINT NOT NULL,
    active_energy_wh BIGINT NOT NULL,
    temperature DOUBLE NOT NULL,
    ingestion_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT uk_charger_event_seq UNIQUE KEY (charger_id, event_time, sequence_number),
    INDEX idx_bitemporal_lookup (charger_id, event_time, ingestion_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
