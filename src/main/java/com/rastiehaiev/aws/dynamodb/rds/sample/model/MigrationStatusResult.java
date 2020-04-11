package com.rastiehaiev.aws.dynamodb.rds.sample.model;

import lombok.Data;

@Data
public class MigrationStatusResult {

    private final MigrationStatus status;
}
