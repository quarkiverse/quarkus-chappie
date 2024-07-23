package io.quarkiverse.chappie.sample;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @GET
    @Path("/error")
    @Produces(MediaType.TEXT_PLAIN)
    public String error() {
        // inter var;
        return "Hello from Quarkus REST";
    }

    @GET
    @Path("/exception")
    @Produces(MediaType.TEXT_PLAIN)
    public String exception() {
        throw new RuntimeException("Just an exception :)", new RuntimeException("root cause"));
    }

}
