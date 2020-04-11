package com.rastiehaiev.aws.dynamodb.rds.sample.repository.dynamodb;

import com.rastiehaiev.aws.dynamodb.rds.sample.entity.dynamodb.DynamoDbCandidate;
import com.rastiehaiev.aws.dynamodb.rds.sample.repository.BaseCandidateRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface DynamoDbCandidateRepository extends BaseCandidateRepository<DynamoDbCandidate, String> {

}
