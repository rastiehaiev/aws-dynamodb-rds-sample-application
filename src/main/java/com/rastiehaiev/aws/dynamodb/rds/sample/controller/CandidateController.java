package com.rastiehaiev.aws.dynamodb.rds.sample.controller;

import com.rastiehaiev.aws.dynamodb.rds.sample.coordinator.EndpointCoordinator;
import com.rastiehaiev.aws.dynamodb.rds.sample.exception.BadParametersException;
import com.rastiehaiev.aws.dynamodb.rds.sample.exception.ResourceNotFoundException;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.*;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.CandidateService;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.impl.CandidatesMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CandidateController {

    private final EndpointCoordinator endpointCoordinator;
    private final CandidatesMigrationService candidatesMigrationService;

    @GetMapping("/{endpointId}/candidate")
    public MultipleResults<Candidate> listCandidates(@PathVariable("endpointId") String endpointId) {
        CandidateService candidateService = endpointCoordinator.candidateService(endpointId);
        return new CandidatesResponse().withElements(candidateService.listAll());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{endpointId}/candidate")
    public Candidate createCandidate(@PathVariable("endpointId") String endpointId, @RequestBody Candidate candidate) {
        CandidateService candidateService = endpointCoordinator.candidateService(endpointId);
        return candidateService.create(candidate);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping("/candidate/migration/from/{endpointId1}/to/{endpointId2}")
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
