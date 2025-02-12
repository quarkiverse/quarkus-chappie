package io.quarkiverse.chappie.sample;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

/**
 * A resource class that handles greeting requests.
 * It provides an endpoint to greet users with a customizable name.
 */
@Path("/hello")
public class GreetingResource {

    /**
     * Returns a greeting message.
     * If a name is provided as a query parameter, it will be included in the greeting.
     *
     * @param name the name of the person to greet, can be null
     * @return a greeting message
     */
    @GET
    public String hello(@QueryParam("name") String name) {
        if (name != null) {
            return "Hello " + name;
        }
        return "Hello";
    }
}
