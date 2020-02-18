/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.kie.kogito.jobs.api.JobBuilder;
import org.kie.kogito.jobs.service.model.JobStatus;
import org.kie.kogito.jobs.service.model.ScheduledJob;
import org.kie.kogito.jobs.service.repository.ReactiveJobRepository;
import org.kie.kogito.jobs.service.stream.JobStreams;
import org.kie.kogito.jobs.service.utils.DateUtil;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public abstract class BaseJobRepositoryTest {

    public static final String ID = UUID.randomUUID().toString();

    @Mock
    public Vertx vertx;

    @Mock
    public JobStreams jobStreams;

    private ScheduledJob job;

    public void setUp() {
        lenient().doAnswer(a -> {
                               a.getArgument(0, Handler.class).handle(null);
                               return null;
                           }
        ).when(vertx).runOnContext(any());

        createAndSaveJob(ID);
    }

    public abstract ReactiveJobRepository tested();

    @Test
    void testSaveAndGet() throws ExecutionException, InterruptedException {
        ScheduledJob scheduledJob = tested().get(ID).toCompletableFuture().get();
        assertThat(scheduledJob).isEqualTo(job);
        ScheduledJob notFound = tested().get(UUID.randomUUID().toString()).toCompletableFuture().get();
        assertThat(notFound).isNull();
    }

    private void createAndSaveJob(String id) {
        job = ScheduledJob.builder()
                .job(JobBuilder.builder()
                             .id(id)
                             .expirationTime(DateUtil.now())
                             .priority(1)
                             .callbackEndpoint("url")
                             .build())
                .build();
        tested().save(job);
    }

    @Test
    void testExists() throws ExecutionException, InterruptedException {
        Boolean exists = tested().exists(ID).toCompletableFuture().get();
        assertThat(exists).isTrue();
        Boolean notFound = tested().exists(UUID.randomUUID().toString()).toCompletableFuture().get();
        assertThat(notFound).isFalse();
    }

    @Test
    void testDelete() throws ExecutionException, InterruptedException {
        ScheduledJob scheduledJob = tested().delete(ID).toCompletableFuture().get();
        assertThat(scheduledJob).isEqualTo(job);
        ScheduledJob notFound = tested().get(ID).toCompletableFuture().get();
        assertThat(notFound).isNull();
    }

    @Test
    void testFindAll() throws ExecutionException, InterruptedException {
        List<ScheduledJob> jobs = tested().findAll().toList().run().toCompletableFuture().get();
        assertThat(jobs.size()).isEqualTo(1);
        assertThat(jobs.get(0)).isEqualTo(job);
    }

    @Test
    void testFindByStatusBetweenDates() throws ExecutionException, InterruptedException {
        List<ScheduledJob> jobs = IntStream.rangeClosed(1, 10).boxed()
                .map(id -> ScheduledJob.builder()
                        .status(JobStatus.SCHEDULED)
                        .job(JobBuilder.builder()
                                     .id(String.valueOf(id))
                                     .expirationTime(DateUtil.now().plusMinutes(id))
                                     .priority(id)
                                     .build())
                        .build())
                .peek(tested()::save)
                .collect(Collectors.toList());

        final List<ScheduledJob> fetched = tested().findByStatusBetweenDatesOrderByPriority(DateUtil.now(),
                                                                                            DateUtil.now().plusMinutes(5).plusSeconds(1),
                                                                                            JobStatus.SCHEDULED)
                .toList()
                .run()
                .toCompletableFuture()
                .get();

        assertThat(fetched.size()).isEqualTo(5);

        IntStream.rangeClosed(0, 4).forEach(
                i -> assertThat(fetched.get(i)).isEqualTo(jobs.get(fetched.size() - 1 - i))
        );

        //not found test
        List<ScheduledJob> fetchedNotFound = tested().findByStatusBetweenDatesOrderByPriority(DateUtil.now(),
                                                                                              DateUtil.now().plusMinutes(5).plusSeconds(1),
                                                                                              JobStatus.CANCELED)
                .toList()
                .run()
                .toCompletableFuture()
                .get();

        assertThat(fetchedNotFound.size()).isEqualTo(0);

        fetchedNotFound = tested().findByStatusBetweenDatesOrderByPriority(DateUtil.now().plusDays(1),
                                                                           DateUtil.now().plusDays(2),
                                                                           JobStatus.SCHEDULED)
                .toList()
                .run()
                .toCompletableFuture()
                .get();

        assertThat(fetchedNotFound.size()).isEqualTo(0);
    }
}