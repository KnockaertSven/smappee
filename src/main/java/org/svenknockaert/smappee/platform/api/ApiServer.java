package org.svenknockaert.smappee.platform.api;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.Collection;

public final class ApiServer {

    private ApiServer() {
    }

    public static HttpServer start(int port, Collection<Object> resources) {
        return start("0.0.0.0", port, resources);
    }

    public static HttpServer start(String host, int port, Collection<Object> resources) {
        ResourceConfig config = new ResourceConfig()
                .register(JacksonFeature.class)
                .register(ObjectMapperContextResolver.class)
                .register(ValidationExceptionMapper.class);

        for (Object resource : resources) {
            config.register(resource);
        }

        URI baseUri = URI.create("http://" + host + ":" + port + "/");
        return JdkHttpServerFactory.createHttpServer(baseUri, config);
    }
}
