/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.prediction.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.core.io.impl.ClassPathResource;
import org.jbpm.process.instance.impl.humantask.HumanTaskWorkItemHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.bpmn2.BpmnProcess;
import org.kie.kogito.process.bpmn2.BpmnVariables;
import org.kie.kogito.process.impl.CachedWorkItemHandlerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.process.workitems.InternalKogitoWorkItem;
import org.kie.kogito.services.identity.StaticIdentityProvider;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_ACTIVE;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_COMPLETED;

public class PredictionAwareHumanTaskLifeCycleTest {

    private Policy<?> securityPolicy = SecurityPolicy.of(new StaticIdentityProvider("john"));

    private AtomicBoolean predictNow;
    private List<String> trainedTasks;

    private PredictionService predictionService;

    private ProcessConfig config;

    @BeforeEach
    public void configure() {

        predictNow = new AtomicBoolean(false);
        trainedTasks = new ArrayList<>();

        predictionService = new PredictionService() {

            @Override
            public void train(org.kie.api.runtime.process.WorkItem task, Map<String, Object> inputData, Map<String, Object> outputData) {
                trainedTasks.add(((InternalKogitoWorkItem) task).getStringId());
            }

            @Override
            public PredictionOutcome predict(org.kie.api.runtime.process.WorkItem task, Map<String, Object> inputData) {
                if (predictNow.get()) {
                    return new PredictionOutcome(95, 75, Collections.singletonMap("output", "predicted value"));
                }

                return new PredictionOutcome();
            }

            @Override
            public String getIdentifier() {
                return "test";
            }
        };

        CachedWorkItemHandlerConfig wiConfig = new CachedWorkItemHandlerConfig();
        wiConfig.register("Human Task", new HumanTaskWorkItemHandler(new PredictionAwareHumanTaskLifeCycle(predictionService)));
        config = new StaticProcessConfig(wiConfig, new DefaultProcessEventListenerConfig(), new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory()), null);
    }

    @Test
    public void testUserTaskWithPredictionService() {
        predictNow.set(true);

        BpmnProcess process = BpmnProcess.from(config, new ClassPathResource("BPMN2-UserTask.bpmn2")).get(0);
        process.configure();

        ProcessInstance processInstance = process.createInstance(BpmnVariables.create(Collections.singletonMap("test", "test")));

        processInstance.start();
        assertEquals(STATE_COMPLETED, processInstance.status());

        Model result = (Model) processInstance.variables();
        assertEquals(2, result.toMap().size());
        assertEquals("predicted value", result.toMap().get("s"));

        assertEquals(0, trainedTasks.size());

    }

    @Test
    public void testUserTaskWithoutPredictionService() {

        BpmnProcess process = BpmnProcess.from(config, new ClassPathResource("BPMN2-UserTask.bpmn2")).get(0);
        process.configure();

        ProcessInstance processInstance = process.createInstance(BpmnVariables.create(Collections.singletonMap("test", "test")));

        processInstance.start();
        assertEquals(STATE_ACTIVE, processInstance.status());

        WorkItem workItem = processInstance.workItems(securityPolicy).get(0);
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameters().get("ActorId"));
        processInstance.completeWorkItem(workItem.getId(), Collections.singletonMap("output", "given value"), securityPolicy);
        assertEquals(STATE_COMPLETED, processInstance.status());

        Model result = (Model) processInstance.variables();
        assertEquals(2, result.toMap().size());
        assertEquals("given value", result.toMap().get("s"));

        assertEquals(1, trainedTasks.size());

    }
}
