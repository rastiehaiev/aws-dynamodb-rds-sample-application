package com.rastiehaiev.aws.dynamodb.rds.sample;

import com.rastiehaiev.aws.dynamodb.rds.sample.model.*;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.dynamodb.DynamoDbCandidateRepository;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.rds.RdsCandidateRepository;
import org.apache.commons.collections4.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ApplicationIT.Initializer.class})
class ApplicationIT {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>()
            .withDatabaseName("candidate")
            .withUsername("rdsuser")
            .withPassword("password");

    @Container
    public static DynamoDbContainer<?> dynamoDbContainer = new DynamoDbContainer<>();

    private static final String SOURCE_ENDPOINT = "v1";
    private static final String DESTINATION_ENDPOINT = "v2";

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RdsCandidateRepository rdsCandidateRepository;
    @Autowired
    private DynamoDbCandidateRepository dynamoDbCandidateRepository;

    private RdsApi rdsApi = new RdsApi();
    private DynamoDbApi dynamoDbApi = new DynamoDbApi();

    @BeforeEach
    public void cleanUpDatabases() {
        rdsCandidateRepository.deleteAll();
        dynamoDbCandidateRepository.deleteAll();
    }

    @Test
    public void shouldReturnNoCandidates_whenNoCandidatesAddedToRds() {
        CandidatesResponse response = rdsApi.list();
        assertNotNull(response);
        assertEquals(0, response.getTotal());
    }

    @Test
    public void shouldReturnNoCandidates_whenNoCandidatesAddedToDynamoDb() {
        CandidatesResponse response = dynamoDbApi.list();
        assertNotNull(response);
        assertEquals(0, response.getTotal());
    }

    @Test
    public void shouldReturnNoDataForMigrationStatus_whenAttemptingToMigrateEmptyRds() {
        MigrationStatusResult migrationStatusResult = rdsApi.migrate();
        assertNotNull(migrationStatusResult);
        assertEquals(MigrationStatus.NO_DATA_FOR_MIGRATION, migrationStatusResult.getStatus());
    }

    @Test
    public void shouldReturnNoDataForMigrationStatus_whenAttemptingToMigrateEmptyDynamoDb() {
        MigrationStatusResult migrationStatusResult = dynamoDbApi.migrate();

        assertNotNull(migrationStatusResult);
        assertEquals(MigrationStatus.NO_DATA_FOR_MIGRATION, migrationStatusResult.getStatus());
    }

    @Test
    public void shouldCreateCandidateInRds() {
        Candidate createdCandidate = rdsApi.create(1);
        assertThat(createdCandidate).isNotNull();
        assertThat(createdCandidate.getUuid()).isNotNull();
    }

    @Test
    public void shouldCreateCandidateInDynamoDb() {
        Candidate createdCandidate = dynamoDbApi.create(1);
        assertThat(createdCandidate).isNotNull();
        assertThat(createdCandidate.getUuid()).isNotNull();
    }

    @Test
    public void migrateCandidatesInTwoDirection() {
        int size = 1000;

        List<UUID> createdInRdsCandidateUuids = IntStream.range(0, size)
                .boxed()
                .map(rdsApi::create)
                .map(Candidate::getUuid)
                .collect(Collectors.toList());
        assertThat(rdsApi.list().getTotal()).isEqualTo(size);

        List<UUID> createdInDynamoDbCandidateUuids = IntStream.range(0, size)
                .boxed()
                .map(dynamoDbApi::create)
                .map(Candidate::getUuid)
                .collect(Collectors.toList());
        assertThat(dynamoDbApi.list().getTotal()).isEqualTo(size);

        await().until(() -> {
            MigrationStatusResult statusResult = rdsApi.migrate();
            return statusResult.getStatus() == MigrationStatus.NO_DATA_FOR_MIGRATION;
        });

        await().until(() -> {
            MigrationStatusResult statusResult = dynamoDbApi.migrate();
            return statusResult.getStatus() == MigrationStatus.NO_DATA_FOR_MIGRATION;
        });

        List<UUID> wholeUuidsList = ListUtils.union(createdInRdsCandidateUuids, createdInDynamoDbCandidateUuids);

        List<UUID> rdsCandidateUuids = listCandidateUuids(rdsApi);
        assertThat(rdsCandidateUuids).hasSize(wholeUuidsList.size());
        rdsCandidateUuids.removeAll(wholeUuidsList);
        assertThat(rdsCandidateUuids).isEmpty();

        List<UUID> dynamoDbCandidateUuids = listCandidateUuids(dynamoDbApi);
        assertThat(dynamoDbCandidateUuids).hasSize(wholeUuidsList.size());
        dynamoDbCandidateUuids.removeAll(wholeUuidsList);
        assertThat(dynamoDbCandidateUuids).isEmpty();
    }

    private List<UUID> listCandidateUuids(Api api) {
        return api.list().getElements().stream().map(Candidate::getUuid).collect(Collectors.toList());
    }

    private class RdsApi extends Api {

        private RdsApi() {
            super(SOURCE_ENDPOINT, DESTINATION_ENDPOINT);
        }
    }

    private class DynamoDbApi extends Api {

        private DynamoDbApi() {
            super(DESTINATION_ENDPOINT, SOURCE_ENDPOINT);
        }
    }

    private class Api {

        private final String source;
        private final String destination;

        private Api(String source, String destination) {
            this.source = source;
            this.destination = destination;
        }

        public Candidate create(int number) {
            Candidate candidate = createTestCandidate(number);
            return restTemplate.postForObject(candidateEndpoint(source), candidate, Candidate.class);
        }

        public MigrationStatusResult migrate() {
            return restTemplate.patchForObject(candidateMigrationEndpoint(source, destination), null, MigrationStatusResult.class);
        }

        public CandidatesResponse list() {
            return restTemplate.getForObject(candidateEndpoint(source), CandidatesResponse.class);
        }
    }

    private String candidateMigrationEndpoint(String source, String destination) {
        return baseUrl() + "/candidate/migration/from/" + source + "/to/" + destination + "";
    }

    private String candidateEndpoint(String version) {
        return baseUrl() + "/" + version + "/candidate";
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    private Candidate createTestCandidate(int number) {
        ExperienceItem experienceItem = new ExperienceItem();
        experienceItem.setCompany("Google");
        experienceItem.setProject("GCP");
        experienceItem.setStartTimestamp(1460223160000L);

        Candidate candidate = new Candidate();
        candidate.setFirstName("Johnny");
        candidate.setLastName("Crocker_" + number);
        candidate.setProgrammingLanguage(ProgrammingLanguage.KOTLIN);
        candidate.setExperience(Collections.singletonList(experienceItem));
        return candidate;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            postgreSQLContainer.start();
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "amazon.dynamodb.endpoint: http://localhost:" + dynamoDbContainer.getPort()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}