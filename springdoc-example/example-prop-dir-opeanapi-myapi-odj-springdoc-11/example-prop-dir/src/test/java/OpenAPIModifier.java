import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.ReaderListener;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * This test uses Atlassian's OpenApiValidationFilter to validate our code against our OpenAPI Swagger spec file, which
 * is describing our APIs.
 * It is using a custom OpenAPIModifer.class to modify our OpenAPI Swagger spec file before it runs the tests.
 * This is so, because the OpenAPI validator will check for all possible components and properties and fail if any one
 * of them is null or missing. This would mean we have to mock basically everything which is not feasible.
 * So, instead OpenAPIModifer.class adds the "nullable" property to our OpenAPI spec, so that we can test only for the
 * paths, components, schemas etc. which we are currently interested in in any given test.
 */
@Component
@OpenAPIDefinition
public class OpenAPIModifier implements ReaderListener {

    @Override
    public void beforeScan(Reader reader, OpenAPI openAPI) {
    }

    @Override
    public void afterScan(Reader reader, OpenAPI openAPI) {
        modifyOpenAPIWithNullable(openAPI);
    }

    public OpenAPI modifyOpenAPIWithNullable(OpenAPI openAPI) {
        Components comps = openAPI.getComponents();
        comps.setSchemas(makeAllSchemasNullable(openAPI));
        openAPI.setComponents(comps);

        return openAPI;
    }

    private Map<String, Schema> makeAllSchemasNullable(OpenAPI openAPI) {

        Map<String, Schema> nullableSchemas = new HashMap<>();

        if (openAPI != null && openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            for (Map.Entry<String, Schema> entry : openAPI.getComponents().getSchemas().entrySet()) {
                Schema temporarySchema = entry.getValue();
                temporarySchema.setNullable(true);

                temporarySchema.setProperties(makeAllSchemaPropertiesNullable(temporarySchema));

                nullableSchemas.put(entry.getKey(), temporarySchema);
            }
        }

        return nullableSchemas;
    }

    private Map<String, Schema> makeAllSchemaPropertiesNullable(Schema schema) {

        Map<String, Schema> properties = schema.getProperties();

        if (properties != null) {
            for (Map.Entry<String, Schema> propertiesEntry : properties.entrySet()) {
                Schema propSchemaTemp = propertiesEntry.getValue();
                propSchemaTemp.setNullable(true);
                properties.put(propertiesEntry.getKey(), propSchemaTemp);
            }
        }

        return properties;
    }
}
