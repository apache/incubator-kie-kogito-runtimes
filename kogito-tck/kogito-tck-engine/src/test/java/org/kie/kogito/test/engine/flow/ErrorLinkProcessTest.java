/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.test.engine.flow;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.process.Processes;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestExtension;
import org.kie.kogito.tck.junit.api.KogitoUnitTestResource;

import static org.assertj.core.api.Assertions.assertThat;

@KogitoUnitTestExtension
public class ErrorLinkProcessTest {


    public static final String PROCESS_EMPTY = "org/kie/kogito/test/engine/common/EmptyLinkProcess.bpmn2";
    public static final String PROCESS_MULTI_THROW = "org/kie/kogito/test/engine/common/MultipleThrowLinkProcess.bpmn2";
    public static final String PROCESS_MULTI_CATCH = "org/kie/kogito/test/engine/common/MultipleCatchLinkProcess.bpmn2";
    public static final String PROCESS_UNCONNECTED = "org/kie/kogito/test/engine/common/UnconnectedLinkProcess.bpmn2";
    public static final String DIFFERENT_PROCESS = "org/kie/kogito/test/engine/common/DifferentLinkProcess.bpmn2";
    

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = PROCESS_EMPTY)}
    )
    public void testEmptyLinkEvents(Throwable throwable) {
        assertThat(throwable).hasCauseInstanceOf(IllegalArgumentException.class).hasMessageContaining("nodes do not have a name");
    }
    
    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = PROCESS_MULTI_THROW)}
    )
    public void testMultiThrowLinkEvents(KogitoUnitTestContext context) {
        assertThat(context.find(Application.class).get(Processes.class).processById("org.jbpm.test.functional.common.MultipleThrowLinkProcess")).isNotNull();
    }
    

    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = PROCESS_MULTI_CATCH)}
    )
    public void testMultiCatchEvents(Throwable throwable) {
        assertThat(throwable).hasCauseInstanceOf(IllegalArgumentException.class).hasMessageContaining("multiple catch nodes");
    }
    
    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = PROCESS_UNCONNECTED)}
    )
    public void testUnconnectedEvents(Throwable throwable) {
        assertThat(throwable).hasCauseInstanceOf(IllegalArgumentException.class).hasMessageContaining("not connection");
    }
    
    @Test
    @KogitoUnitTestDeployment(
        resources = {@KogitoUnitTestResource(path = DIFFERENT_PROCESS)}
    )
    public void testDifferentProcess(Throwable throwable) {
        assertThat(throwable).hasCauseInstanceOf(IllegalArgumentException.class).hasMessageContainingAll("not connection", "subprocess");
    }
}
