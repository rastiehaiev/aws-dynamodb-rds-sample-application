package com.rastiehaiev.aws.dynamodb.rds.sample.repository.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.rastiehaiev.aws.dynamodb.rds.sample.entity.dynamodb.DynamoDbCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DynamoDbCandidateRepositoryImpl {

    private final DynamoDBTemplate dynamoDBTemplate;

    public List<String> findUuidsForMigration() {
        DynamoDbCandidate hashKeyObject = new DynamoDbCandidate();
        DynamoDBQueryExpression<DynamoDbCandidate> queryExpression = new DynamoDBQueryExpression<DynamoDbCandidate>()
                .withIndexName("migrationStatusIndex")
                .withConsistentRead(false)
                .withHashKeyValues(hashKeyObject);

        return dynamoDBTemplate.query(DynamoDbCandidate.class, queryExpression).stream()
                .map(DynamoDbCandidate::getId)
                .collect(Collectors.toList());
    }
}
