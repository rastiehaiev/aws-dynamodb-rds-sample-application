package com.rastiehaiev.aws.dynamodb.rds.sample.configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.rastiehaiev.aws.dynamodb.rds.sample.coordinator.EndpointCoordinator;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.impl.DynamoDbCandidateService;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.impl.RdsCandidateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.rastiehaiev.aws.dynamodb.rds.sample.constants.ProjectConstants.CANDIDATE_TABLE_NAME;
import static com.rastiehaiev.aws.dynamodb.rds.sample.constants.ProjectConstants.MIGRATION_STATUS_GLOBAL_INDEX_NAME;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = "com.rastiehaiev.aws.dynamodb.rds.sample.repository.rds")
@EnableDynamoDBRepositories(basePackages = "com.rastiehaiev.aws.dynamodb.rds.sample.repository.dynamodb")
public class ApplicationConfiguration {

    private static final long DEFAULT_READ_CAPACITY_UNITS = 5L;
    private static final long DEFAULT_WRITE_CAPACITY_UNITS = 5L;

    private static final String ID_ATTRIBUTE = "id";
    private static final String MIGRATION_STATUS_ATTRIBUTE = "migrationStatus";

    @Value("${application.migration.threads-count:}")
    private Integer threadsCount;

    @Value("${amazon.dynamodb.endpoint:}")
    private String dynamoDbEndpoint;

    @Value("${amazon.aws.accessKey}")
    private String awsAccessKey;

    @Value("${amazon.aws.secretKey}")
    private String awsSecretKey;

    @Bean
    public EndpointCoordinator endpointCoordinator(RdsCandidateService rdsCandidateService,
                                                   DynamoDbCandidateService dynamoDbCandidateService) {

        EndpointCoordinator endpointCoordinator = new EndpointCoordinator();
        endpointCoordinator.register("v1", rdsCandidateService);
        endpointCoordinator.register("v2", dynamoDbCandidateService);
        return endpointCoordinator;
    }

    @Bean
    public ThreadPoolExecutor migrationThreadPoolExecutor() {
        if (threadsCount == null) {
            threadsCount = Runtime.getRuntime().availableProcessors();
        }
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(threadsCount);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()));
        if (StringUtils.isNoneBlank(dynamoDbEndpoint)) {
            builder = builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, null));
        }
        return builder.build();
    }

    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
    }

    @Bean
    public CommandLineRunner createDynamoDbTable(DynamoDBMapper mapper) {
        return args -> {

            AttributeDefinition idAttribute = new AttributeDefinition()
                    .withAttributeName(ID_ATTRIBUTE)
                    .withAttributeType(ScalarAttributeType.S);

            AttributeDefinition migratedAttribute = new AttributeDefinition()
                    .withAttributeName(MIGRATION_STATUS_ATTRIBUTE)
                    .withAttributeType(ScalarAttributeType.S);

            KeySchemaElement idKeyElement = new KeySchemaElement()
                    .withAttributeName(ID_ATTRIBUTE)
                    .withKeyType(KeyType.HASH);

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(CANDIDATE_TABLE_NAME)
                    .withKeySchema(List.of(idKeyElement))
                    .withGlobalSecondaryIndexes(generateMigratedIndex())
                    .withAttributeDefinitions(List.of(idAttribute, migratedAttribute))
                    .withProvisionedThroughput(new ProvisionedThroughput()
                            .withReadCapacityUnits(DEFAULT_READ_CAPACITY_UNITS)
                            .withWriteCapacityUnits(DEFAULT_WRITE_CAPACITY_UNITS));

            boolean created = TableUtils.createTableIfNotExists(amazonDynamoDB(), request);
            if (created) {
                TableUtils.waitUntilActive(amazonDynamoDB(), CANDIDATE_TABLE_NAME);
                log.info("Table '{}' has been just created.", CANDIDATE_TABLE_NAME);
            }
        };
    }

    private GlobalSecondaryIndex generateMigratedIndex() {

        KeySchemaElement migratedElement = new KeySchemaElement()
                .withAttributeName(MIGRATION_STATUS_ATTRIBUTE)
                .withKeyType(KeyType.HASH);

        KeySchemaElement idElement = new KeySchemaElement()
                .withAttributeName(ID_ATTRIBUTE)
                .withKeyType(KeyType.RANGE);

        Projection projection = new Projection().withProjectionType(ProjectionType.KEYS_ONLY);

        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
                .withReadCapacityUnits(DEFAULT_READ_CAPACITY_UNITS)
                .withWriteCapacityUnits(DEFAULT_WRITE_CAPACITY_UNITS);

        return new GlobalSecondaryIndex()
                .withIndexName(MIGRATION_STATUS_GLOBAL_INDEX_NAME)
                .withKeySchema(List.of(migratedElement, idElement))
                .withProvisionedThroughput(provisionedThroughput)
                .withProjection(projection);
    }
}
