package com.rastiehaiev.aws.dynamodb.rds.sample.service;

import com.google.common.collect.Lists;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.Candidate;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.BaseCandidateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class BaseCandidateService<E, I> implements CandidateService {

    private final BaseCandidateRepository<E, I> repository;

    public BaseCandidateService(BaseCandidateRepository<E, I> repository) {
        this.repository = repository;
    }

    @Override
    public Candidate create(Candidate candidate) {
        Assert.notNull(candidate, "candidate cannot be null.");
        E entity = repository.save(fromCandidate(candidate));
        return toCandidate(entity);
    }

    @Override
    public List<Candidate> listAll() {
        List<E> candidates = Lists.newArrayList(repository.findAll());
        return candidates.stream().map(this::toCandidate).collect(Collectors.toList());
    }

    @Override
    public List<String> findUuidsForMigration() {
        return repository.findUuidsForMigration();
    }

    @Override
    public void migrate(List<Candidate> candidates) {
        if (CollectionUtils.isNotEmpty(candidates)) {
            List<E> entities = candidates.stream()
                    .map(this::fromCandidate)
                    .peek(this::setMigrationStatus)
                    .collect(toList());
            repository.saveAll(entities);
        }
    }

    @Override
    public void migrateTo(List<String> ids, Consumer<List<Candidate>> candidatesCreator) {
        List<E> entities = findByUuids(ids);
        List<Candidate> candidates = entities.stream().map(this::toCandidate).collect(toList());
        candidatesCreator.accept(candidates);
        entities.forEach(this::setMigrationStatus);
        repository.saveAll(entities);
    }

    protected abstract List<E> findByUuids(List<String> uuids);

    protected abstract E fromCandidate(Candidate candidate);

    protected abstract void setMigrationStatus(E entity);

    protected abstract Candidate toCandidate(E entity);
}
