package io.quarkiverse.chappie.sample;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/hello")
public class GreetingResource {

    @GET
    public String hello(@QueryParam("name") String name) {
        if (name != null) {
            return "Hello " + name;
        }
        return "Hello";
    }
}
