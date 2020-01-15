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

package org.kie.kogito.jobs.service.executor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.axle.core.MultiMap;
import io.vertx.axle.core.Vertx;
import io.vertx.axle.core.buffer.Buffer;
import io.vertx.axle.ext.web.client.HttpRequest;
import io.vertx.axle.ext.web.client.HttpResponse;
import io.vertx.axle.ext.web.client.WebClient;
import io.vertx.core.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.jobs.api.Job;
import org.kie.kogito.jobs.api.JobBuilder;
import org.kie.kogito.jobs.service.converters.HttpConverters;
import org.kie.kogito.jobs.service.model.JobExecutionResponse;
import org.kie.kogito.jobs.service.model.ScheduledJob;
import org.kie.kogito.jobs.service.stream.JobStreams;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpJobExecutorTest {

    public static final String ENDPOINT = "http://localhost:8080/endpoint";
    public static final String JOB_ID = UUID.randomUUID().toString();

    @InjectMocks
    private HttpJobExecutor tested;

    @Mock
    private Vertx vertx;

    @Spy
    private HttpConverters httpConverters = new HttpConverters();

    @Mock
    private JobStreams jobStreams;

    @Mock
    private WebClient webClient;

    @Test
    void testInitialize(@Mock io.vertx.core.Vertx vertxCore) {
        when(vertx.getDelegate()).thenReturn(vertxCore);
        tested.initialize();
        assertNotNull(tested.getClient());
    }

    @Test
    void testExecutePeriodic(@Mock HttpRequest<Buffer> request, @Mock MultiMap params) {
        Job job = JobBuilder.builder().repeatInterval(1l).repeatLimit(10).callbackEndpoint(ENDPOINT).id(JOB_ID).
                build();
        ScheduledJob scheduledJob = ScheduledJob.builder().job(job).executionCounter(1).build();

        Map queryParams = assertExecuteAndReturnQueryParams(request, params, scheduledJob, false);
        assertThat(queryParams.size()).isEqualTo(1);
        assertThat(queryParams.get("limit")).isEqualTo("8");
    }

    private Map assertExecuteAndReturnQueryParams(@Mock HttpRequest<Buffer> request, @Mock MultiMap params,
                                                  ScheduledJob scheduledJob, boolean mockError) {
        when(webClient.request(HttpMethod.POST, 8080, "localhost", "/endpoint")).thenReturn(request);
        when(request.queryParams()).thenReturn(params);
        HttpResponse response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(mockError ? 500 : 200);
        when(request.send()).thenReturn(CompletableFuture.completedFuture(response));

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<JobExecutionResponse> responseCaptor = ArgumentCaptor.forClass(JobExecutionResponse.class);

        tested.execute(CompletableFuture.completedFuture(scheduledJob));
        verify(webClient).request(HttpMethod.POST, 8080, "localhost", "/endpoint");
        verify(request).queryParams();
        verify(params).addAll(mapCaptor.capture());
        verify(request).send();
        JobExecutionResponse jobExecutionResponse = mockError
                ? verify(jobStreams).publishJobError(responseCaptor.capture())
                : verify(jobStreams).publishJobSuccess(responseCaptor.capture());
        JobExecutionResponse value = responseCaptor.getValue();
        assertThat(value.getJobId()).isEqualTo(JOB_ID);
        assertThat(value.getCode()).isEqualTo(mockError ? "500" : "200");
        return mapCaptor.getValue();
    }

    @Test
    void testExecute(@Mock HttpRequest<Buffer> request, @Mock MultiMap params) {
        Job job = createSimpleJob();
        ScheduledJob scheduledJob = ScheduledJob.builder().job(job).build();

        Map queryParams = assertExecuteAndReturnQueryParams(request, params, scheduledJob, false);
        assertThat(queryParams.size()).isEqualTo(0);
    }

    @Test
    void testExecuteWithError(@Mock HttpRequest<Buffer> request, @Mock MultiMap params) {
        Job job = createSimpleJob();
        ScheduledJob scheduledJob = ScheduledJob.builder().job(job).build();

        Map queryParams = assertExecuteAndReturnQueryParams(request, params, scheduledJob, true);
        assertThat(queryParams.size()).isEqualTo(0);
    }

    private Job createSimpleJob() {
        return JobBuilder.builder().callbackEndpoint(ENDPOINT).id(JOB_ID).build();
    }
}