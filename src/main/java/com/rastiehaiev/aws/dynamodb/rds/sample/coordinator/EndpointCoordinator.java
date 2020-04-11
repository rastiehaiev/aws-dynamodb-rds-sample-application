package com.rastiehaiev.aws.dynamodb.rds.sample.coordinator;

import com.rastiehaiev.aws.dynamodb.rds.sample.exception.ResourceNotFoundException;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.CandidateService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointCoordinator {

    private Map<String, CandidateService> candidateServiceMap = new ConcurrentHashMap<>();

    public void register(String name, CandidateService candidateService) {
        candidateServiceMap.put(name, candidateService);
    }

    public CandidateService candidateService(String name) {
        CandidateService candidateService = candidateServiceMap.get(name);
        if (candidateService == null) {
            throw new ResourceNotFoundException("Resource '" + name + "' not found.");
        }
        return candidateService;
    }
}
