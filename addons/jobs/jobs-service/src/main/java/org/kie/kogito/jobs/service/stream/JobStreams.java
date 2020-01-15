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

package org.kie.kogito.jobs.service.stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.kie.kogito.jobs.service.model.JobExecutionResponse;
import org.kie.kogito.jobs.service.model.ScheduledJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that configure the Consumers for Job Streams,like Job Executed, Job Error... and execute the actions for each
 * received item.
 */
@ApplicationScoped
public class JobStreams {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStreams.class);

    /**
     * Publish on Stream of Job Error events
     */
    @Inject
    @Channel(AvailableStreams.JOB_ERROR)
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    Emitter<JobExecutionResponse> jobErrorEmitter;

    /**
     * Publish on Stream of Job Success events
     */
    @Inject
    @Channel(AvailableStreams.JOB_SUCCESS)
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    Emitter<JobExecutionResponse> jobSuccessEmitter;

    /**
     * Publish on Stream of Job Success events
     */
    @Inject
    @Channel(AvailableStreams.JOB_STATUS_CHANGE)
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    Emitter<ScheduledJob> jobStatusChangeEmitter;

    public JobExecutionResponse publishJobError(JobExecutionResponse response) {
        jobErrorEmitter.send(response);
        return response;
    }

    public JobExecutionResponse publishJobSuccess(JobExecutionResponse response) {
        jobSuccessEmitter.send(response);
        return response;
    }

    public ScheduledJob publishJobStatusChange(ScheduledJob scheduledJob) {
        jobStatusChangeEmitter.send(scheduledJob);
        return scheduledJob;
    }

    //Broadcast Events from Emitter to Streams

    @Incoming(AvailableStreams.JOB_ERROR)
    @Outgoing(AvailableStreams.JOB_ERROR_EVENTS)
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public JobExecutionResponse jobErrorBroadcast(JobExecutionResponse response) {
        LOGGER.debug("Error broadcast published {}", response);
        return response;
    }

    @Incoming(AvailableStreams.JOB_SUCCESS)
    @Outgoing(AvailableStreams.JOB_SUCCESS_EVENTS)
    @Broadcast
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public JobExecutionResponse jobSuccessBroadcast(JobExecutionResponse response) {
        LOGGER.debug("Success broadcast published {}", response);
        return response;
    }

    @Incoming(AvailableStreams.JOB_STATUS_CHANGE)
    @Outgoing(AvailableStreams.JOB_STATUS_CHANGE_EVENTS)
    @Broadcast
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public ScheduledJob jobStatusChangeBroadcast(ScheduledJob job) {
        LOGGER.debug("Status change broadcast for Job {}", job);
        return job;
    }
}