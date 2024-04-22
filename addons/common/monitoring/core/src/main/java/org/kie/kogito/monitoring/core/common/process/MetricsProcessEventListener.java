/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.monitoring.core.common.process;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.kie.api.event.process.ErrorEvent;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.SLAViolatedEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Counter.Builder;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

public class MetricsProcessEventListener extends DefaultKogitoProcessEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsProcessEventListener.class);
    private static Map<String, AtomicInteger> gaugeMap = new HashMap<>();
    private final String identifier;
    private final KogitoGAV gav;
    private final MeterRegistry meterRegistry;

    public MetricsProcessEventListener(String identifier, KogitoGAV gav, MeterRegistry meterRegistry) {
        this.identifier = identifier;
        this.gav = gav;
        this.meterRegistry = meterRegistry;
    }

    private Counter getNumberOfProcessInstancesStartedCounter(String appId, String processId) {
        return buildCounter("kogito_process_instance_started_total", "Started Process Instances", appId, processId);
    }

    private Counter getErrorCounter(String appId, String processId, String errorMessage) {
        return buildCounter("kogito_process_instance_error", "Process instances currently in error state", appId, processId, Tag.of("error_message", errorMessage));
    }

    private Counter getNumberOfSLAsViolatedCounter(String appId, String processId, String nodeName) {
        return buildCounter("kogito_process_instance_sla_violated_total", "Process Instances SLA Violated", appId, processId, Tag.of("node_name", nodeName));
    }

    private Counter getNumberOfProcessInstancesCompletedCounter(String appId, String processId, String state) {
        return buildCounter("kogito_process_instance_completed_total", "Completed Process Instances", appId, processId, Tag.of("state", state));
    }

    private Counter buildCounter(String name, String description, String appId, String processId, Tag... tags) {
        Builder builder = Counter
                .builder(name)
                .description(description)
                .tag("app_id", appId).tag("process_id", processId).tag("artifactId", gav.getArtifactId()).tag("version", gav.getVersion());
        for (Tag tag : tags) {
            builder.tag(tag.getKey(), tag.getValue());
        }
        return builder.register(meterRegistry);
    }

    private AtomicInteger getRunningProcessInstancesGauge(String appId, String processId) {
        if (gaugeMap.containsKey(appId + processId)) {
            return gaugeMap.get(appId + processId);
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Gauge.builder("kogito_process_instance_running_total", atomicInteger, AtomicInteger::doubleValue)
                .description("Running Process Instances")
                .tags(Arrays.asList(Tag.of("app_id", appId), Tag.of("process_id", processId), Tag.of("artifactId", gav.getArtifactId()), Tag.of("version", gav.getVersion())))
                .register(meterRegistry);
        gaugeMap.put(appId + processId, atomicInteger);
        return atomicInteger;
    }

    private DistributionSummary getProcessInstancesDurationSummary(String appId, String processId) {
        return DistributionSummary.builder("kogito_process_instance_duration_seconds")
                .description("Process Instances Duration")
                .tags(Arrays.asList(Tag.of("app_id", appId), Tag.of("process_id", processId), Tag.of("artifactId", gav.getArtifactId()), Tag.of("version", gav.getVersion())))
                .register(meterRegistry);
    }

    private DistributionSummary getWorkItemsDurationSummary(String name) {
        return DistributionSummary.builder("kogito_work_item_duration_seconds")
                .description("Work Items Duration")
                .tags(Arrays.asList(Tag.of("name", name), Tag.of("artifactId", gav.getArtifactId()), Tag.of("version", gav.getVersion())))
                .register(meterRegistry);
    }

    protected void recordRunningProcessInstance(String containerId, String processId) {
        getRunningProcessInstancesGauge(containerId, processId).incrementAndGet();
    }

    protected static double millisToSeconds(long millis) {
        return TimeUnit.MILLISECONDS.toSeconds(millis);
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        LOGGER.debug("After process started event: {}", event);
        final ProcessInstance processInstance = event.getProcessInstance();
        getNumberOfProcessInstancesStartedCounter(identifier, processInstance.getProcessId()).increment();
        recordRunningProcessInstance(identifier, processInstance.getProcessId());
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.debug("After process completed event: {}", event);
        final KogitoWorkflowProcessInstance processInstance = (KogitoWorkflowProcessInstance) event.getProcessInstance();
        getRunningProcessInstancesGauge(identifier, processInstance.getProcessId()).decrementAndGet();

        getNumberOfProcessInstancesCompletedCounter(identifier, processInstance.getProcessId(), fromState(processInstance.getState())).increment();

        if (processInstance.getStartDate() != null) {
            final double duration = millisToSeconds(processInstance.getEndDate().getTime() - processInstance.getStartDate().getTime());
            getProcessInstancesDurationSummary(identifier, processInstance.getProcessId()).record(duration);
            LOGGER.debug("Process Instance duration: {}s", duration);
        }
    }

    @Override
    public void onError(ErrorEvent event) {
        LOGGER.debug("After Error event: {}", event);
        final KogitoWorkflowProcessInstance processInstance = (KogitoWorkflowProcessInstance) event.getProcessInstance();
        getErrorCounter(identifier, processInstance.getProcessId(), processInstance.getErrorMessage()).increment();
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.debug("Before Node left event: {}", event);
        final KogitoNodeInstance nodeInstance = (KogitoNodeInstance) event.getNodeInstance();
        if (nodeInstance instanceof KogitoWorkItemNodeInstance) {
            KogitoWorkItemNodeInstance wi = (KogitoWorkItemNodeInstance) nodeInstance;
            if (wi.getTriggerTime() != null) {
                final String name = (String) wi.getWorkItem().getParameters().getOrDefault("TaskName", wi.getWorkItem().getName());
                final double duration = millisToSeconds(wi.getLeaveTime().getTime() - wi.getTriggerTime().getTime());
                getWorkItemsDurationSummary(name).record(duration);
                LOGGER.debug("Work Item {}, duration: {}s", name, duration);
            }
        }
    }

    @Override
    public void afterSLAViolated(SLAViolatedEvent event) {
        LOGGER.debug("After SLA violated event: {}", event);
        final ProcessInstance processInstance = event.getProcessInstance();
        if (processInstance != null && event.getNodeInstance() != null) {
            getNumberOfSLAsViolatedCounter(identifier, processInstance.getProcessId(), event.getNodeInstance().getNodeName()).increment();
        }
    }

    private static String fromState(int state) {
        switch (state) {
            case KogitoProcessInstance.STATE_ABORTED:
                return "Aborted";
            case KogitoProcessInstance.STATE_COMPLETED:
                return "Completed";
            case KogitoProcessInstance.STATE_ERROR:
                return "Error";
            default:
            case KogitoProcessInstance.STATE_ACTIVE:
                return "Active";
        }
    }

}
