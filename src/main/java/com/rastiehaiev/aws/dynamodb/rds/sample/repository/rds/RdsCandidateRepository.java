package com.rastiehaiev.aws.dynamodb.rds.sample.repository.rds;

import com.rastiehaiev.aws.dynamodb.rds.sample.entity.rds.RdsCandidateEntity;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.BaseCandidateRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RdsCandidateRepository extends BaseCandidateRepository<RdsCandidateEntity, Long> {

    @Override
    @Query(value = "SELECT CAST ( uuid AS VARCHAR ) FROM candidate WHERE migrated = false", nativeQuery = true)
    List<String> findUuidsForMigration();

    List<RdsCandidateEntity> findByUuidIn(List<UUID> uuids);
}
