package com.rastiehaiev.aws.dynamodb.rds.sample.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrationStatusResult {

    private MigrationStatus status;
}
