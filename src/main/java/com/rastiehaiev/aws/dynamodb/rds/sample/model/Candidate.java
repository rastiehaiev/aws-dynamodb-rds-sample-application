package com.rastiehaiev.aws.dynamodb.rds.sample.model;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Candidate {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private ProgrammingLanguage programmingLanguage;
    private List<ExperienceItem> experience;
}
