package com.example.directory.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(ObjectMapper objectMapper) {
        // Access through /<Jersey's servlet path>/application.wadl
        this.register(WadlResource.class);

        // register jackson for json
        register(new ObjectMapperContextResolver(objectMapper));

        // This
        register(OpenApiResource.class);

        BeanConfig swaggerConfig = new BeanConfig();
        swaggerConfig.setBasePath("/api");
        SwaggerConfigLocator.getInstance().putConfig(SwaggerContextService.CONFIG_ID_DEFAULT, swaggerConfig);

        packages(getClass().getPackage().getName(), ApiListingResource.class.getPackage().getName());

        register(SwaggerInfoBlackMagic.class);

        register(OpenApiResource.class);
    }

//    @Value("${spring.jersey.application-path:/api}")
//    private String apiPath;

//    @PostConstruct
//    public void init() {
//        // Register components where DI is needed
//        this.configureSwagger();
//    }

//    private void configureSwagger() {
//        // Available at localhost:port/swagger.json
//        this.register(ApiListingResource.class);
//        this.register(SwaggerSerializers.class);
//
////        BeanConfig config = new BeanConfig();
////        config.setConfigId("springboot-jersey-swagger-docker-example");
////        config.setTitle("Spring Boot + Jersey + Swagger + Docker Example");
////        config.setVersion("v1");
////        config.setContact("Orlando L Otero");
////        config.setSchemes(new String[] { "http", "https" });
////        config.setBasePath(this.apiPath);
////        config.setResourcePackage("com.asimio.jerseyexample.rest.v1");
////        config.setPrettyPrint(true);
////        config.setScan(true);
//    }


    @Provider
    public static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
        private final ObjectMapper mapper;

        public ObjectMapperContextResolver(ObjectMapper mapper) {
            this.mapper = mapper;
            this.mapper.findAndRegisterModules();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }
}
