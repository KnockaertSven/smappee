package org.svenknockaert.smappee.telemetry.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.osgi.service.component.annotations.*;
import org.svenknockaert.smappee.telemetry.api.dto.IngestionResponse;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

import java.util.List;

@Path("/api/telemetry/samples")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class TelemetryIngestionResource {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private TelemetryIngestionService ingestionService;

    public TelemetryIngestionResource() {
    }

    @Activate
    public TelemetryIngestionResource(
            @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
            TelemetryIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @POST
    public IngestionResponse receiveSamples(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload cannot be empty");
        }

        JsonNode tree;
        try {
            tree = MAPPER.readTree(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed JSON payload: " + e.getMessage(), e);
        }

        List<MeterSampleDto> samples;
        try {
            if (tree.isArray()) {
                samples = MAPPER.readerForListOf(MeterSampleDto.class).readValue(tree);
            } else if (tree.isObject()) {
                if (tree.has("samples") && tree.get("samples").isArray()) {
                    samples = MAPPER.readerForListOf(MeterSampleDto.class).readValue(tree.get("samples"));
                } else {
                    samples = List.of(MAPPER.treeToValue(tree, MeterSampleDto.class));
                }
            } else {
                throw new IllegalArgumentException("Expected JSON object or array");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed JSON payload: " + e.getMessage(), e);
        }

        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Samples list cannot be empty");
        }

        if (ingestionService != null) {
            ingestionService.ingestBatch(samples);
        }

        return new IngestionResponse("ACCEPTED", samples.size());
    }
}
