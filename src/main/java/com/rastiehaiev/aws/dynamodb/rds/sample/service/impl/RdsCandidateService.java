package com.rastiehaiev.aws.dynamodb.rds.sample.service.impl;

import com.google.common.collect.Lists;
import com.rastiehaiev.aws.dynamodb.rds.sample.entity.rds.ExperienceItemEntity;
import com.rastiehaiev.aws.dynamodb.rds.sample.entity.rds.RdsCandidateEntity;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.Candidate;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.ExperienceItem;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.rds.RdsCandidateRepository;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.BaseCandidateService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class RdsCandidateService extends BaseCandidateService<RdsCandidateEntity, Long> {

    private final RdsCandidateRepository repository;

    public RdsCandidateService(RdsCandidateRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<Candidate> listAll() {
        return super.listAll();
    }

    @Override
    @Transactional
    public void migrate(List<Candidate> candidates) {
        super.migrate(candidates);
    }

    @Override
    @Transactional
    public void migrateTo(List<String> ids, Consumer<List<Candidate>> candidatesCreator) {
        super.migrateTo(ids, candidatesCreator);
    }

    @Override
    protected void setMigrationStatus(RdsCandidateEntity entity) {
        entity.setMigrated(true);
    }

    @Override
    protected List<RdsCandidateEntity> findByUuids(List<String> ids) {
        List<UUID> uuids = ids.stream().map(UUID::fromString).collect(toList());
        return Lists.newArrayList(repository.findByUuidIn(uuids));
    }

    @Override
    protected RdsCandidateEntity fromCandidate(Candidate candidate) {
        RdsCandidateEntity entity = new RdsCandidateEntity();
        entity.setUuid(candidate.getUuid());
        entity.setFirstName(candidate.getFirstName());
        entity.setLastName(candidate.getLastName());
        entity.setProgrammingLanguage(candidate.getProgrammingLanguage());
        entity.setExperiences(candidate.getExperience().stream().map(this::toExperienceItemEntity).collect(Collectors.toSet()));
        return entity;
    }

    @Override
    protected Candidate toCandidate(RdsCandidateEntity entity) {
        Candidate candidate = new Candidate();
        candidate.setUuid(entity.getUuid());
        candidate.setFirstName(entity.getFirstName());
        candidate.setLastName(entity.getLastName());
        candidate.setProgrammingLanguage(entity.getProgrammingLanguage());
        candidate.setExperience(entity.getExperiences().stream().map(this::toExperienceItem).collect(toList()));
        return candidate;
    }

    private ExperienceItem toExperienceItem(ExperienceItemEntity entity) {
        ExperienceItem item = new ExperienceItem();
        item.setCompany(entity.getCompany());
        item.setProject(entity.getProject());
        item.setStartTimestamp(entity.getStartTimestamp());
        item.setEndTimestamp(entity.getEndTimestamp());
        return item;
    }

    private ExperienceItemEntity toExperienceItemEntity(ExperienceItem experienceItem) {
        ExperienceItemEntity entity = new ExperienceItemEntity();
        entity.setCompany(experienceItem.getCompany());
        entity.setProject(experienceItem.getProject());
        entity.setStartTimestamp(experienceItem.getStartTimestamp());
        entity.setEndTimestamp(experienceItem.getEndTimestamp());
        return entity;
    }
}
