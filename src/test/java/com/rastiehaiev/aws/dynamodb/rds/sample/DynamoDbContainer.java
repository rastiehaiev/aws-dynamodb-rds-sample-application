package com.rastiehaiev.aws.dynamodb.rds.sample;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class DynamoDbContainer<SELF extends PostgreSQLContainer<SELF>> extends GenericContainer<SELF> {

    private static final int DYNAMO_DB_PORT = 8000;

    public DynamoDbContainer() {
        super("amazon/dynamodb-local");
        addExposedPort(DYNAMO_DB_PORT);
    }

    public int getPort() {
        return getMappedPort(DYNAMO_DB_PORT);
    }
}
