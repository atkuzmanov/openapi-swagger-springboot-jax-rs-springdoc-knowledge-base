import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.example.prop.dir.domain.BusinessUnit;
import com.example.example.prop.dir.domain.Property;
import com.example.example.prop.dir.domain.TechnicalPlace;
import com.example.example.prop.dir.model.AddressData;
import com.example.example.prop.dir.mongodb.document.PropertyDocument;
import com.example.example.prop.dir.service.AuthenticationProvider;
import com.example.example.prop.dir.service.property.PropertyCreateService;
import com.palantir.docker.compose.DockerComposeExtension;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * This test uses Atlassian's OpenApiValidationFilter to validate our code against our OpenAPI Swagger spec file, which
 * is describing our APIs.
 * It is using a custom OpenAPIModifer.class to modify our OpenAPI Swagger spec file before it runs the tests.
 * This is so, because the OpenAPI validator will check for all possible components and properties and fail if any one
 * of them is null or missing. This would mean we have to mock basically everything which is not feasible.
 * So, instead OpenAPIModifer.class adds the "nullable" property to our OpenAPI spec, so that we can test only for the
 * paths, components, schemas etc. which we are currently interested in in any given test.
 * TODO: Write more tests.
 * <p>
 * As of the time of writing this comment the test is written as an integration test which starts up the application in
 * order to carry out the tests.
 * At the moment this start up is working locally but it  is failing our builds as the application fails to start up
 * properly in our pipelines.
 * Currently this test should be disabled and should be run only locally for validating our code against our OpenAPI
 * Swagger spec file, which is describing our APIs.
 * TODO: Find a way to fix this, so that this test can run in our pipelines as well.
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "nonAsync"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext()
@EnableAspectJAutoProxy
public class InboundSwaggerOpenApiTestIT {

    private static final String BASE_URL = "/api/property";

    private final List<String> KI_USER_ROLES = List.of("example-ki-example-confidential-reader");

    public static final String SWAGGER_JSON_URL = "static/swagger-openapi-specs/example-property-dir-inbound/example-property-directory-inbound-openapi-swagger-spec.json";

    private OpenApiValidationFilter validationFilter;

    @TestConfiguration
    static class TestAsyncConfig extends AsyncConfigurerSupport {

        public Executor getAsyncExecutor() {
            return new SyncTaskExecutor();
        }

    }

    @RegisterExtension
    static final DockerComposeExtension docker = DockerComposeExtension.builder()
            .pullOnStartup(true)
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("mongodb", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("zookeeper", HealthChecks.toHaveAllPortsOpen())
            .build();

    @SpyBean
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private Geocoder geocoderMock;

    @Autowired
    private PropertyCreateService propertyCreateService;

    @Autowired
    private OpenAPIModifier openAPIModifier;

    @LocalServerPort
    private int randomPort;

    @BeforeAll
    static void updateEnv() {
        System.setProperty("example.cluster.zookeeper.host", docker.containers().container("zookeeper")
                .port(2181)
                .inFormat("$HOST:$EXTERNAL_PORT"));
    }

    @BeforeEach
    void setup() {
        mongoTemplate.indexOps(PropertyDocument.class).ensureIndex(new GeospatialIndex("location")
                .typed(GeoSpatialIndexType.GEO_2DSPHERE));
        mongoTemplate.remove(new Query(), com.example.example.prop.dir.model.Property.class);

        RestAssured.port = randomPort;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api";
        RestAssured.defaultParser = Parser.JSON;

        modifyOpenAPISpecWithNullable();
    }

    @Test
    void testPropertyEndpoint() {

        doReturn(KI_USER_ROLES).when(authenticationProvider).fetchUserRoles();

        when(geocoderMock.geocode(ArgumentMatchers.any(AddressData.class)))
                .thenReturn(GeocodingResult
                        .builder()
                        .latitude(42d)
                        .longitude(12d)
                        .build());

        Property property = createSingleProperty();

        ResponseEntity<Property> response = restTemplate.postForEntity(BASE_URL, property, Property.class);

        Property savedProperty = response.getBody();
        response = restTemplate.getForEntity(BASE_URL + "/" + savedProperty.getId(), Property.class);
        savedProperty = response.getBody();
        assertThat(Objects.requireNonNull(savedProperty).getCoordinates().getLatitude(), is(42d));
        assertThat(savedProperty.getCoordinates().getLongitude(), is(12d));

        /* This is an alternative way to the above for creating a property without using the RestTemplate.
         * This might be useful if using WireMock is necessary as you need to bind WireMock to a RestTemplate and
         * having more than one RestTemplate might cause some problems.
         * I faced some difficulties and strange behaviour when I tried using WireMock, so leaving this comment until
         * the "TO-DO" at the top of this class about writing more test is resolved.
         * Property testProp = mongoTemplate.insert(createSingleProperty());
         */

        given()
                .filter(validationFilter)
                .port(randomPort)
                .header("accept", "application/json")
                .when().get("/property/" + savedProperty.getId())
                .then()
                .statusCode(200);
    }

    /* This is a shorthand used for getting the application port configured for the application instead of the
     * rendom port for running the tests. This is still needed until the "TO-DOs" at the top of this class are resolved.
     */
    private int getAppPort() {
        return 8120;
    }

    private void modifyOpenAPISpecWithNullable() {

        File file = new File("src/main/resources/" + SWAGGER_JSON_URL);
        JsonNode jsonNode = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonNode = mapper.readValue(file, JsonNode.class);
            OpenAPI oas;
            String swaggerJson = Json.mapper().writeValueAsString(jsonNode);
            assertTrue(swaggerJson.contains("openapi"));
            assertTrue(swaggerJson.contains("servers"));
            assertTrue(swaggerJson.contains("paths"));
            assertTrue(swaggerJson.contains("components"));
            OpenAPI rebuilt = Json.mapper().readValue(swaggerJson, OpenAPI.class);
            oas = openAPIModifier.modifyOpenAPIWithNullable(rebuilt);

            String oasModified = Json.mapper().writeValueAsString(oas);

            validationFilter = new OpenApiValidationFilter(oasModified);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Property createSingleProperty() {
        return Property.builder().build();
    }
}
