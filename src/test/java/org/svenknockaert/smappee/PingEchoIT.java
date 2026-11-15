package org.svenknockaert.smappee;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class PingEchoIT extends BaseIT {

    @Test
    void should_return_status_up_when_pinging() throws IOException, InterruptedException {
        //when
        var response = sendGet("/api/ping");

        //then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void should_echo_payload_when_posting_valid_json() throws IOException, InterruptedException {
        //given
        String requestJson = "{\"message\":\"hello Smappee\"}";

        //when
        var response = sendPost("/api/echo", requestJson);

        //then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"message\":\"hello Smappee\"");
    }

    @Test
    void should_reject_with_bad_request_when_posting_invalid_json() throws IOException, InterruptedException {
        //given
        String invalidJson = "{invalid-json-payload}";

        //when
        var response = sendPost("/api/echo", invalidJson);

        //then
        assertThat(response.statusCode()).isEqualTo(400);
    }
}
