package org.svenknockaert.smappee.telemetry.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.osgi.service.component.annotations.*;
import org.svenknockaert.smappee.telemetry.api.dto.IngestionResponse;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

@Path("/api/telemetry/samples")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class TelemetryIngestionResource {

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
    public IngestionResponse receiveSample(MeterSampleDto sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Payload cannot be empty");
        }

        if (ingestionService != null) {
            ingestionService.ingest(sample);
        }

        return new IngestionResponse("ACCEPTED", 1);
    }
}
