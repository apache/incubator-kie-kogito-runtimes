/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.tests;

import java.util.concurrent.atomic.AtomicInteger;

import defaultPackage.BusinessRuleTaskModel;
import defaultPackage.BusinessRuleTaskProcess;
import defaultPackage.BusinessRuleTaskProcessInstance;
import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.drools.core.config.StaticRuleConfig;
import org.drools.core.event.DefaultAgendaEventListener;
import org.junit.jupiter.api.Test;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.kogito.Config;
import org.kie.kogito.StaticConfig;
import org.kie.kogito.app.Application;
import org.kie.kogito.codegen.annotations.KogitoTest;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.process.impl.StaticProcessConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@KogitoTest
public class BusinessRuleTaskTest {

    @Test
    public void testBasicBusinessRuleTask() throws Exception {

        Application app = new Application();
        assertThat(app).isNotNull();

        BusinessRuleTaskProcess p = app.processes().createBusinessRuleTaskProcess();


        BusinessRuleTaskModel businessRuleTaskModel = new BusinessRuleTaskModel();
        businessRuleTaskModel.setPerson(new Person("john", 25));

        BusinessRuleTaskProcessInstance processInstance = p.createInstance(businessRuleTaskModel);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
        BusinessRuleTaskModel result = processInstance.variables();
        assertTrue(result.getPerson().isAdult());
    }

    @Test
    public void testBasicBusinessRuleTaskWithAgendaListener() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        // extends the App class and override the configuration
        Application app = new Application() {
            @Override
            public Config config() {
                return new StaticConfig(
                        StaticProcessConfig.Default(),
                        new StaticRuleConfig(
                                new DefaultRuleEventListenerConfig(
                                        new DefaultAgendaEventListener() {
                                            @Override
                                            public void afterMatchFired(AfterMatchFiredEvent event) {
                                                counter.incrementAndGet();
                                            }
                                        }
                                )));
            }
        };

        BusinessRuleTaskProcess p = app.processes().createBusinessRuleTaskProcess();

        BusinessRuleTaskModel businessRuleTaskModel = new BusinessRuleTaskModel();
        businessRuleTaskModel.setPerson(new Person("john", 25));

        BusinessRuleTaskProcessInstance processInstance = p.createInstance(businessRuleTaskModel);
        processInstance.start();

        assertThat(processInstance.status()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        BusinessRuleTaskModel result = processInstance.variables();
        assertTrue(result.getPerson().isAdult());

        assertThat(counter.get()).isEqualTo(1);
    }
}
