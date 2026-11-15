package org.svenknockaert.smappee.platform.internal;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.svenknockaert.smappee.platform.api.PingResponse;

@Path("/api/ping")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class PingResource {

    @GET
    public Response ping() {
        return Response.ok(new PingResponse("UP", Instant.now())).build();
    }
}
