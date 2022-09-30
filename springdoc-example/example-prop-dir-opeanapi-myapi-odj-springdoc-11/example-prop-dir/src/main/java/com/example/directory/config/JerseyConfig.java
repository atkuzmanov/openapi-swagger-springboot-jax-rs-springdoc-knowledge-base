package com.example.directory.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.example.example.prop.dir.resource.PropertyResource;
import com.example.example.prop.dir.resource.exceptions.IllegalArgumentExceptionMapper;
import com.example.example.prop.dir.resource.exceptions.PropertyWriteProtectionExceptionMapper;
import com.example.example.prop.dir.resource.exceptions.ResourceNotFoundExceptionMapper;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(ObjectMapper objectMapper) {
        // register endpoints
        register(PropertyResource.class);
        // register jackson for json
        register(new ObjectMapperContextResolver(objectMapper));
        register(ResourceNotFoundExceptionMapper.class);
        register(IllegalArgumentExceptionMapper.class);
        register(PropertyWriteProtectionExceptionMapper.class);
        // register OpenApi resources for generation of OpenApi swagger spec
        register(OpenApiResource.class);
        register(AcceptHeaderOpenApiResource.class);
    }

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
