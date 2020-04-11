package com.rastiehaiev.aws.dynamodb.rds.sample.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectConstants {

    public static final String CANDIDATE_TABLE_NAME = "candidate";
    public static final String MIGRATION_STATUS_GLOBAL_INDEX_NAME = "migrationStatusIndex";
}
