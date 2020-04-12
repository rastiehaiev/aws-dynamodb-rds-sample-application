package com.rastiehaiev.aws.dynamodb.rds.sample.entity.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;

@Data
@DynamoDBDocument
public class DynamoDbExperienceItem {

    private String company;
    private String project;
    private long startTimestamp;
    private Long endTimestamp;
}
