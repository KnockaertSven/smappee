package org.svenknockaert.smappee.telemetry.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;
import org.svenknockaert.smappee.telemetry.api.dto.EnergyConsumptionResponse;
import org.svenknockaert.smappee.telemetry.api.dto.IntervalHistoryResponse;
import org.svenknockaert.smappee.telemetry.api.dto.LatestReadingResponse;

@Path("/api/chargers")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class TelemetryQueryResource {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private TelemetryQueryService queryService;

    @GET
    @Path("/{id}/latest")
    public LatestReadingResponse getLatestReading(@PathParam("id") String chargerId) {
        if (queryService != null) {
            return queryService.getLatestReading(chargerId)
                    .orElseGet(() -> new LatestReadingResponse(chargerId, Instant.now(), Instant.now(), 0L, 25.0));
        }

        return new LatestReadingResponse(chargerId, Instant.now(), Instant.now(), 0L, 25.0);
    }

    @GET
    @Path("/{id}/history")
    public IntervalHistoryResponse getIntervalHistory(
            @PathParam("id") String chargerId,
            @QueryParam("interval") String interval) {
        Instant targetInterval = interval != null ? Instant.parse(interval) : Instant.now();
        if (queryService != null) {
            return queryService.getIntervalHistory(chargerId, targetInterval);
        }

        return new IntervalHistoryResponse(chargerId, targetInterval, List.of());
    }

    @GET
    @Path("/{id}/energy")
    public EnergyConsumptionResponse getEnergyConsumed(
            @PathParam("id") String chargerId,
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        Instant fromTime = from != null ? Instant.parse(from) : Instant.now();
        Instant toTime = to != null ? Instant.parse(to) : Instant.now();
        if (queryService != null) {
            return queryService.getEnergyConsumed(chargerId, fromTime, toTime);
        }

        return new EnergyConsumptionResponse(chargerId, fromTime, toTime, 0L);
    }
}
