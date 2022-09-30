package com.example.directory.config;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.api.OpenAPIConfigBuilder;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import springfox.boot.starter.autoconfigure.OpenApiAutoConfiguration;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import static io.swagger.v3.jaxrs2.integration.ServletConfigContextUtils.getContextIdFromServletConfig;

//@Provider
//public class SwaggerInfoBlackMagic implements Feature {
//    @Context
//    ServletConfig config;
//    @Context
//    Application app;
//
//    @Override
//    public boolean configure(FeatureContext context) {
//        //The aim here is to force construction of a (convincing) OpenApiContext before swagger does!
//        //This has been lifted from BaseOpenApiResource
//        String ctxId = getContextIdFromServletConfig(config);
//        try {
//            OpenApiContext ctx = new JaxrsOpenApiContextBuilder()
//                    .servletConfig(config)
//                    .application(app)
//                    //Might need more of these depending on your setup..
//                    //.resourcePackages(resourcePackages)
//                    //.configLocation(configLocation))
////                    .openApiConfiguration(getOpenApi())
////                    .openApiConfiguration()
//                    .ctxId(ctxId)
//                    .buildContext(true); //this also stores the instance statically
//        } catch (OpenApiConfigurationException e) {
//            throw new RuntimeException(e);
//        }
//        return true;
//    }
//}

public class SwaggerInfoBlackMagic {

}
