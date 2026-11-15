package org.svenknockaert.smappee.platform.internal;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.svenknockaert.smappee.platform.api.EchoPayload;

@Path("/api/echo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, scope = ServiceScope.PROTOTYPE)
public class EchoResource {

    @POST
    public Response echo(EchoPayload payload) {
        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok(payload).build();
    }
}
