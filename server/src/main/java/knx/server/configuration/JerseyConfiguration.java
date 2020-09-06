package knx.server.configuration;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Component;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import knx.server.CorsFilter;
import knx.server.rest.DataGeneratorResource;
import knx.server.rest.DevicesResource;
import knx.server.rest.SystemResource;

@Component
@ApplicationPath("/knxmeister/api")
public class JerseyConfiguration extends ResourceConfig {

    /**
     * ctor
     */
    public JerseyConfiguration() {

        // CORS filter for angular
        register(CorsFilter.class);

        register(SystemResource.class);
        register(DevicesResource.class);
        register(DataGeneratorResource.class);

        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }

    @PostConstruct
    public void init() {
        this.configureSwagger();
    }

    /**
     * https://stackoverflow.com/questions/35966204/how-to-integrate-swagger-with-jersey-spring-boot
     * https://stackoverflow.com/questions/37640863/springfox-swagger-no-api-docs-with-spring-boot-jersey-and-gradle/42228055#42228055
     *
     * Test: http://localhost:8080/basic/api/swagger.json
     */
    private void configureSwagger() {

        // Available at localhost:port/swagger.json
        this.register(ApiListingResource.class);
        this.register(SwaggerSerializers.class);

        final BeanConfig config = new BeanConfig();
        config.setConfigId("JerseyConfiguration");
        config.setTitle("JerseyConfiguration");
        config.setVersion("v1");
        config.setContact("Me");
        config.setSchemes(new String[] { "http", "https" });
        config.setBasePath("/knxmeister/api");

        // this package path has to be the top-level java package of the Jersey
        // resources that you want to appear in the swagger.json
        config.setResourcePackage("knx.server");
        config.setPrettyPrint(true);
        config.setScan(true);
    }

}
