package com.example.directory.config;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// This
@Component
@Primary
public class CombinedSwaggerResourcesProvider implements SwaggerResourcesProvider {

    @Resource
    private InMemorySwaggerResourcesProvider inMemorySwaggerResourcesProvider;

    @Override
    public List<SwaggerResource> get() {

        SwaggerResource jerseySwaggerResource = new SwaggerResource();
//        jerseySwaggerResource.setLocation("/api/swagger.json");
//        jerseySwaggerResource.setSwaggerVersion("2.0");
        jerseySwaggerResource.setLocation("/api/openapi.json");
        jerseySwaggerResource.setName("Jersey");

        return Stream.concat(Stream.of(jerseySwaggerResource), inMemorySwaggerResourcesProvider.get().stream()).collect(Collectors.toList());
    }
}

//@Component
//@Primary
//public class CombinedSwaggerResourcesProvider implements SwaggerResourcesProvider {
//    @Resource
//    private InMemorySwaggerResourcesProvider inMemorySwaggerResourcesProvider;
//
//    @Override
//    public List<SwaggerResource> get() {
//
//        SwaggerResource jerseySwaggerResource = new SwaggerResource();
//        jerseySwaggerResource.setLocation("/api/openapi.json");
//        jerseySwaggerResource.setSwaggerVersion("3.0.1");
//        jerseySwaggerResource.setName("Jersey");
//
//        return Stream.concat(Stream.of(jerseySwaggerResource), inMemorySwaggerResourcesProvider.get().stream()).collect(Collectors.toList());
//    }
//}

//public class CombinedSwaggerResourcesProvider {
//
//}