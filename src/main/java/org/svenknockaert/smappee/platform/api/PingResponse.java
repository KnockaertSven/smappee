package org.svenknockaert.smappee.platform.api;

import java.time.Instant;

public record PingResponse(String status, Instant timestamp) {
}
