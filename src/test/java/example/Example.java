/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.websecurity.CorsConfiguration;
import com.palantir.websecurity.WebSecurityBundle;
import com.palantir.websecurity.WebSecurityConfigurable;
import com.palantir.websecurity.WebSecurityConfiguration;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public final class Example {

    public static final String EXAMPLES_RESOURCE_RESPONSE = "hello world";
    public static final String CSP_FROM_APP = "csp from app default";
    public static final String CTO_FROM_APP = "cto should be overridden";
    public static final String CTO_FROM_YML = "cto is overridden yay!"; // appears in the example.yml file

    private Example() {
        // utility class for testing
    }

    public static void main(String[] args) throws Exception {
        new ExampleApplication().run(args);
    }

    @Path("hello")
    public static final class ExampleResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public String helloWorld() {
            return EXAMPLES_RESOURCE_RESPONSE;
        }
    }

    public static final class ExampleConfiguration extends Configuration implements WebSecurityConfigurable {

        @JsonProperty("webSecurity")
        @NotNull
        @Valid
        private final WebSecurityConfiguration webSecurity = WebSecurityConfiguration.DEFAULT;

        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return this.webSecurity;
        }
    }

    public static final class ExampleApplication extends Application<ExampleConfiguration> {

        private final WebSecurityConfiguration webSecurityDefaults = WebSecurityConfiguration.builder()

                // set app defaults for different header values
                .contentSecurityPolicy(CSP_FROM_APP)
                .contentTypeOptions(CTO_FROM_APP)

                // CORS is still DISABLED, since the allowedOrigins is not set, but the default value will be
                // respected if it's ever turned on
                .cors(CorsConfiguration.builder()
                        .preflightMaxAge(60 * 10)
                        .build())

                .build();

        private final WebSecurityBundle webSecurityBundle = new WebSecurityBundle(webSecurityDefaults);

        @Override
        public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
            bootstrap.addBundle(webSecurityBundle);
            bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        }

        @Override
        public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
            environment.jersey().register(new ExampleResource());
        }
    }
}
