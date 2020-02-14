/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.jobs.service.repository.impl;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.core.Vertx;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.kie.kogito.jobs.service.model.JobStatus;
import org.kie.kogito.jobs.service.model.ScheduledJob;
import org.kie.kogito.jobs.service.qualifier.Repository;
import org.kie.kogito.jobs.service.repository.ReactiveJobRepository;
import org.kie.kogito.jobs.service.stream.JobStreams;

@ApplicationScoped
@Repository("in-memory")
public class InMemoryJobRepository extends BaseReactiveJobRepository implements ReactiveJobRepository {

    private final Map<String, ScheduledJob> jobMap = new ConcurrentHashMap<>();

    public InMemoryJobRepository() {
        super(null, null);
    }

    @Inject
    public InMemoryJobRepository(Vertx vertx, JobStreams jobStreams) {
        super(vertx, jobStreams);
    }

    @Override
    public CompletionStage<ScheduledJob> doSave(ScheduledJob job) {
        return runAsync(() -> {
            jobMap.put(job.getId(), job);
            return job;
        });
    }

    @Override
    public CompletionStage<ScheduledJob> get(String key) {
        return runAsync(() -> jobMap.get(key));
    }

    @Override
    public CompletionStage<Boolean> exists(String key) {
        return runAsync(() -> jobMap.containsKey(key));
    }

    @Override
    public CompletionStage<ScheduledJob> delete(String key) {
        return runAsync(() -> jobMap.remove(key));
    }

    @Override
    public PublisherBuilder<ScheduledJob> findAll() {
        return ReactiveStreams.fromIterable(jobMap.values());
    }

    @Override
    public PublisherBuilder<ScheduledJob> findByStatusBetweenDatesOrderByPriority(ZonedDateTime from, ZonedDateTime to, JobStatus... status) {
        return ReactiveStreams.fromIterable(
                jobMap.values()
                        .stream()
                        .filter(j -> Optional.ofNullable(j.getStatus())
                                .filter(s -> Objects.nonNull(status))
                                .map(s -> Stream.of(status).anyMatch(s::equals)).orElse(true))
                        .filter(j -> j.getExpirationTime().isAfter(from) && j.getExpirationTime().isBefore(to))
                        .sorted(Comparator.comparing(ScheduledJob::getPriority).reversed())
                        .collect(Collectors.toList()));
    }
}
