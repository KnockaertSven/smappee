package org.svenknockaert.smappee;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PingIT extends BaseIT {

    @Test
    void should_return_status_up_when_pinging() throws IOException, InterruptedException {
        //when
        var response = sendGet("/api/ping");

        //then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }
}
