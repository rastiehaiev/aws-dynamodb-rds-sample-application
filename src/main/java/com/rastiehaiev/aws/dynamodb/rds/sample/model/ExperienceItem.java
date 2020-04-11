package com.rastiehaiev.aws.dynamodb.rds.sample.model;

import lombok.Data;

@Data
public class ExperienceItem {

    private String company;
    private String project;
    private long startTimestamp;
    private Long endTimestamp;
}
