package com.rastiehaiev.aws.dynamodb.rds.sample.controller;

import com.rastiehaiev.aws.dynamodb.rds.sample.coordinator.EndpointCoordinator;
import com.rastiehaiev.aws.dynamodb.rds.sample.exception.BadParametersException;
import com.rastiehaiev.aws.dynamodb.rds.sample.exception.ResourceNotFoundException;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.Candidate;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.MigrationStatus;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.MigrationStatusResult;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.CandidateService;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.impl.CandidatesMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CandidateController {

    private final EndpointCoordinator endpointCoordinator;
    private final CandidatesMigrationService candidatesMigrationService;

    @GetMapping("/endpoint/{endpointId}/candidate")
    public List<Candidate> listCandidates(@PathVariable("endpointId") String endpointId) {
        CandidateService candidateService = endpointCoordinator.candidateService(endpointId);
        return candidateService.listAll();
    }

    @PostMapping("/endpoint/{endpointId}/candidate")
    public void createCandidate(@PathVariable("endpointId") String endpointId, @RequestBody Candidate candidate) {
        CandidateService candidateService = endpointCoordinator.candidateService(endpointId);
        candidateService.create(candidate);
    }

    @PatchMapping("/migration/from/{endpointId1}/to/{endpointId2}")
    public MigrationStatusResult runMigration(@PathVariable("endpointId1") String endpointId1, @PathVariable("endpointId2") String endpointId2) {
        MigrationStatus status = candidatesMigrationService.migrate(endpointId1, endpointId2);
        return new MigrationStatusResult(status);
    }

    @ExceptionHandler(BadParametersException.class)
    private ResponseEntity<?> handleBadParametersException(BadParametersException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    private ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
