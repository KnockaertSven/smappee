package org.svenknockaert.smappee.telemetry.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;
import org.svenknockaert.smappee.telemetry.api.dto.MeterSampleDto;

@Path("/api/telemetry/samples")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class TelemetryIngestionResource {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private TelemetryIngestionService ingestionService;

    @POST
    public MeterSampleDto receiveSample(MeterSampleDto sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Sample payload cannot be null");
        }

        if (ingestionService != null) {
            ingestionService.ingest(sample);
        }

        return sample;
    }
}
