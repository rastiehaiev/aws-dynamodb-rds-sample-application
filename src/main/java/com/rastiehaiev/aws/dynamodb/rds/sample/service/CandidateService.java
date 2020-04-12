package com.rastiehaiev.aws.dynamodb.rds.sample.service;

import com.rastiehaiev.aws.dynamodb.rds.sample.model.Candidate;

import java.util.List;
import java.util.function.Consumer;

public interface CandidateService {

    List<Candidate> listAll();

    Candidate create(Candidate candidate);

    void migrate(List<Candidate> candidates);

    List<String> findUuidsForMigration();

    void migrateTo(List<String> uuids, Consumer<List<Candidate>> candidatesCreator);
}
