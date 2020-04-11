package com.rastiehaiev.aws.dynamodb.rds.sample.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BaseCandidateRepository<E, I> extends CrudRepository<E, I> {

    List<String> findUuidsForMigration();
}
