package org.svenknockaert.smappee.platform.internal;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.svenknockaert.smappee.platform.api.PingResponse;

@Path("/api/ping")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class PingResource {

    @GET
    public PingResponse ping() {
        return new PingResponse("UP", Instant.now());
    }
}
