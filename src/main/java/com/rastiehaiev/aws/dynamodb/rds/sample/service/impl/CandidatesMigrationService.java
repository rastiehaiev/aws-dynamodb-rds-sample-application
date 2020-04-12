package com.rastiehaiev.aws.dynamodb.rds.sample.service.impl;

import com.rastiehaiev.aws.dynamodb.rds.sample.coordinator.EndpointCoordinator;
import com.rastiehaiev.aws.dynamodb.rds.sample.exception.BadParametersException;
import com.rastiehaiev.aws.dynamodb.rds.sample.model.MigrationStatus;
import com.rastiehaiev.aws.dynamodb.rds.sample.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatesMigrationService {

    private final EndpointCoordinator endpointCoordinator;
    private final ThreadPoolExecutor migrationThreadPoolExecutor;

    private CompletableFuture<Void> lastMigration;

    @Value("${application.migration.batch-size}")
    private int batchSize;

    public synchronized MigrationStatus migrate(String source, String destination) {
        if (source.equals(destination)) {
            throw new BadParametersException("Source and destination should not be the same.");
        }

        CandidateService sourceCandidateService = endpointCoordinator.candidateService(source);
        CandidateService destinationCandidateService = endpointCoordinator.candidateService(destination);

        if (lastMigration != null && !lastMigration.isDone()) {
            return MigrationStatus.IN_PROGRESS;
        }
        List<String> uuidsForMigration = sourceCandidateService.findUuidsForMigration();
        if (CollectionUtils.isEmpty(uuidsForMigration)) {
            return MigrationStatus.NO_DATA_FOR_MIGRATION;

        }
        Queue<String> idsQueue = new ConcurrentLinkedQueue<>(uuidsForMigration);
        List<MigrationTask> tasks = getMigrationTasks(sourceCandidateService, destinationCandidateService, idsQueue);
        List<Future<Void>> futureResults = runMigrationTasks(tasks);

        lastMigration = CompletableFuture.supplyAsync(() -> {
            futureResults.forEach(this::waitForCompletion);
            return null;
        });
        return MigrationStatus.SUBMITTED;
    }

    private List<Future<Void>> runMigrationTasks(List<MigrationTask> tasks) {
        try {
            return migrationThreadPoolExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<MigrationTask> getMigrationTasks(CandidateService sourceCandidateService,
                                                  CandidateService destinationCandidateService,
                                                  Queue<String> uuids) {

        List<MigrationTask> tasks = new ArrayList<>();
        for (int i = 0; i < migrationThreadPoolExecutor.getCorePoolSize(); i++) {
            tasks.add(new MigrationTask(uuids, sourceCandidateService, destinationCandidateService, batchSize));
        }
        return tasks;
    }

    private void waitForCompletion(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequiredArgsConstructor
    private static class MigrationTask implements Callable<Void> {

        private final Queue<String> uuids;
        private final CandidateService sourceCandidateService;
        private final CandidateService destinationCandidateService;
        private final int batchSize;

        @Override
        public Void call() {
            List<String> uuids;
            do {
                uuids = pollMultiple(this.uuids);
                if (!CollectionUtils.isEmpty(uuids)) {
                    log.info("Migrating {} elements.", uuids.size());
                    sourceCandidateService.migrateTo(uuids, destinationCandidateService::migrate);
                }
            } while (!uuids.isEmpty());
            return null;
        }

        private List<String> pollMultiple(Queue<String> queue) {
            return IntStream.range(0, batchSize).boxed()
                    .map(i -> queue.poll())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}
