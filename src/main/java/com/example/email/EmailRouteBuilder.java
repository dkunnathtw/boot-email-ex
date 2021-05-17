package com.example.email;

import java.util.UUID;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EmailRouteBuilder extends RouteBuilder {

    public static final String ENDPOINT = "direct:testMailerEndpoint";

    @Override
    public void configure() throws Exception {

        //This is a direct endpoint for purposes of this example, but can be from any of the inputs that Camel EIP supports.
        from(ENDPOINT)
            .doTry()
                .setHeader("subject", constant("Hello, world!"))
                .setHeader("to", constant("user@example.com"))
                .setHeader("java.smtp.from", method(UUID.class, "randomUUID"))
                .to("smtp://127.0.0.1:3025?username=spring&password=spring")
            .doCatch(Exception.class)
                .log(String.format("Caught an exception on %s", ENDPOINT))
            .end();
    }

    
    
}
