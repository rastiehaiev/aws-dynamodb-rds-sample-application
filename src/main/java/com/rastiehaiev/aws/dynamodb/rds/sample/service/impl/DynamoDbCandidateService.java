package com.rastiehaiev.aws.dynamodb.rds.sample.service.impl;

import com.google.common.collect.Lists;
import com.rastiehaiev.aws.dynamodb.rds.sample.entity.dynamodb.DynamoDbCandidate;
import com.rastiehaiev.aws.dynamodb.rds.sample.entity.dynamodb.DynamoDbExperienceItem;
import com.rastiehaiev.aws.dynamodb.rds.sample.entity.dynamodb.MigrationStatus;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.Candidate;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.ExperienceItem;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.dynamodb.DynamoDbCandidateRepository;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.BaseCandidateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Service
public class DynamoDbCandidateService extends BaseCandidateService<DynamoDbCandidate, String> {

    private final DynamoDbCandidateRepository repository;

    public DynamoDbCandidateService(DynamoDbCandidateRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected List<DynamoDbCandidate> findByUuids(List<String> uuids) {
        return Lists.newArrayList(repository.findAllById(uuids));
    }

    @Override
    protected void setMigrationStatus(DynamoDbCandidate entity) {
        entity.setMigrationStatus(MigrationStatus.DONE);
    }

    @Override
    protected DynamoDbCandidate fromCandidate(Candidate candidate) {
        DynamoDbCandidate dynamoDbCandidate = new DynamoDbCandidate();
        dynamoDbCandidate.setId(Optional.ofNullable(candidate.getUuid()).map(UUID::toString).orElse(UUID.randomUUID().toString()));
        dynamoDbCandidate.setFirstName(candidate.getFirstName());
        dynamoDbCandidate.setLastName(candidate.getLastName());
        dynamoDbCandidate.setProgrammingLanguage(candidate.getProgrammingLanguage());
        dynamoDbCandidate.setExperience(getExperienceItems(candidate.getExperience()));
        return dynamoDbCandidate;
    }

    @Override
    protected Candidate toCandidate(DynamoDbCandidate entity) {
        Candidate candidate = new Candidate();
        candidate.setUuid(UUID.fromString(entity.getId()));
        candidate.setFirstName(entity.getFirstName());
        candidate.setLastName(entity.getLastName());
        candidate.setProgrammingLanguage(entity.getProgrammingLanguage());
        candidate.setExperience(entity.getExperience().stream().map(this::toExperienceItem).collect(Collectors.toList()));
        return candidate;
    }

    private List<DynamoDbExperienceItem> getExperienceItems(List<ExperienceItem> experienceItems) {
        return emptyIfNull(experienceItems).stream()
                .map(this::toDynamoDbExperienceItem)
                .collect(Collectors.toList());
    }

    private ExperienceItem toExperienceItem(DynamoDbExperienceItem dynamoDbExperienceItem) {
        ExperienceItem experienceItem = new ExperienceItem();
        experienceItem.setCompany(dynamoDbExperienceItem.getCompany());
        experienceItem.setProject(dynamoDbExperienceItem.getProject());
        experienceItem.setStartTimestamp(dynamoDbExperienceItem.getStartTimestamp());
        experienceItem.setEndTimestamp(dynamoDbExperienceItem.getEndTimestamp());
        return experienceItem;
    }

    private DynamoDbExperienceItem toDynamoDbExperienceItem(ExperienceItem experienceItem) {
        DynamoDbExperienceItem dynamoDbExperienceItem = new DynamoDbExperienceItem();
        dynamoDbExperienceItem.setCompany(experienceItem.getCompany());
        dynamoDbExperienceItem.setProject(experienceItem.getProject());
        dynamoDbExperienceItem.setStartTimestamp(experienceItem.getStartTimestamp());
        dynamoDbExperienceItem.setEndTimestamp(experienceItem.getEndTimestamp());
        return dynamoDbExperienceItem;
    }
}
